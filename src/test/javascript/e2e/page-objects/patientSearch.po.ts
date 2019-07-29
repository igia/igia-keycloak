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
import { element, by, ExpectedConditions as EC } from 'protractor';

export class SearchPage {
  mrn = element(by.name('mrn'));
  fname = element(by.name('first'));
  lname = element(by.name('last'));
  dob = element(by.name('terra-date-birthdate'));
  gender = element(by.css('#select-gender'));
  gender_male_option = element(by.id('terra-select-option-male'));
  gender_female_option = element(by.id('terra-select-option-female'));

  search = element(by.css('[type="submit"]'));

  //TBD: Currently element id is not present. Element id is being added. Change class name post that...
  information = element(by.css('.DDsANBtyMMgiQF4qHjcUL'));

  searchTable = element(by.css('#patient-search-results'));

  //TBD: Class to be changed...
  selectPatientCheckBox = element(by.css('._3vp0qRRxFX_DJWAnDIEjDy'));

  //TBD: Class to be changed...
  selectButton = element(by.cssContainingText('[type="Submit"]', 'Select'));
}
