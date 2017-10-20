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
package com.cinnober.ciguan.transport.impl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.AsMetaDataFactoryIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.data.AsDataSourceDef;
import com.cinnober.ciguan.data.AsMetaDataData;
import com.cinnober.ciguan.data.AsViewDefinition;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsComponent;
import com.cinnober.ciguan.transport.AsMetaDataCreatorIf;
import com.cinnober.ciguan.xml.impl.AsDefMetaData;

/**
 * Factory to create metadata for use by the client.
 */
public class AsMetaDataFactory extends AsComponent implements AsMetaDataFactoryIf, MvcModelAttributesIf {

    @Override
    public void synchronizeExternalData() {

        Map<String, AsDefMetaData> tMetaDataMap = new LinkedHashMap<String, AsDefMetaData>();
        for (String tType : getTypesFromConfiguration()) {
            if (As.getType(tType) != null) {
                tMetaDataMap.put(tType, new AsDefMetaData(tType));
            }
        }

        // Finally use explicitly configured metadata and let it overwrite its automatically created dito
        for (AsDefMetaData tMetaData : As.getMetaDataHandler().getMetaDataClasses()) {
            tMetaDataMap.put(tMetaData.getClassName(), tMetaData);
        }

        // Create metadata for all configured classes
        AsMetaDataCreatorIf tCreator = As.getBeanFactory().create(AsMetaDataCreatorIf.class);
        for (AsDefMetaData tMetaData : tMetaDataMap.values()) {
            tCreator.create(tMetaData);
        }

        // Finalize and submit the metadata
        tCreator.finalizeMetaData();
        for (Map.Entry<String, String> tEntry : tCreator.getMetaData().entrySet()) {
            AsMetaDataData tData = new AsMetaDataData(tEntry.getKey(), tEntry.getValue());
            As.getBdxHandler().broadcast(tData);
        }
    }

    /**
     * Gets the types from configuration.
     *
     * @return the types from configuration
     */
    private Set<String> getTypesFromConfiguration() {

        Set<String> tMetaSet = new HashSet<String>();
        // Populate from view models
        for (AsViewDefinition tViewDefinition : As.getConfigXmlParser().getViewDefinitions().values()) {
            String tModelClass = tViewDefinition.getModel();
            tMetaSet.add(tModelClass);
            // Add BLOB models too
            for (CwfDataIf tBlobDef : tViewDefinition.getValues().getObjectList(TAG_BLOB)) {
                tMetaSet.add(tBlobDef.getProperty(ATTR_MODEL));
            }
            // Add context object types
            for (CwfDataIf tContextDef : tViewDefinition.getValues().getObjectList(TAG_CONTEXT)) {
                tMetaSet.add(tContextDef.getProperty(ATTR_TYPE));
            }
        }
        // Populate all data source item types (except for map based items)
        for (AsDataSourceDef<?> tDataSourceDef : AsDataSourceDef.getAll()) {
            tMetaSet.add(tDataSourceDef.getType());
        }
        tMetaSet.remove(null);
        tMetaSet.remove("");
        return tMetaSet;
    }

}
