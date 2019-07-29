<#--

    This Source Code Form is subject to the terms of the Mozilla Public License, v.
    2.0 with a Healthcare Disclaimer.
    A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
    be found under the top level directory, named LICENSE.
    If a copy of the MPL was not distributed with this file, You can obtain one at
    http://mozilla.org/MPL/2.0/.
    If a copy of the Healthcare Disclaimer was not distributed with this file, You
    can obtain one at the project website https://github.com/igia.

    Copyright (C) 2018-2019 Persistent Systems, Inc.

-->
<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        ${msg("loginTitleHtml",realm.name)}
    <#elseif section = "form">
        <form id="kc-totp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="totp" class="${properties.kcLabelClass!}">Patient identifier for SMART application</label>
                </div>

                <div class="${properties.kcInputWrapperClass!}">
                    <input id="totp" name="patient" type="text" class="${properties.kcInputClass!}" />
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <div class="${properties.kcFormButtonsWrapperClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}" name="cancel" id="kc-cancel" type="submit" value="${msg("doCancel")}"/>
                    </div>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>