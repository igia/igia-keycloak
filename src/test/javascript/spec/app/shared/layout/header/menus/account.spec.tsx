/*
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
import React from 'react';
import { shallow } from 'enzyme';

import { getLoginUrl } from 'app/shared/util/url-utils';
import { NavDropdown } from 'app/shared/layout/header/header-components';
import { AccountMenu } from 'app/shared/layout/header/menus';

describe('AccountMenu', () => {
  let mountedWrapper;

  const authenticatedWrapper = () => {
    if (!mountedWrapper) {
      mountedWrapper = shallow(<AccountMenu isAuthenticated />);
    }
    return mountedWrapper;
  };
  const guestWrapper = () => {
    if (!mountedWrapper) {
      mountedWrapper = shallow(<AccountMenu />);
    }
    return mountedWrapper;
  };

  beforeEach(() => {
    mountedWrapper = undefined;
  });

  // All tests will go here

  it('Renders a authenticated AccountMenu component', () => {
    const dropdown = authenticatedWrapper().find(NavDropdown);
    expect(dropdown).toHaveLength(1);
    expect(dropdown.find({ to: '/login' })).toHaveLength(0);
    expect(dropdown.find({ to: '/logout' })).toHaveLength(1);
  });

  it('Renders a guest AccountMenu component', () => {
    const dropdown = guestWrapper().find(NavDropdown);
    expect(dropdown).toHaveLength(1);
    expect(dropdown.find({ href: getLoginUrl() })).toHaveLength(1);
    expect(dropdown.find({ to: '/logout' })).toHaveLength(0);
  });
});
