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
package com.cinnober.ciguan.client.impl;

import com.cinnober.ciguan.CwfModelNameIf;

/**
 * Enumeration with all known model names
 */
public enum MvcModelNames implements CwfModelNameIf {

    // Client side models
    AboutModel,
    AbstractModel,
    AbstractFormModel,
    AlertModel,
    ChartModel,
    ColumnModel,
    ConfirmModel,
    DataSubscriptionModel,
    EditableViewportModel,
    EditFilterModel,
    ErrorModel,
    FormResultModel,
    GridModel,
    IframeModel,
    InfoModel,
    ListModel,
    LoginFormModel,
    ManageColumnsModel,
    MenuModel,
    MenuParameters,
    NoModel,
    PerspectiveGroupModel,
    PerspectiveModel,
    PlaceholderModel,
    ScrollModel,
    SessionModel,
    ViewportEditorModel,
    ViewportModel,
    ViewportHandleMappingModel,
    QuickFilterModel,
    UrlParametersModel,
    
    // Client to server (requests)
    // TODO: Should these really be here?
    FileUploadResult,
    LogEntry,
    SessionRequest,
    ServerRequest,
    ServerResponse,

    // Special
    RemoveContext,
    SelectLocaleResponse,
    SelectPerspectiveResponse,
    TypeAheadFilter
    
}
