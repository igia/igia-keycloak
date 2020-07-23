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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.common.ClientConnection;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.models.TokenManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpoint;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import io.igia.keycloak.authentication.SmartLaunchContextAuthenticator;
import io.igia.keycloak.authentication.SmartLaunchContextAuthenticatorFactory;

@RunWith(MockitoJUnitRunner.class)
public class SmartLaunchContextAuthenticatorTest {
	private AuthenticationFlowContext context;
	private AuthenticationSessionModel sessionModel;
	private ClientModel clientModel;
	private AuthenticatorConfigModel authenticatorConfigModel;
	private RealmModel realm;
	private KeycloakSession session;
	private KeycloakContext keycloakContext;
	private RootAuthenticationSessionModel rootSession;
	private UserModel userModel;
	private ClientConnection clientConnection;
	private UserSessionProvider userSessionProvider;
	private UserSessionModel userSessionModel;
	private AuthenticatedClientSessionModel launchClientSessionModel;
	private ClientModel launchClientModel;
	private KeycloakUriInfo uriInfo;
	private TokenManager tokenManager;
	private AuthenticationExecutionModel executionModel;
	private Map<String, ClientScopeModel> defaultClientScopes;
	private Map<String, ClientScopeModel> defaultLaunchClientScopes;
	private Map<String, String> authenticatorConfig;

	private Response response;
	private Boolean attempted;

	@Test
	public void testAuthenticate() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
		setupDefaultMocks();

		SmartLaunchContextAuthenticator authenticator = new SmartLaunchContextAuthenticator();
		authenticator.authenticate(context);

        assertNotNull("Challenge response not null.", response);
        assertNotNull("Location header exists.", response.getMetadata().get("Location").get(0));

		String location = response.getMetadata().get("Location").get(0).toString();
		URI uri = new URI(location);
		assertTrue("Location host matches expected.", uri.getHost().equalsIgnoreCase("externallaunch.com"));

		List<NameValuePair> params = URLEncodedUtils.parse(uri, Charset.forName("UTF-8"));

		String aud = getQueryValue("aud", params);
        assertNotNull("Location contains aud query param.", aud);
        assertEquals("Aud query param expected value.", "http://fhirserver.org", aud);
		String access_token = getQueryValue("access_token", params);
        assertNotNull("Location contains access token query param.", access_token);
        assertEquals("Access token query param expected value.", "encodedtoken", access_token);

