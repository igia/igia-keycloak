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
const os = require('os');

exports.config = {
  allScriptsTimeout: 20000,

  specs: [
    './e2e/modules/login/*.spec.ts',
    './e2e/modules/patientSearch/*.spec.ts',
    /* jhipster-needle-add-protractor-tests - JHipster will add protractors tests here */
  ],

  capabilities: {
    browserName: 'chrome',
    chromeOptions: {
        args: process.env.JHI_E2E_HEADLESS
          ? [ '--headless', '--disable-gpu', '--window-size=800,600' ]
          : [ '--disable-gpu', '--window-size=800,600' ]
    }
  },

  directConnect: true,

  baseUrl: 'http://localhost:8096/launch.html?iss=http://apigtw-app:8088/igiafhirapiexample/api',

  framework: 'mocha',

  SELENIUM_PROMISE_MANAGER: false,

  mochaOpts: {
    reporter: 'spec',
    slow: 3000,
    ui: 'bdd',
    timeout: 30000
  },

  beforeLaunch () {
    require('ts-node').register({
      project: './tsconfig.e2e.json'
    });
  },

  onPrepare () {
    // @ts-ignore
    browser.driver.manage().window().setSize(1280, 1024);
    // @ts-ignore
    browser.ignoreSynchronization = true;
    // Disable animations
    // @ts-ignore
    browser.executeScript('document.body.className += " notransition";');
    const chai = require('chai');
    const chaiAsPromised = require('chai-as-promised');
    chai.use(chaiAsPromised);
    // @ts-ignore
    global.chai = chai;
  }
};
