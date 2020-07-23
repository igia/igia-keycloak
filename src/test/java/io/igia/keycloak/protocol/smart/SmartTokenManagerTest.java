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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.igia.keycloak.authentication.SmartLaunchContextAuthenticator;
import io.igia.keycloak.protocol.smart.SmartTokenManager;
import io.igia.keycloak.protocol.smart.SmartTokenManager.SmartAccessTokenResponseBuilder;

@RunWith(MockitoJUnitRunner.class)
public class SmartTokenManagerTest {
	@Test
	public void testBuild() {
		RealmModel realm = Mockito.mock(RealmModel.class);
		ClientModel client = Mockito.mock(ClientModel.class);
		EventBuilder event = Mockito.mock(EventBuilder.class);
		KeycloakSession session = Mockito.mock(KeycloakSession.class);
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		ClientSessionContext clientSessionCtx = Mockito.mock(ClientSessionContext.class);
		AuthenticatedClientSessionModel authenticatedClientSessionModel = Mockito.mock(AuthenticatedClientSessionModel.class);

		SmartTokenManager smartTokenManager = Mockito.spy(new SmartTokenManager());
		SmartAccessTokenResponseBuilder spy = Mockito.spy(
				smartTokenManager.new SmartAccessTokenResponseBuilder(realm, client, event, session, userSession, clientSessionCtx));

		Map<String, String> notes = new HashMap<String, String>();
		notes.put(SmartLaunchContextAuthenticator.LAUNCH_SCOPE_PREFIX + "note1", "note1value");
		notes.put(SmartLaunchContextAuthenticator.LAUNCH_SCOPE_PREFIX + "note2", "note2value");

		Mockito.doReturn(new AccessTokenResponse()).when(spy).buildAccessTokenResponse();
		Mockito.when(clientSessionCtx.getClientSession()).thenReturn(authenticatedClientSessionModel);
		Mockito.when(authenticatedClientSessionModel.getNotes()).thenReturn(notes);

		AccessTokenResponse res = spy.build();
        assertEquals("AccessTokenResponse contains note1 correct value", "note1value", res.getOtherClaims().get("note1"));
        assertEquals("AccessTokenResponse contains note2 correct value", "note2value", res.getOtherClaims().get("note2"));
	}

	@Test
	public void testBuildEmptyNote() {
		RealmModel realm = Mockito.mock(RealmModel.class);
		ClientModel client = Mockito.mock(ClientModel.class);
		EventBuilder event = Mockito.mock(EventBuilder.class);
		KeycloakSession session = Mockito.mock(KeycloakSession.class);
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		ClientSessionContext clientSessionCtx = Mockito.mock(ClientSessionContext.class);
		AuthenticatedClientSessionModel authenticatedClientSessionModel = Mockito.mock(AuthenticatedClientSessionModel.class);

		SmartTokenManager smartTokenManager = Mockito.spy(new SmartTokenManager());
		SmartAccessTokenResponseBuilder spy = Mockito.spy(
				smartTokenManager.new SmartAccessTokenResponseBuilder(realm, client, event, session, userSession, clientSessionCtx));

		Map<String, String> notes = new HashMap<String, String>();
		notes.put(SmartLaunchContextAuthenticator.LAUNCH_SCOPE_PREFIX + "note1", "");

		Mockito.doReturn(new AccessTokenResponse()).when(spy).buildAccessTokenResponse();
		Mockito.when(clientSessionCtx.getClientSession()).thenReturn(authenticatedClientSessionModel);
		Mockito.when(authenticatedClientSessionModel.getNotes()).thenReturn(notes);

		AccessTokenResponse res = spy.build();
        assertNull("AccessTokenResponse does not contain empty claim value", res.getOtherClaims().get("note1"));
	}

	@Test
	public void testBuildNullNote() {
		RealmModel realm = Mockito.mock(RealmModel.class);
		ClientModel client = Mockito.mock(ClientModel.class);
		EventBuilder event = Mockito.mock(EventBuilder.class);
		KeycloakSession session = Mockito.mock(KeycloakSession.class);
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		ClientSessionContext clientSessionCtx = Mockito.mock(ClientSessionContext.class);
		AuthenticatedClientSessionModel authenticatedClientSessionModel = Mockito.mock(AuthenticatedClientSessionModel.class);

		SmartTokenManager smartTokenManager = Mockito.spy(new SmartTokenManager());
		SmartAccessTokenResponseBuilder spy = Mockito.spy(
				smartTokenManager.new SmartAccessTokenResponseBuilder(realm, client, event, session, userSession, clientSessionCtx));

		Map<String, String> notes = new HashMap<String, String>();
		notes.put(SmartLaunchContextAuthenticator.LAUNCH_SCOPE_PREFIX + "note1", null);

		Mockito.doReturn(new AccessTokenResponse()).when(spy).buildAccessTokenResponse();
		Mockito.when(clientSessionCtx.getClientSession()).thenReturn(authenticatedClientSessionModel);
		Mockito.when(authenticatedClientSessionModel.getNotes()).thenReturn(notes);

		AccessTokenResponse res = spy.build();
        assertNull("AccessTokenResponse does not contain null claim value", res.getOtherClaims().get("note1"));
	}
}
