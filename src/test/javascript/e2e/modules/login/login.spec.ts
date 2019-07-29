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
import { browser, ExpectedConditions as EC } from 'protractor';
import { expect } from 'chai';
import { SignInPage } from '../../page-objects/sign-in.po';
import { maximizeBrowser } from '../../page-objects/util';

describe('login', () => {
  const signInPage: SignInPage = new SignInPage();
  const waitPeriod = 10000;

  before(async () => {
    maximizeBrowser();
  });

  beforeEach(async () => {
    browser.waitForAngularEnabled(false);
    browser.get('http://localhost:8096/launch.html?iss=http://apigtw-app:8088/igiafhirapiexample/api');
  });

  afterEach(() => {});

  // tslint:disable:no-unused-expression
  it('should fail to login with bad password', async () => {
    browser.wait(EC.urlContains('igia-fhir-api-example'), waitPeriod, 'Login url not present').catch(() => {});
    expect(await browser.getCurrentUrl()).to.contains('igiafhirapiexample');

    await signInPage.login('admin', 'foo');
    browser.wait(signInPage.loginError.isPresent(), waitPeriod, 'Login error not present after wait of 3 seconds.');
    expect(await signInPage.loginError.getText()).to.eq('Invalid username or password.');
  });

  it('should login successfully with admin account', async () => {
    browser.wait(
      EC.urlContains('smart-launch-context'),
      waitPeriod,
      'Page is not redirected to the smart launch app page after wait of 10 seconds.'
    );
    await signInPage.login('admin', 'admin');
  });
});
