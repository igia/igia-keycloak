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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.igia.keycloak.protocol.smart.ClientSessionNoteMapper;

@RunWith(MockitoJUnitRunner.class)
public class ClientSessionNoteMapperTest {
	@Test
	public void testSetClaim() {		
		IDToken token = new AccessToken();		
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		KeycloakSession session = Mockito.mock(KeycloakSession.class);
		AuthenticatedClientSessionModel clientSession = Mockito.mock(AuthenticatedClientSessionModel.class);	
		
		Mockito.when(clientSession.getNote("clientSessionNote")).thenReturn("clientSessionNoteValue");
		
		ClientSessionNoteMapper clientSessionNoteMapper = new ClientSessionNoteMapper();
		ProtocolMapperModel mappingModel = 
				ClientSessionNoteMapper.createClaimMapper("name", "clientSessionNote", "tokenClaimName", "jsonType", true, true);
		clientSessionNoteMapper.setClaim(token, mappingModel, userSession, session, clientSession);
	
		assertTrue("Token claim name matches expected value", 
				token.getOtherClaims().get("tokenClaimName").equals("clientSessionNoteValue"));
	}
	
	@Test
	public void testSetClaimNullNoteValue() {		
		IDToken token = new AccessToken();
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		KeycloakSession session = Mockito.mock(KeycloakSession.class);
		AuthenticatedClientSessionModel clientSession = Mockito.mock(AuthenticatedClientSessionModel.class);	
		
		Mockito.when(clientSession.getNote("clientSessionNote")).thenReturn(null);
		
		ClientSessionNoteMapper clientSessionNoteMapper = new ClientSessionNoteMapper();
		ProtocolMapperModel mappingModel = 
				ClientSessionNoteMapper.createClaimMapper("name", "clientSessionNote", "tokenClaimName", "jsonType", true, true);
		clientSessionNoteMapper.setClaim(token, mappingModel, userSession, session, clientSession);
	
		assertTrue("No other claim with claim name", 
				token.getOtherClaims().get("tokenClaimName") == null);
	}

	@Test
	public void testTransformAccessToken() {		
		AccessToken token = new AccessToken();	
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		KeycloakSession session = Mockito.mock(KeycloakSession.class);
		AuthenticatedClientSessionModel clientSession = Mockito.mock(AuthenticatedClientSessionModel.class);	
		
		Mockito.when(clientSession.getNote("clientSessionNote")).thenReturn("clientSessionNoteValue");
		
		ClientSessionNoteMapper clientSessionNoteMapper = new ClientSessionNoteMapper();
		ProtocolMapperModel mappingModel = 
				ClientSessionNoteMapper.createClaimMapper("name", "clientSessionNote", "tokenClaimName", "jsonType", true, true);
		clientSessionNoteMapper.transformAccessToken(token, mappingModel, session, userSession, clientSession);
	
		assertTrue("Token claim name matches expected value",
				token.getOtherClaims().get("tokenClaimName").equals("clientSessionNoteValue"));
	}
	
	@Test
	public void testTransformAccessTokenIncludeFalse() {		
		AccessToken token = new AccessToken();	
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		KeycloakSession session = Mockito.mock(KeycloakSession.class);
		AuthenticatedClientSessionModel clientSession = Mockito.mock(AuthenticatedClientSessionModel.class);			
		
		ClientSessionNoteMapper clientSessionNoteMapper = new ClientSessionNoteMapper();
		ProtocolMapperModel mappingModel = 
				ClientSessionNoteMapper.createClaimMapper("name", "clientSessionNote", "tokenClaimName", "jsonType", false, true);
		clientSessionNoteMapper.transformAccessToken(token, mappingModel, session, userSession, clientSession);
	
		assertTrue("No other claim with claim name", 
				token.getOtherClaims().get("tokenClaimName") == null);
	}
	
	@Test
	public void testTransformIdToken() {		
		IDToken token = new IDToken();		
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		KeycloakSession session = Mockito.mock(KeycloakSession.class);
		AuthenticatedClientSessionModel clientSession = Mockito.mock(AuthenticatedClientSessionModel.class);	
		
		Mockito.when(clientSession.getNote("clientSessionNote")).thenReturn("clientSessionNoteValue");
		
		ClientSessionNoteMapper clientSessionNoteMapper = new ClientSessionNoteMapper();
		ProtocolMapperModel mappingModel = 
				ClientSessionNoteMapper.createClaimMapper("name", "clientSessionNote", "tokenClaimName", "jsonType", true, true);
		clientSessionNoteMapper.transformIDToken(token, mappingModel, session, userSession, clientSession);
	
		assertTrue("Token claim name matches expected value",
				token.getOtherClaims().get("tokenClaimName").equals("clientSessionNoteValue"));
	}
	
	@Test
	public void testTransformIdTokenIncludeFalse() {		
		IDToken token = new IDToken();	
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		KeycloakSession session = Mockito.mock(KeycloakSession.class);
		AuthenticatedClientSessionModel clientSession = Mockito.mock(AuthenticatedClientSessionModel.class);			
		
		ClientSessionNoteMapper clientSessionNoteMapper = new ClientSessionNoteMapper();
		ProtocolMapperModel mappingModel = 
				ClientSessionNoteMapper.createClaimMapper("name", "clientSessionNote", "tokenClaimName", "jsonType", true, false);
		clientSessionNoteMapper.transformIDToken(token, mappingModel, session, userSession, clientSession);
	
		assertTrue("No other claim with claim name", 
				token.getOtherClaims().get("tokenClaimName") == null);
	}
}
