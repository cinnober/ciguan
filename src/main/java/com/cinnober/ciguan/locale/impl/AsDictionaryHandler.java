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
package com.cinnober.ciguan.locale.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.AsDictionaryHandlerIf;
import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.data.AsDictionaryLanguage;
import com.cinnober.ciguan.data.AsDictionaryTranslations;
import com.cinnober.ciguan.data.AsDictionaryWord;
import com.cinnober.ciguan.datasource.owner.AsGlobalDataSources;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsComponent;
import com.cinnober.ciguan.impl.AsUtil;

/**
 *
 *
 */
public class AsDictionaryHandler extends AsComponent implements AsDictionaryHandlerIf, MvcModelAttributesIf {

    private static final String DELIMITER = ";";

    private Map<String, AsDictionaryWord> mWords = new HashMap<String, AsDictionaryWord>();
    private Map<String, AsDictionaryLanguage> mLanguages = new LinkedHashMap<String, AsDictionaryLanguage>();
    private AsDictionaryLanguage mDefaultLanguage;

    private String[] mCurrentLanguages;

    @Override
    public void startComponent() throws AsInitializationException {
        parse(As.getConfigXmlParser().getConfigurationDocument());
    }

    @Override
    public void allComponentsStarted() throws AsInitializationException {
        submitDictionaryConfiguration();
    }

    protected void parse(Element pNode) {
        try {
            NodeList tNodeList = XPathAPI.selectNodeList(pNode, "//" + TAG_AS_DICTIONARY);
            for (int i = 0; i < tNodeList.getLength(); i++) {
                Node tNode = tNodeList.item(i);
                CwfDataIf tTree = AsUtil.parse((Element) tNode);
                String tPath = tTree.getProperty(ATTR_PATH);
                parse(tPath);
            }
        }
        catch (TransformerException e) {
            throw new RuntimeException("Error selecting plugin source node", e);
        }
    }

    protected void parse(String pPath) {
        try {
            char[] tBytes = new char[1024];
            StringBuilder tBuffer = new StringBuilder();
            InputStream tStream = getClass().getResourceAsStream(pPath);
            if (tStream == null) {
                tStream = ClassLoader.getSystemResourceAsStream(pPath);
            }
            if (tStream == null) {
                tStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(pPath);
            }
            InputStreamReader tReader = new InputStreamReader(tStream, "UTF-8");
            int tCount = tReader.read(tBytes);
            while (tCount > 0) {
                tBuffer.append(new String(tBytes, 0, tCount));
                tCount = tReader.read(tBytes);
            }

            String[] tArr = tBuffer.toString().split("\n");

            createLanguages(tArr[0]);
            for (int i = 1; i < tArr.length; i++) {
                if (tArr[i].indexOf(DELIMITER) > 0) {
                    createWord(tArr[i].split(DELIMITER));
                }
            }
            tStream.close();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse dictionary: " + pPath, e);
        }
    }

    /**
     * Submit the dictionary configuration
     */
    protected void submitDictionaryConfiguration() {
        for (AsDictionaryLanguage tLanguage : mLanguages.values()) {
            StringBuilder tBuffer = new StringBuilder();
            for (AsDictionaryWord tWord : mWords.values()) {
                tBuffer.append(tBuffer.length() > 0 ? "|" : "");
                tBuffer.append(tWord.key);
                tBuffer.append(";");
                tBuffer.append(tWord.getText(tLanguage.getLocale()));
            }
            AsDictionaryTranslations tTranslations =
                new AsDictionaryTranslations(tLanguage.getText(), tBuffer.toString());
            As.getBdxHandler().broadcast(tTranslations);
        }

        for (AsDictionaryWord tData : mWords.values()) {
            As.getBdxHandler().broadcast(tData);
        }
        for (AsDictionaryLanguage tData : mLanguages.values()) {
            As.getBdxHandler().broadcast(tData);
        }
    }

    @Override
    public void reloadConfiguration() {
        // Empty the configuration related data sources
        AsGlobalDataSources tDs = As.getGlobalDataSources();
        tDs.getDataSource(AsDictionaryTranslations.class).clear();
        tDs.getDataSource(AsDictionaryWord.class).clear();
        tDs.getDataSource(AsDictionaryLanguage.class).clear();

        mWords.clear();
        mLanguages.clear();
        mDefaultLanguage = null;

        parse(As.getConfigXmlParser().getConfigurationDocument());
        submitDictionaryConfiguration();
    }

    @Override
    public Collection<AsDictionaryWord> getDictionaryWords() {
        return mWords.values();
    }

    @Override
    public Collection<AsDictionaryLanguage> getDictionaryLanguages() {
        return mLanguages.values();
    }

    @Override
    public AsDictionaryLanguage getDefaultLanguage() {
        return mDefaultLanguage;
    }

    @Override
    public AsDictionaryLanguage getDictionaryLanguage(String pLanguageCode) {
        return mLanguages.get(pLanguageCode);
    }

    @Override
    public String getTranslation(String pKey, Locale pLocale) {
        AsDictionaryWord tWord = mWords.get(pKey);
        return tWord == null ? pKey : tWord.getText(pLocale);
    }

    private void createLanguages(String pRow) {
        pRow = pRow.trim();
        mCurrentLanguages = pRow.split(DELIMITER);
        for (int i = 1; i < mCurrentLanguages.length; i++) {
            String tLanguageCode = mCurrentLanguages[i].trim();
            if (!mLanguages.containsKey(tLanguageCode)) {
                AsDictionaryLanguage tLanguage = new AsDictionaryLanguage(tLanguageCode);
                mLanguages.put(tLanguageCode, tLanguage);
                if (mDefaultLanguage == null) {
                    mDefaultLanguage = tLanguage;
                }
            }
        }
    }

    private void createWord(String[] pText) {
        String tKey = pText[0].trim();
        AsDictionaryWord tWord = mWords.get(tKey);
        if (tWord == null) {
            tWord = new AsDictionaryWord(tKey);
            mWords.put(tKey, tWord);
        }
        for (int i = 1; i < Math.min(pText.length, mCurrentLanguages.length); i++) {
            String tLanguageCode = mCurrentLanguages[i].trim();
            String tText = pText[i].trim();
            // Allow empty translation only for the first language
            if (!tText.isEmpty() || i == 1) {
                tWord.addTranslation(tLanguageCode, tText);
            }
        }
    }

}