		String token = getQueryValue("token", params);
        assertNotNull("Location contains token query param.", token);
		assertTrue("Token query param host and path as expected.", token.startsWith("http://keycloak/realms/realmName/protocol/smart-openid-connect/smart-launch-context?"));
		assertTrue("Token query param contains session_code.", token.contains("session_code"));
		assertTrue("Token query param contains client_id.", token.contains("client_id=clientId"));
		assertTrue("Token query param contains tab_id.", token.contains("tab_id=tabId"));
		assertTrue("Token query param contains auth_session_id.", token.contains("auth_session_id=rootSessionId"));
		assertTrue("Token query param contains execution.", token.contains("execution=executionId"));
		assertTrue("Token query param contains app-token.", token.contains("app-token=%7BAPP_TOKEN%7D"));
	}

	@Test
	public void testAuthenticateScopeNoMatch() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
		setupDefaultMocks();
		//override mocks
		Mockito.when(sessionModel.getClientNote(OIDCLoginProtocol.SCOPE_PARAM)).thenReturn("launch/encounter");

		SmartLaunchContextAuthenticator authenticator = new SmartLaunchContextAuthenticator();
		authenticator.authenticate(context);

		assertTrue("Context attempted.", attempted);
	}

	@Test
	public void testAuthenticateDefaultScopeMatch() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
		setupDefaultMocks();
		//override mocks
		Mockito.when(sessionModel.getClientNote(OIDCLoginProtocol.SCOPE_PARAM)).thenReturn("launch/encounter");
		defaultClientScopes.put("launch/patient", Mockito.mock(ClientScopeModel.class));
		Mockito.when(clientModel.getClientScopes(true, true)).thenReturn(defaultClientScopes);

		SmartLaunchContextAuthenticator authenticator = new SmartLaunchContextAuthenticator();
		authenticator.authenticate(context);

        assertNotNull("Challenge response not null.", response);
	}

	@Test
	public void testAuthenticateSupportedConfigNull() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
		setupDefaultMocks();
		//override mocks
		authenticatorConfig.put(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_SUPPORTED_PARAMS,
				null);

		SmartLaunchContextAuthenticator authenticator = new SmartLaunchContextAuthenticator();
		authenticator.authenticate(context);

		assertTrue("Context attempted.", attempted);
	}

	@Test
	public void testAuthenticateSupportedConfigEmpty() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
		setupDefaultMocks();
		//override mocks
		authenticatorConfig.put(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_SUPPORTED_PARAMS,
				"");

		SmartLaunchContextAuthenticator authenticator = new SmartLaunchContextAuthenticator();
		authenticator.authenticate(context);

		assertTrue("Context attempted.", attempted);
	}

	private void setupDefaultMocks() {
		context = Mockito.mock(AuthenticationFlowContext.class);
		sessionModel = Mockito.mock(AuthenticationSessionModel.class);
		clientModel = Mockito.mock(ClientModel.class);
		authenticatorConfigModel = Mockito.mock(AuthenticatorConfigModel.class);
		realm = Mockito.mock(RealmModel.class);
		session = Mockito.mock(KeycloakSession.class);
		keycloakContext = Mockito.mock(KeycloakContext.class);
		rootSession = Mockito.mock(RootAuthenticationSessionModel.class);
		userModel = Mockito.mock(UserModel.class);
		clientConnection = Mockito.mock(ClientConnection.class);
		userSessionProvider = Mockito.mock(UserSessionProvider.class);
		userSessionModel = Mockito.mock(UserSessionModel.class);
		launchClientSessionModel = Mockito.mock(AuthenticatedClientSessionModel.class);
		launchClientModel = Mockito.mock(ClientModel.class);
		uriInfo = Mockito.mock(KeycloakUriInfo.class);
		tokenManager = Mockito.mock(TokenManager.class);
		executionModel = Mockito.mock(AuthenticationExecutionModel.class);
		defaultClientScopes = new HashMap<String, ClientScopeModel>();
		defaultLaunchClientScopes = new HashMap<String, ClientScopeModel>();
		authenticatorConfig = new HashMap<String, String>();

		// keycloak context
		Mockito.when(session.getContext()).thenReturn(keycloakContext);
		Mockito.when(keycloakContext.getUri()).thenReturn(uriInfo);
//		Mockito.when(keycloakContext.getRealm()).thenReturn(realm);
		Mockito.when(session.sessions()).thenReturn(userSessionProvider);
		Mockito.when(session.tokens()).thenReturn(tokenManager);
		Mockito.when(tokenManager.encode(any())).thenReturn("encodedtoken");
		// realm
		Mockito.when(realm.getName()).thenReturn("realmName");
		//auth context
		Mockito.when(context.getRealm()).thenReturn(realm);
		Mockito.when(context.getSession()).thenReturn(session);
		Mockito.when(context.getAuthenticationSession()).thenReturn(sessionModel);
		Mockito.when(context.getUser()).thenReturn(userModel);
		Mockito.when(context.getConnection()).thenReturn(clientConnection);
		Mockito.when(clientConnection.getRemoteAddr()).thenReturn("remoteAddr");
		Mockito.when(context.getAuthenticatorConfig()).thenReturn(authenticatorConfigModel);
		Mockito.when(authenticatorConfigModel.getConfig()).thenReturn(authenticatorConfig);
		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(uriInfo.getBaseUri()).thenReturn(URI.create("http://keycloak"));
		Mockito.when(context.getExecution()).thenReturn(executionModel);
		Mockito.when(executionModel.getId()).thenReturn("executionId");
		// auth session
		Mockito.when(sessionModel.getParentSession()).thenReturn(rootSession);
		Mockito.when(rootSession.getId()).thenReturn("rootSessionId");
		Mockito.doNothing().when(rootSession).setTimestamp(anyInt());
		Mockito.when(sessionModel.getAuthenticatedUser()).thenReturn(userModel);
		Mockito.when(sessionModel.getClientNote(OIDCLoginProtocol.SCOPE_PARAM)).thenReturn("launch/patient");
		Mockito.when(sessionModel.getClientNote(AuthorizationEndpoint.LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX + "aud"))
				.thenReturn("http://fhirserver.org");
		Mockito.when(sessionModel.getClient()).thenReturn(clientModel);
		Mockito.when(sessionModel.getTabId()).thenReturn("tabId");
		// user
		Mockito.when(userModel.getUsername()).thenReturn("userName");
		Mockito.when(userModel.getId()).thenReturn("userId");
		// user session
		Mockito.when(userSessionModel.getNote(AuthenticationManager.AUTH_TIME)).thenReturn("1234567890");
		Mockito.when(userSessionProvider.createUserSession(anyString(), any(), any(), anyString(), anyString(), anyString(), eq(false), isNull(), isNull()))
			.thenReturn(userSessionModel);
		// app client
		Mockito.when(clientModel.getClientScopes(true, true)).thenReturn(defaultClientScopes);
		Mockito.when(clientModel.getClientId()).thenReturn("clientId");
		// launch client
		Mockito.when(launchClientModel.getClientScopes(true, true)).thenReturn(defaultLaunchClientScopes);
		Mockito.when(launchClientModel.getClientId()).thenReturn("launchClientid");
		Mockito.when(realm.getClientByClientId("launchClientid")).thenReturn(launchClientModel);
		// launch client session
		Mockito.when(launchClientSessionModel.getClient()).thenReturn(launchClientModel);
		Mockito.when(userSessionProvider.createClientSession(any(), any(), any())).thenReturn(launchClientSessionModel);

		// authenticator configuration
		authenticatorConfig.put(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_SUPPORTED_PARAMS,
				"patient");
		authenticatorConfig.put(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_URL,
				"http://externallaunch.com?token={TOKEN}");
		authenticatorConfig.put(SmartLaunchContextAuthenticatorFactory.CONFIG_EXTERNAL_SMART_LAUNCH_CLIENT_ID,
				"launchClientid");

		// capture challenge response
		response = null;
		Mockito.doAnswer(new Answer<Object>() {
		    @Override
		    public Object answer(InvocationOnMock invocation) throws Throwable {
		    	response = (Response) invocation.getArguments()[0];
		        return null;
		    }
		}).when(context).forceChallenge(any());

		// capture attempted
		attempted = false;
		Mockito.doAnswer(new Answer<Object>() {
		    @Override
		    public Object answer(InvocationOnMock invocation) throws Throwable {
		    	attempted = true;
		        return null;
		    }
		}).when(context).attempted();
	}

	private String getQueryValue(String key, List<NameValuePair> params) {
		for (NameValuePair param : params) {
			if(param.getName().equals(key)) {
				return param.getValue();
			}
		}
		return null;
	}
}
