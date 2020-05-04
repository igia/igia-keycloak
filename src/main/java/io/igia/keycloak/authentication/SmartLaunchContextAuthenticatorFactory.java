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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

public class SmartLaunchContextAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory, ServerInfoAwareProviderFactory {
	public static final String PROVIDER_ID = "smart-launch-context-authenticator";
	private static final SmartLaunchContextAuthenticator SINGLETON = new SmartLaunchContextAuthenticator();

    public static final String CONFIG_APPLICATION_ID = "application-id";
    public static final String CONFIG_EXTERNAL_SMART_LAUNCH_URL = "external-smart-launch-url";
    public static final String CONFIG_EXTERNAL_SMART_LAUNCH_SECRET_KEY = "external-smart-launch-secret-key";
    public static final String CONFIG_EXTERNAL_SMART_LAUNCH_CLIENT_ID = "external-smart-launch-client-id";
    public static final String CONFIG_EXTERNAL_SMART_LAUNCH_SUPPORTED_PARAMS = "external-smart-launch--supported-params";

    public static final String QUERY_PARAM_APP_TOKEN = "app-token";
    public static final String INITIATED_BY_SMART_LAUNCH_EXT_APP = "INITIATED_BY_SMART_LAUNCH_EXT_APP";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.CONDITIONAL,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty(CONFIG_EXTERNAL_SMART_LAUNCH_URL,
        		"External SMART Launch URL",
        		"External URL to redirect for user launch context selection. The launch URL must include a query parameter with value placeholder \"{TOKEN}\".",
        		ProviderConfigProperty.STRING_TYPE, null);
        configProperties.add(property);
        property = new ProviderConfigProperty(CONFIG_EXTERNAL_SMART_LAUNCH_SECRET_KEY,
        		"External SMART Launch Secret Key", "HmacSHA256 secret key for smart launch external application.",
        		ProviderConfigProperty.STRING_TYPE, null);
        configProperties.add(property);
        property = new ProviderConfigProperty(CONFIG_EXTERNAL_SMART_LAUNCH_CLIENT_ID,
        		"External SMART Launch Client Id", "Client Id for smart launch external application.",
        		ProviderConfigProperty.STRING_TYPE, null);
        configProperties.add(property);
        property = new ProviderConfigProperty(CONFIG_EXTERNAL_SMART_LAUNCH_SUPPORTED_PARAMS,
        		"External SMART Launch Supported Params", "Space separated list of Smart launch context parameters supported by external application.",
        		ProviderConfigProperty.STRING_TYPE, null);
        configProperties.add(property);
    }

    @Override
    public String getHelpText() {
        return "Context for Smart application launch.";
    }

    @Override
    public String getDisplayType() {
        return "Smart Launch";
    }

    @Override
    public String getReferenceCategory() {
        return "Smart Launch";
    }

    @Override
    public void init(Config.Scope config) {
    	// no required actions
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    	// no required actions
    }

    @Override
    public void close() {
    	// no required actions
    }

	@Override
	public Map<String, String> getOperationalInfo() {
		Map<String, String> ret = new LinkedHashMap<>();
        ret.put("version", "1.0");
        return ret;
	}
}
