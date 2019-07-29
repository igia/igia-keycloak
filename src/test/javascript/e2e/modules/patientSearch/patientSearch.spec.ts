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

import { SearchPage } from '../../page-objects/patientSearch.po';
import { PatientDataManagerPage } from '../../page-objects/patientDataManager.po';

describe('patient search', () => {
  const searchPage = new SearchPage();
  const patientDataManagerPage = new PatientDataManagerPage();
  const waitPeriod = 30000;
  before(async () => {});

  beforeEach(async () => {
    browser.waitForAngularEnabled(false);
    await browser.wait(EC.visibilityOf(searchPage.mrn), waitPeriod, 'Took a long time for mrn field to be visible');
  });

  afterEach(async () => {
    await browser.refresh();
  });

  // tslint:disable:no-unused-expression
  it('should check the fields on Search Page', async () => {
    expect(await searchPage.mrn.isPresent()).to.be.true;
    expect(await searchPage.fname.isPresent()).to.be.true;
    expect(await searchPage.lname.isPresent()).to.be.true;
    expect(await searchPage.dob.isPresent()).to.be.true;
    expect(await searchPage.gender.isPresent()).to.be.true;
    expect(await searchPage.search.isPresent()).to.be.true;
  });

  // tslint:disable:no-unused-expression
  it('should search patient as per mrn entered', async () => {
    await searchPage.mrn.sendKeys('test12345');
    await searchPage.search.click();
    await browser.wait(EC.visibilityOf(searchPage.searchTable), waitPeriod, 'Took a long time for search table to be visible');
    expect(await searchPage.searchTable.getText()).contains('test12345');
  });

  // tslint:disable:no-unused-expression
  it('should fail to search patient with invalid mrn', async () => {
    await searchPage.mrn.sendKeys('123');
    await searchPage.search.click();
    await browser.wait(
      EC.textToBePresentInElement(searchPage.information, 'No matching patients found.'),
      waitPeriod,
      'Took a long time for information message to be visible'
    );
    expect(await searchPage.information.getText()).contains('No matching patients found.');
  });

  // tslint:disable:no-unused-expression
  it('should show error when only first name is searched', async () => {
    await searchPage.fname.sendKeys('John');
    await searchPage.search.click();
    await browser.wait(
      EC.textToBePresentInElement(searchPage.information, 'At least one of MRN, birthdate, or first and last name is required.'),
      waitPeriod,
      'Took a long time for error message to be visible'
    );
    expect(await searchPage.information.getText()).contains('At least one of MRN, birthdate, or first and last name is required.');
  });

  // tslint:disable:no-unused-expression
  it('should display a record when searched on first name and last name', async () => {
    await searchPage.fname.sendKeys('John');
    await searchPage.lname.sendKeys('Smith');
    await searchPage.search.click();
    await browser.wait(EC.visibilityOf(searchPage.searchTable), waitPeriod, 'Took a long time for search table to be present');
    expect(await searchPage.searchTable.getText()).contains('John');
  });

  // tslint:disable:no-unused-expression
  it('should display a patient record when searched on birth date', async () => {
    await searchPage.dob.sendKeys('10/10/2000');
    await searchPage.search.click();
    await browser.wait(EC.visibilityOf(searchPage.searchTable), waitPeriod, 'Took a long time for search table to be visible');
    expect(await searchPage.searchTable.getText()).contains('John');
  });

  // tslint:disable:no-unused-expression
  it('should display a patient record when searched on first name, last name and gender', async () => {
    await searchPage.fname.sendKeys('John');
    await searchPage.lname.sendKeys('Smith');
    await searchPage.gender.click();
    await browser.wait(EC.presenceOf(searchPage.gender_male_option), 3000, 'Took a long time for combo-box option to be present');
    await searchPage.gender_male_option.click();
    await searchPage.search.click();
    await browser.wait(EC.visibilityOf(searchPage.searchTable), 3000, 'Took a long time for search table to be visible');
    expect(await searchPage.searchTable.getText()).contains('John');
  });

  // tslint:disable:no-unused-expression
  it('should select the searched record and verify it on Patient Data Manager', async () => {
    await searchPage.mrn.sendKeys('test12345');
    await searchPage.search.click();
    await browser.wait(EC.visibilityOf(searchPage.searchTable), waitPeriod, 'Took a long time for search table to be visible');
    expect(await searchPage.searchTable.getText()).contains('John');
    await browser.wait(EC.visibilityOf(searchPage.selectPatientCheckBox), waitPeriod, 'Took a long time to select the patient');
    await searchPage.selectPatientCheckBox.click();
    await browser.wait(EC.visibilityOf(searchPage.selectButton), waitPeriod, 'Took a long time for select button to be visible');
    await searchPage.selectButton.click();
    browser.wait(
      EC.urlContains('session_state'),
      waitPeriod,
      'Page is not redirected to the smart launch app page after wait of 5 seconds.'
    );

    await browser.wait(
      EC.visibilityOf(patientDataManagerPage.dataManagerPageHeader),
      waitPeriod,
      'Took a long time for Patient Data Manager Page Header to be visible'
    );
    expect(await patientDataManagerPage.dataManagerPageHeader.getText()).to.eq('Patient Data Manager');

    await browser.wait(EC.visibilityOf(patientDataManagerPage.patientName), waitPeriod, 'Took a long time for Patient Name to be visible');
    expect(await patientDataManagerPage.patientName.getText()).contains('John Smith');
  });
});
