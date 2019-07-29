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
import axios from 'axios';
import sinon from 'sinon';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import { TranslatorContext } from 'react-jhipster';

import locale, { setLocale, ACTION_TYPES } from 'app/shared/reducers/locale';

describe('Locale reducer tests', () => {
  it('should return the initial state', () => {
    const localeState = locale(undefined, {});
    expect(localeState).toMatchObject({
      currentLocale: undefined
    });
  });

  it('should correctly set the first time locale', () => {
    const localeState = locale(undefined, { type: ACTION_TYPES.SET_LOCALE, locale: 'en' });
    expect(localeState).toMatchObject({
      currentLocale: 'en'
    });
    expect(TranslatorContext.context.locale).toEqual('en');
  });

  it('should correctly detect update in current locale state', () => {
    TranslatorContext.setLocale('en');
    expect(TranslatorContext.context.locale).toEqual('en');
    const localeState = locale({ currentLocale: 'en' }, { type: ACTION_TYPES.SET_LOCALE, locale: 'es' });
    expect(localeState).toMatchObject({
      currentLocale: 'es'
    });
    expect(TranslatorContext.context.locale).toEqual('es');
  });

  describe('Locale actions', () => {
    let store;
    beforeEach(() => {
      store = configureStore([thunk])({});
      axios.get = sinon.stub().returns(Promise.resolve({ key: 'value' }));
    });

    it('dispatches SET_LOCALE action for default locale', async () => {
      TranslatorContext.setDefaultLocale('en');
      const defaultLocale = TranslatorContext.context.defaultLocale;
      expect(Object.keys(TranslatorContext.context.translations)).not.toContainEqual(defaultLocale);

      const expectedActions = [
        {
          type: ACTION_TYPES.SET_LOCALE,
          locale: defaultLocale
        }
      ];

      await store.dispatch(setLocale(defaultLocale)).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
        expect(TranslatorContext.context.translations).toBeDefined();
        expect(Object.keys(TranslatorContext.context.translations)).toContainEqual(defaultLocale);
      });
    });
  });
});
