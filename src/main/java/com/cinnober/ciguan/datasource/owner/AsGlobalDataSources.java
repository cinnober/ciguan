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
package com.cinnober.ciguan.datasource.owner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.AsBdxListenerIf;
import com.cinnober.ciguan.AsBdxMapperIf;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.data.AsDataSourceDef;
import com.cinnober.ciguan.data.AsDictionaryWord;
import com.cinnober.ciguan.datasource.AsDataSourceFactoryIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.CwfGlobalDataSources;
import com.cinnober.ciguan.datasource.filter.AsRefDataFilter;
import com.cinnober.ciguan.datasource.impl.AsEmapiTreeMapList;
import com.cinnober.ciguan.datasource.impl.AsMapRefData;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;
import com.cinnober.ciguan.datasource.listtree.AsListTreeDefinition;
import com.cinnober.ciguan.datasource.listtree.AsListTreeRoot;
import com.cinnober.ciguan.datasource.tree.AsTreeData;
import com.cinnober.ciguan.datasource.tree.AsTreeRoot;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsRefDataObject;
import com.cinnober.ciguan.locale.impl.AsConstantGroupValueData;
import com.cinnober.ciguan.locale.impl.AsEnumValueData;

/**
 * Class holding application server global data sources.
 */
@SuppressWarnings("deprecation")
public class AsGlobalDataSources extends AsDataSources implements MvcModelAttributesIf {

    /** The Constant DATASOURCE_DEF. */
    public static final String DATASOURCE_DEF = "DATASOURCE_DEF";

    /** The Bdx listener. */
    private final BdxListener mBdxListener = new BdxListener();

    /** The Class to list mapping. */
    private final HashMap<Class<?>, AsListIf<?>> mClassToListMapping = new HashMap<Class<?>, AsListIf<?>>();

    /** The Listeners. */
    private final List<ConstantGroupListenerIf> mListeners = new ArrayList<ConstantGroupListenerIf>();

    /**
     * Interface for constant group creation callback handling.
     */
    public interface ConstantGroupListenerIf {

        /**
         * Called when a constant group list is created.
         *
         * @param pList the list
         */
        void onListCreated(AsListIf<?> pList);
    }

    /**
     * Default instance.
     */
    public AsGlobalDataSources() {
        // need to create this list before all other!
        createList(DATASOURCE_DEF, AsDataSourceDef.class, ATTR_ID, ATTR_ID, null, true);
    }

