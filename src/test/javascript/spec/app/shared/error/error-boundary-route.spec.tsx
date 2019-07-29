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
import { Route } from 'react-router-dom';
import { shallow, mount } from 'enzyme';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

const ErrorComp = () => {
  throw new Error('test');
};

describe('error-boundary-route component', () => {
  beforeEach(() => {
    // ignore console and jsdom errors
    jest.spyOn((window as any)._virtualConsole, 'emit').mockImplementation(() => false);
    jest.spyOn((window as any).console, 'error').mockImplementation(() => false);
  });

  // All tests will go here
  it('Should throw error when no component is provided', () => {
    expect(() => shallow(<ErrorBoundaryRoute />)).toThrow(Error);
  });

  it('Should render fallback component when an uncaught error is thrown from component', () => {
    const route = shallow(<ErrorBoundaryRoute component={ErrorComp} path="/" />);
    const renderedRoute = route.find(Route);
    expect(renderedRoute.length).toEqual(1);
    expect(renderedRoute.props().path).toEqual('/');
    expect(renderedRoute.props().render).toBeDefined();
    const renderFn: Function = renderedRoute.props().render;
    const comp = mount(
      renderFn({
        location: '/'
      })
    );
    expect(comp.length).toEqual(1);
    expect(comp.html()).toEqual('<div><h2 class="error">An unexpected error has occurred.</h2></div>');
  });
});
