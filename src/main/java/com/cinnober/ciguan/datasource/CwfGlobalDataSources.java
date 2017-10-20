/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Cinnober Financial Technology AB (cinnober.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cinnober.ciguan.datasource;

/**
 * Class containing global data source identifiers
 */
public interface CwfGlobalDataSources {

    String CONTEXTMENUS_ALL = "CONTEXTMENUS_ALL";
    String DATASOURCE_DEF = "DATASOURCE_DEF";
    String MEMBERS_ALL = "MEMBERS_ALL";
    String MEMBER_UNITS_ALL = "MEMBER_UNITS_ALL";
    String MENUITEMS_ALL = "MENUITEMS_ALL";
    String METADATA_ALL = "METADATA_ALL";
    String MVC_VIEWDEFS_ALL = "MVC_VIEWDEFS_ALL";
    String MVC_DISPLAYDEFS_ALL = "MVC_DISPLAYDEFS_ALL";
    String DICTIONARY = "DICTIONARY";
    String LANGUAGE_ALL = "LANGUAGE_ALL";
    String PERSPECTIVES_ALL = "PERSPECTIVES_ALL";

    /* filtered datasources (member) */

    String PUBLIC_MEMBERS_ALL = "PUBLIC_MEMBERS_ALL";
    String PUBLIC_MEMBERS_CLEARER_ALL = "PUBLIC_MEMBERS_CLEARER_ALL";
    String PUBLIC_MEMBERS_TRADING_ALL = "PUBLIC_MEMBERS_TRADING_ALL";

    /* filtered datasources (user) */

    String ALERTS_ALL = "ALERTS_ALL";
    String MARKET_MESSAGES_ALL = "MARKET_MESSAGES_ALL";
    String USERS_ALL_LOGGED_IN = "USERS_ALL_LOGGED_IN";
    String REQUESTS_ALL = "REQUESTS_ALL";

}
