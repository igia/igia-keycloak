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
import { browser, by, ElementFinder } from 'protractor';

import { SignInPage } from '../page-objects/sign-in.po';

export const loginUser = async (username: string, password: string) => {
  browser.waitForAngularEnabled(false);
  await browser.get('/');

  await new SignInPage().autoLogin(username, password);
  browser.waitForAngularEnabled(true);
};

export const maximizeBrowser = () => {
  browser.driver
    .manage()
    .window()
    .maximize();
};

export const getTableRowByColumnText = async (table: ElementFinder, colText: string): Promise<ElementFinder[]> => {
  return await table.all(by.css('tbody tr')).filter(async (ef: ElementFinder) => {
    const cols = await ef.all(by.tagName('td')).filter(async (cef: ElementFinder) => (await cef.getText()).startsWith(colText));
    return cols.length !== 0;
  });
};

export const getTableRowsCount = async (table: ElementFinder): Promise<number> => {
  return await table.all(by.css('tbody tr')).count();
};

export const getMatchedTableRowsCount = async (table: ElementFinder, colText: string): Promise<number> => {
  return (await this.getTableRowByColumnText(table, colText)).length;
};

export const isDropdownOptionPresent = async (dropdown: ElementFinder, name: string): Promise<boolean> => {
  return (await dropdown.all(by.cssContainingText('option', name)).count()) > 0;
};

export const getDropdownOptionsCount = async (dropdown: ElementFinder): Promise<number> => {
  return await dropdown.all(by.tagName('option')).count();
};

export const selectDropdownOption = async (dropdown: ElementFinder, name: string) => {
  return await dropdown
    .all(by.cssContainingText('option', name))
    .first()
    .click();
};

export const getSelectedDropdownOption = async (dropdown: ElementFinder): Promise<string> => {
  return await dropdown.element(by.css('option:checked')).getText();
};
