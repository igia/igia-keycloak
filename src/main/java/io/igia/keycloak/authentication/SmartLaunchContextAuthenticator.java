/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 *
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
 */
package io.igia.keycloak.authentication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.TokenVerifier;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Base64;
import org.keycloak.common.util.Time;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpoint;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.services.util.DefaultClientSessionContext;
import org.keycloak.sessions.AuthenticationSessionModel;

import io.igia.keycloak.protocol.smart.SmartOIDCLoginProtocolFactory;
import io.igia.keycloak.protocol.smart.SmartOIDCLoginProtocolService;
import io.igia.keycloak.protocol.smart.SmartTokenManager;

public class SmartLaunchContextAuthenticator implements Authenticator {
	protected static ServicesLogger logger = ServicesLogger.LOGGER;

	public static final String LAUNCH_SCOPE_PREFIX = "launch/";

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA256";
	private static final String AUD_PARAM = "aud";

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		// return attempted if external launch authenticator does not support
		// any of app launch/* requested or default launch scopes
		String scope = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.SCOPE_PARAM);
		Map<String, ClientScopeModel> defaultScopes = context.getAuthenticationSession().getClient().getClientScopes(true, true);

		if (scope == null) {
			scope = "";
		}

		logger.debugf("Requested scope: %s", scope);

		List<String> scopes = Arrays.stream(scope.split("\\s+")).collect(Collectors.toList());
		String supportedParams = context.getAuthenticatorConfig().getConfig()
			.get(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_SUPPORTED_PARAMS);
		if(supportedParams == null || supportedParams.isEmpty()) {
			context.attempted();
			return;
		}

		List<String> params = Arrays.stream(supportedParams.split("\\s+")).collect(Collectors.toList());
		boolean foundMatch = false;
		for(String param: params) {
			if(scopes.contains(LAUNCH_SCOPE_PREFIX + param) ||
					defaultScopes.containsKey(LAUNCH_SCOPE_PREFIX + param)){
				foundMatch = true;
			}
		}

		if(!foundMatch) {
			context.attempted();
			return;
		}

		// redirect to external smart launch URL
		if (context.getAuthenticatorConfig() != null && context.getAuthenticatorConfig().getConfig()
				.containsKey(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_URL)) {
			String redirectUrl = context.getAuthenticatorConfig().getConfig()
					.get(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_URL);
			if (redirectUrl != null && !redirectUrl.isEmpty()) {
				redirect(context, redirectUrl);
				return;
			}
		}

		// use keycloak form template as default if external launch url not configured
		Response challenge = context.form().createForm("smart-launch.ftl");
		context.challenge(challenge);
	}

	private void redirect(AuthenticationFlowContext context, String redirectUrl) {
		String accessCode = generateSessionCode(context);
		String authSessionId = context.getAuthenticationSession().getParentSession().getId();
		String clientId = context.getAuthenticationSession().getClient().getClientId();
		String tabId = context.getAuthenticationSession().getTabId();

		// generate redirect URL
		String aud = context.getAuthenticationSession().getClientNote(AuthorizationEndpoint.LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX + AUD_PARAM);
		String accessToken = generateSmartLaunchAppToken(context);
		try {
			redirectUrl = redirectUrl + "&" + AUD_PARAM + "=" + URLEncoder.encode(aud, StandardCharsets.UTF_8.toString())
				+ "&" + OAuth2Constants.ACCESS_TOKEN + "=" + accessToken;
		} catch (UnsupportedEncodingException e1) {
			throw new RuntimeException(e1);
		}

		// generate launch token
		String externalLaunchToken = smartLaunchBuilder(context.getUriInfo().getBaseUri(),
				authSessionId, accessCode, clientId, tabId)
				.queryParam(Constants.EXECUTION, context.getExecution().getId())
				.queryParam(SmartLaunchContextAuthenticatorFactory.QUERY_PARAM_APP_TOKEN, "{tokenParameterName}")
				.build(context.getRealm().getName(), "{APP_TOKEN}").toString();

		Response challenge;
		try {
			challenge = Response.status(Status.FOUND)
					.header("Location", redirectUrl.replace("{TOKEN}", URLEncoder.encode(externalLaunchToken, StandardCharsets.UTF_8.toString())))
					.build();
			logger.debugf("Redirecting to %s", redirectUrl.replace("{TOKEN}", URLEncoder.encode(externalLaunchToken, StandardCharsets.UTF_8.toString())));
			context.forceChallenge(challenge);
			return;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		final AuthenticationSessionModel authSession = context.getAuthenticationSession();
        if (! Objects.equals(authSession.getAuthNote(SmartLaunchContextAuthenticatorFactory.INITIATED_BY_SMART_LAUNCH_EXT_APP), "true")) {
    		// handle keycloak template form
    		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
    		if (formData.containsKey("cancel")) {
    			context.cancelLogin();
    			return;
    		}

    		for(String key : formData.keySet()) {
    			String value = formData.getFirst(key);
    			context.getAuthenticationSession().setClientNote(LAUNCH_SCOPE_PREFIX + key, value);
    		}

    		context.success();
            return;
        }

        authSession.removeAuthNote(SmartLaunchContextAuthenticatorFactory.INITIATED_BY_SMART_LAUNCH_EXT_APP);

		String appTokenString = context.getUriInfo().getQueryParameters()
			.getFirst(SmartLaunchContextAuthenticatorFactory.QUERY_PARAM_APP_TOKEN);

		if (appTokenString == null || appTokenString.isEmpty()) {
			// no launch context token provided by external app, try again
			authenticate(context);
			return;
		}

		// check for valid signature
		try {
			isApplicationTokenValid(context);
		} catch (VerificationException e) {
			logger.error("Invalid external application token signature.", e);
			context.failure(AuthenticationFlowError.INVALID_CLIENT_SESSION);
			return;
		} catch (IOException e) {
			logger.error("Invalid external application token signature.", e);
			context.failure(AuthenticationFlowError.INVALID_CLIENT_SESSION);
			return;
		}

		JsonWebToken appToken = null;
		try {
			appToken = TokenVerifier.create(appTokenString, JsonWebToken.class).getToken();
		} catch (VerificationException e) {
			logger.error("Invalid external application JWT token.", e);
			context.failure(AuthenticationFlowError.INTERNAL_ERROR);
			return;
		}

		// check for presence of required launch claims
		if(!hasRequiredLaunchClaims(context, appToken)) {
			context.failure(AuthenticationFlowError.INVALID_CLIENT_SESSION);
			return;
		}else {
			//add launch context parameters to client session
			for(Entry<String, Object> entry: appToken.getOtherClaims().entrySet()) {
				context.getAuthenticationSession().setClientNote(LAUNCH_SCOPE_PREFIX + entry.getKey(),
						entry.getValue().toString());
			}
			context.success();
			return;
		}
	}

	private static UriBuilder smartLaunchBuilder(URI baseUri, String authSessionId, String accessCode, String clientId, String tabId) {
		return Urls.realmBase(baseUri).path("{realm}/protocol/" + SmartOIDCLoginProtocolFactory.LOGIN_PROTOCOL)
				.path(SmartOIDCLoginProtocolService.class, "executeSmartLaunchContext")
				.replaceQueryParam(LoginActionsService.SESSION_CODE, accessCode)
				.replaceQueryParam(Constants.CLIENT_ID, clientId)
				.replaceQueryParam(Constants.TAB_ID, tabId)
				.replaceQueryParam(LoginActionsService.AUTH_SESSION_ID, authSessionId);
	}

    private String generateSessionCode(AuthenticationFlowContext context) {
        ClientSessionCode<AuthenticationSessionModel> accessCode = new ClientSessionCode<>(
        		context.getSession(), context.getRealm(), context.getAuthenticationSession());
        context.getAuthenticationSession().getParentSession().setTimestamp(Time.currentTime());
        accessCode.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        return accessCode.getOrGenerateCode();
    }

	private String generateSmartLaunchAppToken(AuthenticationFlowContext context) {
        UserSessionModel userSession = context.getSession().sessions().createUserSession(
        		context.getAuthenticationSession().getParentSession().getId(),
        		context.getRealm(),
        		context.getUser(), context.getUser().getUsername(),
        		context.getConnection().getRemoteAddr(),
        		"auth", false, null, null);

        String clientId = context.getAuthenticatorConfig().getConfig()
        		.get(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_CLIENT_ID);
        ClientModel client = context.getRealm().getClientByClientId(clientId);
        AuthenticatedClientSessionModel clientSession = context.getSession().sessions()
        		.createClientSession(context.getRealm(), client, userSession);
        DefaultClientSessionContext clientSessionContext =
        		DefaultClientSessionContext.fromClientSessionAndClientScopes(clientSession,
                    new HashSet<>(client.getClientScopes(true, true).values()),
                        context.getSession());

        TokenManager tokenManager = new SmartTokenManager();
        AccessToken accessToken = tokenManager.createClientAccessToken(context.getSession(),
        		context.getRealm(), client,
        		context.getAuthenticationSession().getAuthenticatedUser(),
        		userSession,
        		clientSessionContext);
        accessToken.issuer(Urls.realmIssuer(context.getUriInfo().getBaseUri(),
        		context.getRealm().getName()));

        try {
			tokenManager.checkTokenValidForIntrospection(context.getSession(), context.getRealm(), accessToken);
			String encodedToken = context.getSession().tokens().encode(accessToken);
			return encodedToken;
		} catch (OAuthErrorException e) {
			return null;
		}
	}

	private boolean isApplicationTokenValid(AuthenticationFlowContext context)
			throws VerificationException, IOException {
		String appTokenString = context.getUriInfo().getQueryParameters()
				.getFirst(SmartLaunchContextAuthenticatorFactory.QUERY_PARAM_APP_TOKEN);
		String secret = context.getAuthenticatorConfig().getConfig()
				.get(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_SECRET_KEY);
		SecretKeySpec hmacSecretKeySpec = new SecretKeySpec(Base64.decode(secret), HMAC_SHA1_ALGORITHM);

		TokenVerifier.create(appTokenString, JsonWebToken.class).secretKey(hmacSecretKeySpec).verify();

		return true;
	}

	private boolean hasRequiredLaunchClaims(AuthenticationFlowContext context, JsonWebToken appToken) {
		String scope = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.SCOPE_PARAM);
		Map<String, ClientScopeModel> defaultScopes = context.getAuthenticationSession().getClient().getClientScopes(true, true);

		if (scope == null) {
			scope = "";
		}

		List<String> scopes = Arrays.stream(scope.split("\\s+")).collect(Collectors.toList());
		String supportedParams = context.getAuthenticatorConfig().getConfig()
			.get(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_SUPPORTED_PARAMS);
		List<String> params = Arrays.stream(supportedParams.split("\\s+")).collect(Collectors.toList());

		for(String param: params) {
			boolean isRequiredClaim = false;
			if(scopes.contains(LAUNCH_SCOPE_PREFIX + param) ||
					defaultScopes.containsKey(LAUNCH_SCOPE_PREFIX + param)){
				isRequiredClaim = true;
			}

			if(isRequiredClaim) {
				String requiredClaim = (String) appToken.getOtherClaims().get(param);
				if (requiredClaim == null || requiredClaim.isEmpty()) {
					logger.error("External SMART launch application token missing required claims: " + param);
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean requiresUser() {
		return false;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		// run for all realms and users
		return true;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		//no required actions
	}

	@Override
	public void close() {
		//no close actions
	}
}