    /**
     * Adds the constant group listener.
     *
     * @param pListener the listener
     */
    public void addConstantGroupListener(ConstantGroupListenerIf pListener) {
        mListeners.add(pListener);
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * Creates the constant group list.
     *
     * @param pConstantGroupClass the constant group class
     */
    public void createConstantGroupList(Class<?> pConstantGroupClass) {
        // If the data source already exists, do nothing
        AsListIf<?> tDictionary = (AsListIf<?>) getDataSource(CwfGlobalDataSources.DICTIONARY, null, null);
        if (tDictionary == null) {
            AsLoggerIf.Singleton.get().log("Attempting to register constant group " + pConstantGroupClass +
                " before the dictionary exists");
            return;
        }

        if (getDataSource(As.getTypeName(pConstantGroupClass), null, null) != null) {
            return;
        }

        AsEmapiTreeMapList<AsConstantGroupValueData> tList = new AsEmapiTreeMapList<AsConstantGroupValueData>(
            As.getTypeName(pConstantGroupClass), AsConstantGroupValueData.class,
            ATTR_CONSTANT_VALUE, ATTR_CONSTANT_NAME);

        try {
            for (Field tField : pConstantGroupClass.getFields()) {
                if (Modifier.isStatic(tField.getModifiers())) {
                    String tValue = tField.get(null).toString();
                    String tName = tField.getName();
                    // Remove constants with a name containing DO_NOT_USE
                    if (tName.toUpperCase().indexOf("DO_NOT_USE") < 0) {
                        AsDictionaryWord tTranslation = (AsDictionaryWord) tDictionary.get(
                            ".constant." + As.getTypeName(pConstantGroupClass) + "." + tName);
                        if (tTranslation == null) {
                            tTranslation = (AsDictionaryWord) tDictionary.get(".constant." + tName);
                        }
                        AsConstantGroupValueData tCgData = new AsConstantGroupValueData(
                            As.getTypeName(pConstantGroupClass), tName, tValue, tTranslation);
                        tList.add(tCgData);
                    }
                }
            }
        }
        catch (Exception e) {
            // Fatal, abort
            As.systemExit("Could not create constant group list", e);
        }
        putDataSource(tList);
        for (ConstantGroupListenerIf tListener : mListeners) {
            tListener.onListCreated(tList);
        }
    }

    /**
     * Creates the enum list.
     *
     * @param pEnumClass the enum class
     */
    public void createEnumList(Class<?> pEnumClass) {
        // If the data source already exists, do nothing
        AsListIf<?> tDictionary = (AsListIf<?>) getDataSource(CwfGlobalDataSources.DICTIONARY, null, null);
        if (tDictionary == null) {
            AsLoggerIf.Singleton.get().log("Attempting to register enum " + pEnumClass +
                " before the dictionary exists");
            return;
        }

        if (getDataSource(As.getTypeName(pEnumClass), null, null) != null) {
            return;
        }

        AsEmapiTreeMapList<AsEnumValueData> tList = new AsEmapiTreeMapList<AsEnumValueData>(
            As.getTypeName(pEnumClass), AsEnumValueData.class, ATTR_ENUM_NAME, ATTR_ENUM_VALUE);

        try {
            for (Field tField : pEnumClass.getFields()) {
                if (tField.isEnumConstant()) {
                    String tValue = tField.get(null).toString();
                    String tName = tField.getName();
                    AsDictionaryWord tTranslation = (AsDictionaryWord) tDictionary.get(
                        ".enum." + As.getTypeName(pEnumClass) + "." + tName);
                    if (tTranslation == null) {
                        tTranslation = (AsDictionaryWord) tDictionary.get(".enum." + tName);
                    }
                    AsEnumValueData tEnumData = new AsEnumValueData(
                        As.getTypeName(pEnumClass), tName, tValue, tTranslation);
                    tList.add(tEnumData);
                }
            }
        }
        catch (Exception e) {
            // Fatal, abort
            As.systemExit("Could not create constant group list", e);
        }

        putDataSource(tList);
    }

    /**
     * Creates the list.
     *
     * @param <T> the type
     * @param pListId the list id
     * @param pClass the class
     * @param pKey the key
     * @param pText the text
     * @param pFilter the filter
     * @param pMapClass Map the class in the broadcast listener. Query based data sources should not
     * be mapped
     * @return the newly created list
     */
    protected <T> AsListIf<T> createList(String pListId, Class<T> pClass,
        String pKey, String pText, AsFilterIf<T> pFilter, boolean pMapClass) {
        AsEmapiTreeMapList<T> tList = new AsEmapiTreeMapList<T>(pListId, pClass, pKey, pText, pFilter);

        registerList(tList);
        // If the class is a reference data object, add a default filter used for add/update/delete
        // purposes since the reference data messages all have this indicated through an attribute
        if (pMapClass) {
            mBdxListener.map(pClass, tList);
        }
        As.getBdxHandler().addBdxListener(mBdxListener);
        return tList;
    }

    /**
     * Register list.
     *
     * @param <T> the type
     * @param pList the list
     */
    @SuppressWarnings("unchecked")
    protected <T> void registerList(AsEmapiTreeMapList<T> pList) {
        if (AsRefDataObject.class.isAssignableFrom(pList.getItemClass()) && pList.getFilter() == null) {
            AsRefDataFilter tFilter = new AsRefDataFilter();
            pList.setBaseFilter((AsFilterIf<T>) tFilter);
            putDataSourceAs(pList, pList.getDataSourceId());
        }
        else {
            putDataSource(pList);
        }
    }

    /**
     * Creates the xml list.
     *
     * @param pName the name
     * @param pKey the key
     * @param pText the text
     * @param pTagName the tag name
     */
    protected void createXmlList(String pName, String pKey, String pText, String pTagName) {
        AsEmapiTreeMapList<AsXmlRefData> tList = new AsEmapiTreeMapList<AsXmlRefData>(
            pName, AsXmlRefData.class, pKey, pText);
        putDataSource(tList);
        mBdxListener.map(pTagName, tList);
    }

    /**
     * Creates the filtered list.
     *
     * @param <T> the type
     * @param pListId the list id
     * @param pSourceList the source list
     * @param pText
     * @param pFilter the filter
     */
    protected <T> void createFilteredList(String pListId, String pSourceList, String pText, AsFilterIf<T> pFilter) {
        AsDataSourceIf<T> tOriginalDataSource = getDataSource(pSourceList, null, null);
        AsEmapiTreeMapList<T> tOriginalList = (AsEmapiTreeMapList<T>) tOriginalDataSource;
        // Always create a new child list regardless of whether the filter is null or not,
        // to ensure that the list instance ID is always the same as the data source ID
        // in the definition. This will create more sub-lists, but at the same time it ensures
        // that you never inadvertently get the wrong list if you get a list using its own ID.
        AsEmapiTreeMapList<T> tFilteredList = new AsEmapiTreeMapList<T>(
            pListId, tOriginalList, pText, pFilter, null);
        putDataSourceAs(tFilteredList, pListId);
    }

    /**
     * Class listening to the data source definition data source. When items are placed on the
     * definition data source, a data source will be created and added. This allows "on the fly"
     * created data sources.
     *
     * @param <T> the type
     * @param pDef the def
     */
    protected <T> void handleDatasourceDefinition(AsDataSourceDef<T> pDef) {

        if (pDef.getId().equals(DATASOURCE_DEF)) {
            return; // already created
        }
        // Factory lists
        AsDataSourceFactoryIf<?> tFactory = pDef.getFactoryImpl();
        if (tFactory != null) {
            AsListIf<?> tList = tFactory.createGlobalList(pDef.getId());
            if (tList != null) {
                putDataSource(tList);
                if (tFactory.isRootList()) {
                    mBdxListener.map(tList.getItemClass(), (AsEmapiTreeMapList<?>) tList);
                }
            }
            return;
        }

        // Filtered lists with a source
        AsFilterIf<T> tFilter = pDef.getFilter();
        String tId = pDef.getId();
        if (pDef.getSource().length() > 0) {
            createFilteredList(tId, pDef.getSource(), pDef.getTextField(), tFilter);
            return;
        }

        // create list without filter
        Class<T> tItemClass = pDef.getItemClass();
        if (tItemClass != null) {
            String tKeyField = pDef.getKeyField();
            String tTextField = pDef.getTextField();
            createList(tId, tItemClass, tKeyField, tTextField, tFilter, !pDef.isQueryDataSource());
        }
        else {
            System.out.println("Ignoring data source definition: " + pDef.getId());
        }
    }

    /**
     * Class listening for broadcasts. When a broadcast arrives, the appropriate data source is looked up
     * through the class of the arriving message and the message is pushed into the data source.
     */
    protected class BdxListener implements AsBdxListenerIf {

        /** The Class to list map. */
        private Map<Class<?>, Set<AsEmapiTreeMapList<?>>> mClassToListMap =
            new HashMap<Class<?>, Set<AsEmapiTreeMapList<?>>>();

        /** The Tag name to list map. */
        private Map<String, AsEmapiTreeMapList<?>> mTagNameToListMap =
            new HashMap<String, AsEmapiTreeMapList<?>>();

        /** The Bdx mapper. */
        private AsBdxMapperIf mBdxMapper = As.getBeanFactory().create(AsBdxMapperIf.class);

        // TODO: This map is created to handle sub/super class lists where the superclass lists
        // are defined after the subclass lists. After parsing, the map will contain
        // entries since it's not easy to know when all data source definitions are processed.
        // This is probably acceptable, at least for now.
        /** The Pending super classes. */
        private Map<Class<?>, Set<Set<AsEmapiTreeMapList<?>>>> mPendingSuperClasses =
            new HashMap<Class<?>, Set<Set<AsEmapiTreeMapList<?>>>>();

        /**
         * Instantiates a new bdx listener.
         */
        BdxListener() {
            As.getBdxHandler().addBdxListener(this);
        }

        /**
         * Maps the tag name to the specified list.
         *
         * @param pTagName the tag name
         * @param pList the list
         */
        protected void map(String pTagName, AsEmapiTreeMapList<?> pList) {
            if (mTagNameToListMap.put(pTagName, pList) != null) {
                throw new RuntimeException("Duplicate global data source, tagName=" + pTagName);
            }
        }

        /**
         * Maps the tag name to the specified list.
         *
         * @param pClass the class
         * @param pList the list
         */
        protected void map(Class<?> pClass, AsEmapiTreeMapList<?> pList) {
            Set<AsEmapiTreeMapList<?>> tLists = mClassToListMap.get(pClass);
            if (tLists == null) {
                tLists = new HashSet<AsEmapiTreeMapList<?>>();
                mClassToListMap.put(pClass, tLists);
            }
            if (tLists.contains(pList)) {
                throw new RuntimeException("Duplicate global data source " + pClass);
            }
            tLists.add(pList);

            // No superclass mapping for map-based data lists
            if (AsMapRefData.class.isAssignableFrom(pClass)) {
                return;
            }

            // Look in the retry mappings
            Set<Set<AsEmapiTreeMapList<?>>> tSets = mPendingSuperClasses.get(pClass);
            if (tSets != null) {
                for (Set<AsEmapiTreeMapList<?>> tSet : tSets) {
                    tSet.add(pList);
                }
                // Delayed mapping complete, now erase the pending mapping
                tSets.clear();
                mPendingSuperClasses.remove(pClass);
            }

            // Add defined superclass lists too
            Class<?> tClass = pClass.getSuperclass();
            while (tClass != Object.class) {
                AsListIf<?> tDataSource = getDataSource(tClass);
                if (tDataSource != null) {
                    // Data source exists, add its mapping
                    tLists.add((AsEmapiTreeMapList<?>) tDataSource);
                }
                else {
                    // Data source does not exist, add a temporary retry mapping to handle cases
                    // where the superclass list is defined after the subclass list
                    tSets = mPendingSuperClasses.get(tClass);
                    if (tSets == null) {
                        tSets = new HashSet<Set<AsEmapiTreeMapList<?>>>();
                        mPendingSuperClasses.put(tClass, tSets);
                    }
                    tSets.add(tLists);

                }
                tClass = tClass.getSuperclass();
            }
        }

        /***
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void onBroadcast(Object pMessage) {
            // "Normal" messages, including super class processing
            if (mBdxMapper.onBroadcast(pMessage, mClassToListMap)) {
                return;
            }

            // If the message is a tree data, register the tree root
            if (pMessage instanceof AsTreeData) {
                AsTreeData tTreeData = (AsTreeData) pMessage;
                AsTreeRoot tTree = new AsTreeRoot(tTreeData);
                putDataSource(tTree);
                return;
            }

            // If the message is a list tree data, register the list tree root
            if (pMessage instanceof AsListTreeDefinition) {
                AsListTreeDefinition tTreeData = (AsListTreeDefinition) pMessage;
                AsListTreeRoot<?> tTree = new AsListTreeRoot<Object>(tTreeData);
                putDataSource(tTree);
                return;
            }

            // XML based messages
            if (pMessage instanceof AsXmlRefData) {
                AsXmlRefData tData = (AsXmlRefData) pMessage;
                AsEmapiTreeMapList<Object> tList =
                    (AsEmapiTreeMapList<Object>) mTagNameToListMap.get(tData.getTagName());
                if (tList != null) {
                    tList.messageReceived(pMessage);
                }
                return;
            }
        }
    }

    /**
     * Adds the data source definitions.
     *
     * @param pAsDataSources the as data sources
     */
    public void addDataSourceDefinitions(Collection<AsDataSourceDef<?>> pAsDataSources) {
        for (AsDataSourceDef<?> tDef : pAsDataSources) {
            handleDatasourceDefinition(tDef);
        }
    }

    /***
     * {@inheritDoc}
     */
    @Override
    protected void putDataSource(AsDataSourceIf<?> pDataSource) {
        pDataSource.setPermanent();
        super.putDataSource(pDataSource);
        if (pDataSource instanceof AsListIf<?> && !mClassToListMapping.containsKey(pDataSource.getItemClass())) {
            mClassToListMapping.put(pDataSource.getItemClass(), (AsListIf<?>) pDataSource);
        }
    }

    /***
     * {@inheritDoc}
     */
    @Override
    protected void putDataSourceAs(AsDataSourceIf<?> pDataSource, String pDataSourceId) {
        pDataSource.setPermanent();
        super.putDataSourceAs(pDataSource, pDataSourceId);
        if (pDataSource instanceof AsListIf<?> && !mClassToListMapping.containsKey(pDataSource.getItemClass())) {
            mClassToListMapping.put(pDataSource.getItemClass(), (AsListIf<?>) pDataSource);
        }
    }

    /**
     * Gets the data source.
     *
     * @param <T> the type
     * @param pClass the class
     * @return the data source
     */
    @SuppressWarnings("unchecked")
    public <T> AsListIf<T> getDataSource(Class<T> pClass) {
        return (AsListIf<T>) mClassToListMapping.get(pClass);
    }

    /**
     * Gets the data source id.
     *
     * @param pObjectTypeOrDataSourceId the object type or data source id
     * @return the data source id
     */
    public String getDataSourceId(String pObjectTypeOrDataSourceId) {
        Class<?> tClass = As.getType(pObjectTypeOrDataSourceId);
        if (tClass != null) {
            AsListIf<?> tList = As.getGlobalDataSources().getDataSource(tClass);
            if (tList != null) {
                return tList.getDataSourceId();
            }
        }
        return pObjectTypeOrDataSourceId;
    }

}
