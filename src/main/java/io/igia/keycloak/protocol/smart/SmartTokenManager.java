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

import java.util.Map.Entry;

import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessTokenResponse;

import io.igia.keycloak.authentication.SmartLaunchContextAuthenticator;

public class SmartTokenManager extends TokenManager {	
	
	@Override
	public AccessTokenResponseBuilder responseBuilder(RealmModel realm, ClientModel client, EventBuilder event, KeycloakSession session,
            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
		return new SmartAccessTokenResponseBuilder(realm, client, event, session, userSession, clientSessionCtx);
	}
	
	public class SmartAccessTokenResponseBuilder extends TokenManager.AccessTokenResponseBuilder {
        ClientSessionContext clientSessionContext;
        
        public SmartAccessTokenResponseBuilder(RealmModel realm, ClientModel client, EventBuilder event, KeycloakSession session,
        		UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        	super(realm, client, event, session, userSession, clientSessionCtx);	
            this.clientSessionContext = clientSessionCtx;
        }
		
		@Override
		public AccessTokenResponse build() {
			AccessTokenResponse res = buildAccessTokenResponse();
            
            //add all SMART launch context params from client session notes to response
            for(Entry<String, String> entry : clientSessionContext.getClientSession().getNotes().entrySet()) {
            		if(entry.getKey().startsWith(SmartLaunchContextAuthenticator.LAUNCH_SCOPE_PREFIX) &&
            				entry.getValue() != null && !entry.getValue().isEmpty()) {            				     		                    		       
            			res.setOtherClaims(entry.getKey().substring(SmartLaunchContextAuthenticator.LAUNCH_SCOPE_PREFIX.length()), 
            					entry.getValue());              			
            		}
            }
            
			return res;
		}
	
		protected AccessTokenResponse buildAccessTokenResponse() {
			return super.build();
		}
	}
}
