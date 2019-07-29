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
import { REQUEST, SUCCESS } from 'app/shared/reducers/action-type.util';
import thunk from 'redux-thunk';
import axios from 'axios';
import sinon from 'sinon';
import configureStore from 'redux-mock-store';
import promiseMiddleware from 'redux-promise-middleware';

import profile, { ACTION_TYPES, getProfile } from 'app/shared/reducers/application-profile';

describe('Profile reducer tests', () => {
  const initialState = {
    ribbonEnv: '',
    inProduction: true,
    isSwaggerEnabled: false,
    activeProfiles: []
  };
  describe('Common tests', () => {
    it('should return the initial state', () => {
      const toTest = profile(undefined, {});
      expect(toTest).toEqual(initialState);
    });

    it('should return the right payload in prod', () => {
      const payload = {
        data: {
          'deploy-mode': {
            name: 'local',
            description: 'This instance is deployed on local machine for development'
          },
          activeProfiles: ['prod']
        }
      };

      expect(profile(undefined, { type: SUCCESS(ACTION_TYPES.GET_PROFILE), payload })).toEqual({
        activeProfiles: ['prod'],
        ribbonEnv: 'local',
        inProduction: true,
        isSwaggerEnabled: false
      });
    });

    it('should return the right payload in dev with swagger enabled', () => {
      const payload = {
        data: {
          'deploy-mode': {
            name: 'local',
            description: 'This instance is deployed on local machine for development'
          },
          activeProfiles: ['swagger', 'dev']
        }
      };

      expect(profile(undefined, { type: SUCCESS(ACTION_TYPES.GET_PROFILE), payload })).toEqual({
        activeProfiles: ['swagger', 'dev'],
        ribbonEnv: 'local',
        inProduction: false,
        isSwaggerEnabled: true
      });
    });
  });

  describe('Actions', () => {
    let store;

    const resolvedObject = { value: 'whatever' };
    beforeEach(() => {
      const mockStore = configureStore([thunk, promiseMiddleware()]);
      store = mockStore({});
      axios.get = sinon.stub().returns(Promise.resolve(resolvedObject));
    });

    it('dispatches GET_SESSION_PENDING and GET_SESSION_FULFILLED actions', async () => {
      const expectedActions = [
        {
          type: REQUEST(ACTION_TYPES.GET_PROFILE)
        },
        {
          type: SUCCESS(ACTION_TYPES.GET_PROFILE),
          payload: resolvedObject
        }
      ];
      await store.dispatch(getProfile()).then(() => expect(store.getActions()).toEqual(expectedActions));
    });
  });
});
