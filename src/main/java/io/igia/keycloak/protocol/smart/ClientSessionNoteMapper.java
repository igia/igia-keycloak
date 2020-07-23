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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;

public class ClientSessionNoteMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, ServerInfoAwareProviderFactory {
	private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();
	private static final String CLIENT_SESSION_NOTE = "client.session.note";
	//TODO labels should be in theme admin-messages_en.properties, otherwise fail internationalization
	private static final String CLIENT_SESSION_MODEL_NOTE_LABEL = "Client Session Note"; //"clientSession.modelNote.label";
	private static final String CLIENT_SESSION_MODEL_NOTE_HELP_TEXT = "Name of stored client session note within the ClientSession.note map.";//"clientSession.modelNote.tooltip";

	static {
		ProviderConfigProperty property;
		property = new ProviderConfigProperty();
		property.setName(CLIENT_SESSION_NOTE);
		property.setLabel(CLIENT_SESSION_MODEL_NOTE_LABEL);
		property.setHelpText(CLIENT_SESSION_MODEL_NOTE_HELP_TEXT);
		property.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(property);
		OIDCAttributeMapperHelper.addAttributeConfig(configProperties, ClientSessionNoteMapper.class);
	}

	public static final String PROVIDER_ID = "smart-oidc-client-session-note-mapper";

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return CLIENT_SESSION_MODEL_NOTE_LABEL;
	}

	@Override
	public String getDisplayCategory() {
		return TOKEN_MAPPER_CATEGORY;
	}

	@Override
	public String getHelpText() {
		return "Map a custom client session note to a token claim.";
	}


	@Override
	protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession,
			KeycloakSession session, ClientSessionContext clientSessionCtx) {

		String noteName = mappingModel.getConfig().get(CLIENT_SESSION_NOTE);
		String noteValue = clientSessionCtx.getClientSession().getNote(noteName);
		if (noteValue == null) return;
		OIDCAttributeMapperHelper.mapClaim(token, mappingModel, noteValue);
	}

	public static ProtocolMapperModel createClaimMapper(String name,
			String clientSessionNote,
			String tokenClaimName, String jsonType,
			boolean accessToken, boolean idToken) {
		ProtocolMapperModel mapper = new ProtocolMapperModel();
		mapper.setName(name);
		mapper.setProtocolMapper(PROVIDER_ID);
		mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
		Map<String, String> config = new HashMap<String, String>();
		config.put(CLIENT_SESSION_NOTE, clientSessionNote);
		config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, tokenClaimName);
		config.put(OIDCAttributeMapperHelper.JSON_TYPE, jsonType);
		if (accessToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
		if (idToken) config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
		mapper.setConfig(config);
		return mapper;
	}

	@Override
	public Map<String, String> getOperationalInfo() {
		Map<String, String> ret = new LinkedHashMap<>();
        ret.put("version", "1.0");
        return ret;
	}
}
