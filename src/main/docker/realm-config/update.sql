--
-- This Source Code Form is subject to the terms of the Mozilla Public License, v.
-- 2.0 with a Healthcare Disclaimer.
-- A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
-- be found under the top level directory, named LICENSE.
-- If a copy of the MPL was not distributed with this file, You can obtain one at
-- http://mozilla.org/MPL/2.0/.
-- If a copy of the Healthcare Disclaimer was not distributed with this file, You
-- can obtain one at the project website https://github.com/igia.
--
-- Copyright (C) 2018-2019 Persistent Systems, Inc.
--

INSERT INTO client_auth_flow_bindings (CLIENT_ID, FLOW_ID, BINDING_NAME)
SELECT 'ed424acd-36ce-433e-bf59-f1f3143faf6f', (select id from authentication_flow where alias='SMART browser'), 'browser'
where (select count(*) from client_auth_flow_bindings where CLIENT_ID='ed424acd-36ce-433e-bf59-f1f3143faf6f' and BINDING_NAME='browser') = 0;
