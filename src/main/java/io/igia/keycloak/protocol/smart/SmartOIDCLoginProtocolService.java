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
package io.igia.keycloak.protocol.smart;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.AuthenticationFlowResolver;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.endpoints.TokenEndpoint;
import org.keycloak.protocol.oidc.endpoints.UserInfoEndpoint;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.services.resources.SessionCodeChecks;
import org.keycloak.services.util.BrowserHistoryHelper;
import org.keycloak.sessions.AuthenticationSessionModel;

import io.igia.keycloak.authentication.SmartLaunchContextAuthenticatorFactory;

public class SmartOIDCLoginProtocolService {
	private static final Logger LOG = Logger.getLogger(SmartOIDCLoginProtocolService.class);
	
	public static final String FORWARDED_ERROR_MESSAGE_NOTE = "forwardedErrorMessage";
	
	private RealmModel realm;
	private TokenManager tokenManager;
	private EventBuilder event;
	
    @Context
    private UriInfo uriInfo;

    @Context
    private KeycloakSession session;

    @Context
    private HttpHeaders headers;

    @Context
    private HttpRequest request;
    
    @Context
    private ClientConnection clientConnection;
	
	public SmartOIDCLoginProtocolService(RealmModel realm, EventBuilder event) {
		this.realm = realm;        
        this.event = event;
        this.tokenManager = new SmartTokenManager();
	}
		
    public static UriBuilder tokenServiceBaseUrl(UriBuilder baseUriBuilder) {
        return baseUriBuilder.path(RealmsResource.class).path("{realm}/protocol/" + SmartOIDCLoginProtocolFactory.LOGIN_PROTOCOL);
    }

    public static UriBuilder tokenUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(SmartOIDCLoginProtocolService.class, "token");
    }
    
    public static UriBuilder userInfoUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(SmartOIDCLoginProtocolService.class, "issueUserInfo");
    }
    
    @Path("token")
    public Object token() {
        TokenEndpoint endpoint = new TokenEndpoint(tokenManager, realm, event);
        ResteasyProviderFactory.getInstance().injectProperties(endpoint);
        return endpoint;
    }
    
    @Path("userinfo")
    public Object issueUserInfo() {
        UserInfoEndpoint endpoint = new UserInfoEndpoint(tokenManager, realm);
        ResteasyProviderFactory.getInstance().injectProperties(endpoint);
        return endpoint;
    }
    
    @Path("smart-launch-context")
    @GET
    public Response executeSmartLaunchContext(
    		@QueryParam(LoginActionsService.AUTH_SESSION_ID) String authSessionId,
    		@QueryParam(LoginActionsService.SESSION_CODE) String code,
    		@QueryParam(Constants.EXECUTION) String execution,
            @QueryParam(Constants.CLIENT_ID) String clientId,
            @QueryParam(Constants.TAB_ID) String tabId){ 
    	LOG.debugf("smart-launch-context authSessionId %s, code %s, execution %s, clientId %s, tabId %s", 
    			authSessionId, code, execution, clientId, tabId);
    	return handleSmartLaunchContext(authSessionId, code, execution, clientId, tabId);
    }
    
    public Response handleSmartLaunchContext(
    		String authSessionId,
    		String code,
			String execution,
        	String clientId,
        	String tabId) {                
        // Setup client, so error page will contain "back to application" link
        ClientModel client = null;
        if (clientId != null) {
            client = realm.getClientByClientId(clientId);
        }
        if (client != null) {
            session.getContext().setClient(client);
        }

        String flowPath = LoginActionsService.AUTHENTICATE_PATH;
        
        //verify session code
        LOG.debugf("Calling checksForCode");
        SessionCodeChecks checks = checksForCode(authSessionId, code, execution, clientId, tabId, flowPath);
        if (!checks.verifyActiveAndValidAction(AuthenticationSessionModel.Action.AUTHENTICATE.name(),
        		ClientSessionCode.ActionType.LOGIN)) {
            return checks.getResponse();
        }
        
        AuthenticationSessionModel authSession = checks.getAuthenticationSession();    

        authSession.setAuthNote(SmartLaunchContextAuthenticatorFactory.INITIATED_BY_SMART_LAUNCH_EXT_APP, "true");               
        return processFlow(true, execution, authSession, flowPath, AuthenticationFlowResolver.resolveBrowserFlow(authSession), null, new AuthenticationProcessor());		
    }
    
    private SessionCodeChecks checksForCode(String authSessionId, String code, String execution, String clientId, String tabId, String flowPath) {
        SessionCodeChecks res = new SessionCodeChecks(realm, session.getContext().getUri(), request, clientConnection, session, event, authSessionId, code, execution, clientId, tabId, flowPath);
        res.initialVerify();
        return res;
	}
    
    protected Response processFlow(boolean action, String execution, AuthenticationSessionModel authSession, String flowPath, AuthenticationFlowModel flow, String errorMessage, AuthenticationProcessor processor) {
        processor.setAuthenticationSession(authSession)
                .setFlowPath(flowPath)
                .setBrowserFlow(true)
                .setFlowId(flow.getId())
                .setConnection(clientConnection)
                .setEventBuilder(event)
                .setRealm(realm)
                .setSession(session)
                .setUriInfo(session.getContext().getUri())
                .setRequest(request);
        if (errorMessage != null) {
            processor.setForwardedErrorMessage(new FormMessage(null, errorMessage));
        }

        // Check the forwarded error message, which was set by previous HTTP request
        String forwardedErrorMessage = authSession.getAuthNote(FORWARDED_ERROR_MESSAGE_NOTE);
        if (forwardedErrorMessage != null) {
            authSession.removeAuthNote(FORWARDED_ERROR_MESSAGE_NOTE);
            processor.setForwardedErrorMessage(new FormMessage(null, forwardedErrorMessage));
        }

        Response response;
        try {
            if (action) {
                response = processor.authenticationAction(execution);
            } else {
                response = processor.authenticate();
            }
        } catch (WebApplicationException e) {
            response = e.getResponse();
            authSession = processor.getAuthenticationSession();
        } catch (Exception e) {
            response = processor.handleBrowserException(e);
            authSession = processor.getAuthenticationSession(); // Could be changed (eg. Forked flow)
        }

        return BrowserHistoryHelper.getInstance().saveResponseAndRedirect(session, authSession, response, action, request);
    }
}
