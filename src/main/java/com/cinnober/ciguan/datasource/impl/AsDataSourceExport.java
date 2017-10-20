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
package com.cinnober.ciguan.datasource.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsFormatIf;
import com.cinnober.ciguan.data.AsDictionaryWord;
import com.cinnober.ciguan.datasource.AsDataSourceExportIf;
import com.cinnober.ciguan.datasource.AsDataSourceViewportListenerIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.CwfGlobalDataSources;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.CwfBusinessTypes;

/**
 * Standard implementation of the data source export interface
 * Normally created via the bean factory.
 */
public class AsDataSourceExport implements AsDataSourceExportIf {

    @Override
    public <T> String[][] export(AsConnectionIf pConnection, String pViewId,
        AsDataSourceViewportListenerIf<T> pListener) {

        AsListIf<AsDictionaryWord> tDictionary = (AsListIf<AsDictionaryWord>) As.getGlobalDataSources().getDataSource(
            CwfGlobalDataSources.DICTIONARY, (AsFilterIf<AsDictionaryWord>) null, null);

        AsGetMethodIf<T>[] tGetMethods = pListener.getGetMethods();
        List<T> tItems = new ArrayList<T>(((AsListIf<T>) pListener.getDataSource()).values());
        String[][] tExport = new String[tItems.size() + 1][tGetMethods.length];
        for (int i = 0; i < tGetMethods.length; i++) {
            String tFieldName = tGetMethods[i].getAttributeName();
            String tShortFieldName = tFieldName.substring(1 + tFieldName.lastIndexOf('.'));
            AsDictionaryWord tWord = null;
            String[] tSearchKeys = new String[] {
                pViewId + ".field." + tFieldName,
                pViewId + ".field." + tShortFieldName,
                ".field." + tFieldName,
                ".field." + tShortFieldName
            };
            for (int j = 0; j < tSearchKeys.length && tWord == null; j++) {
                tWord = tDictionary.get(tSearchKeys[j]);
            }
            if (tWord != null) {
                tFieldName = tDictionary.getText(tWord, pConnection.getDataSourceService());
            }
            tExport[0][i] = tFieldName;
        }
        int tRow = 1;
        for (T tItem : tItems) {
            for (int i = 0; i < tGetMethods.length; i++) {
                Object tObject = tGetMethods[i].getObject(tItem);
                if (tObject == null) {
                    tExport[tRow][i] = "";
                    continue;
                }
                if (tGetMethods[i].getBusinessType() == CwfBusinessTypes.Text
                    || tGetMethods[i].getBusinessType() == CwfBusinessTypes.MultiLineText
                    || tGetMethods[i].getBusinessType() == CwfBusinessTypes.Password
                    || tGetMethods[i].getBusinessType() == CwfBusinessTypes.Url
                    || tGetMethods[i].getBusinessType() == CwfBusinessTypes.Date
                    || tGetMethods[i].getBusinessType() == CwfBusinessTypes.SecondsSinceTime
                    || tGetMethods[i].getBusinessType() == CwfBusinessTypes.Time
                    || tGetMethods[i].getBusinessType() == CwfBusinessTypes.Constant
                    || tGetMethods[i].getBusinessType() == CwfBusinessTypes.Enum) {
                    tExport[tRow][i] = "\"" + tGetMethods[i].getText(tItem, pConnection.getDataSourceService()) + "\"";
                    continue;
                }
                if (tGetMethods[i].getBusinessType() == CwfBusinessTypes.DateTime) {
                    String tText = tGetMethods[i].getText(tItem, pConnection.getDataSourceService());
                    tExport[tRow][i] = "\"" + (tText != null ? tText.replace('T', ' ') : "") + "\"";
                    continue;
                }
                if (tGetMethods[i].getBusinessType() == CwfBusinessTypes.Boolean) {
                    Boolean tValue = (Boolean) tObject;
                    tExport[tRow][i] = tValue != null ? Boolean.toString(tValue.booleanValue()) : "";
                    continue;
                }
                if (tObject instanceof BigInteger) {
                    BigDecimal tDivide = new BigDecimal((BigInteger) tObject).divide(
                        new BigDecimal(AsFormatIf.Singleton.get().getDivisor(tGetMethods[i].getBusinessType())));
                    tExport[tRow][i] = tDivide.toPlainString();
                    continue;
                }
                BigDecimal tDivide = new BigDecimal(((Number) tObject).longValue()).divide(
                    new BigDecimal(AsFormatIf.Singleton.get().getDivisor(tGetMethods[i].getBusinessType())));
                tExport[tRow][i] = tDivide.toPlainString();
            }
            tRow++;
        }
        return tExport;
    }

}
