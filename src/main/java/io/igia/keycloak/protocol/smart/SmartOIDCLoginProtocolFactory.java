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

import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.oidc.OIDCLoginProtocolFactory;

public class SmartOIDCLoginProtocolFactory extends OIDCLoginProtocolFactory{
	public static final String LOGIN_PROTOCOL = "smart-openid-connect";
	
    @Override
    public LoginProtocol create(KeycloakSession session) {
        return new SmartOIDCLoginProtocol().setSession(session);
    }
    
    @Override
    public Object createProtocolEndpoint(RealmModel realm, EventBuilder event) {
        return new SmartOIDCLoginProtocolService(realm, event);
    }

    @Override
    public String getId() {
        return LOGIN_PROTOCOL;
    }
    
    @Override
    protected void createDefaultClientScopesImpl(RealmModel newRealm) {
    	// The method is empty on purpose.
    	// To avoid recreating default setup
    }
}
