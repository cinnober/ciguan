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
package com.cinnober.ciguan.data;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.AsMetaDataHandlerIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.datasource.AsDataSourceFactoryIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.filter.AsAttributeValueFilter;
import com.cinnober.ciguan.datasource.filter.AsFilter;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;
import com.cinnober.ciguan.impl.As;

/**
 * Structured wrapper around a CwfData object in order to interpret it as
 * a data source tree configuration.
 *
 * @param <T> the generic type
 */
@SuppressWarnings("deprecation")
public class AsDataSourceDef<T> extends AsXmlRefData implements MvcModelAttributesIf {

    /** All the definitions. */
    private static Map<String, AsDataSourceDef<?>> cAllDefinitions = new LinkedHashMap<String, AsDataSourceDef<?>>();

    /** Contains all modifiable attributes. */
    private static Set<String> cModifiableAttributes = new HashSet<String>();
    static {
        cModifiableAttributes.add(ATTR_TEXT);
        cModifiableAttributes.add(ATTR_MEMBER_FILTER);
        cModifiableAttributes.add(ATTR_USER_FILTER);
        cModifiableAttributes.add(ATTR_FACTORY);
        cModifiableAttributes.add(ATTR_TYPE);
    }

    /** The item class. */
    private Class<T> mItemClass;

    /** The member filter constructor. */
    private Constructor<AsFilter<T>> mMemberFilterConstructor;

    /** The user filter constructor. */
    private Constructor<AsFilter<T>> mUserFilterConstructor;

    /** The data souurce factory. */
    private AsDataSourceFactoryIf<?> mFactory;

    /**
     * Instantiates a new as data source def.
     *
     * @param pParent the parent
     * @param pData the data
     */
    @SuppressWarnings("unchecked")
    public AsDataSourceDef(AsDataSourceDef<T> pParent, CwfDataIf pData) {
        super(pData);
        assert cAllDefinitions.containsKey(getId()) == false;
        if (pParent != null) {
            getValues().setProperty(ATTR_SOURCE, pParent.getId());
        }
        if (has(ATTR_SOURCE)) {
            assert cAllDefinitions.containsKey(getSource());
            assert getValues().getProperty(ATTR_TYPE) == null;
            assert getValues().getProperty(ATTR_TEXT) == null;
            assert getValues().getProperty(ATTR_KEY) == null;
            AsDataSourceDef<T> tSource = (AsDataSourceDef<T>) cAllDefinitions.get(getSource());
            getValues().setProperty(ATTR_TYPE, tSource.getType());
            getValues().setProperty(ATTR_TEXT, tSource.getTextField());
            getValues().setProperty(ATTR_KEY, tSource.getKeyField());
        }
        init();
        cAllDefinitions.put(getId(), this);
    }

    /**
     * Initializes the data source definition fields.
     */
    protected void init() {
        mFactory = initFactory();
        mMemberFilterConstructor = getConstructor(getMemberFilter(), String.class);
        mUserFilterConstructor = getConstructor(getUserFilter(), String.class, String.class);
    }

    /**
     * Gets the constructor.
     *
     * @param pFilter the filter
     * @param pConstructorParams the constructor params
     * @return the constructor
     */
    private Constructor<AsFilter<T>> getConstructor(String pFilter, Class<?>... pConstructorParams) {
        if (pFilter == null || pFilter.length() == 0) {
            return null;
        }
        try {
            if (pFilter.indexOf(".") == -1) {
                pFilter = "com.cinnober.ciguan.datasource.filter." + pFilter;
            }
            Class<AsFilter<T>> tFilterClass = getItemClass(pFilter);
            Constructor<AsFilter<T>> tConstructor = tFilterClass.getConstructor(pConstructorParams);
            return tConstructor;
        }
        catch (Exception e) {
            throw new RuntimeException("Error creating filter " + pFilter, e);
        }
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return get(ATTR_ID);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        if (get(ATTR_TYPE).isEmpty() && mItemClass != null) {
            // Placeholder for later expansion since the meta data handler might not be initialized yet
            AsMetaDataHandlerIf tHandler = As.getMetaDataHandler();
            getValues().setProperty(ATTR_TYPE,
                    tHandler == null ? "@" + mItemClass.getName() : As.getTypeName(mItemClass));
        }
        return get(ATTR_TYPE);
    }

    /**
     * Gets the key field.
     *
     * @return the key field
     */
    public String getKeyField() {
        return get(ATTR_KEY);
    }

    /**
     * Gets the text field.
     *
     * @return the text field
     */
    public String getTextField() {
        return get(ATTR_TEXT);
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return get(ATTR_SOURCE);
    }

    /**
     * Gets the member filter.
     *
     * @return the member filter
     */
    public String getMemberFilter() {
        return get(ATTR_MEMBER_FILTER);
    }

    /**
     * Gets the user filter.
     *
     * @return the user filter
     */
    public String getUserFilter() {
        return get(ATTR_USER_FILTER);
    }

    /**
     * Gets the factory.
     *
     * @return the factory
     */
    public String getFactory() {
        return get(ATTR_FACTORY);
    }

    /**
     * Checks if is query data source.
     *
     * @return {@code true}, if is query data source
     */
    public boolean isQueryDataSource() {
        return Boolean.valueOf(get(ATTR_QUERY));
    }

    /**
     * Checks for.
     *
     * @param pProperty the property
     * @return {@code true}, if successful
     */
    private boolean has(String pProperty) {
        String tValue = getValues().getProperty(pProperty);
        return tValue != null && tValue.length() > 0;
    }

    /**
     * Gets the.
     *
     * @param pProperty the property
     * @return the string
     */
    private String get(String pProperty) {
        String tValue = getValues().getProperty(pProperty);
        return tValue == null ? "" : tValue;
    }

    /**
     * Gets the item class from parent.
     *
     * @return the item class from parent
     */
    @SuppressWarnings("unchecked")
    private Class<T> getItemClassFromParent() {
        AsDataSourceDef<?> tDef = this;
        while (tDef.getSource() != null && tDef.getSource().length() > 0) {
            tDef = cAllDefinitions.get(tDef.getSource());
        }
        return (Class<T>) tDef.getItemClass();
    }

    /**
     * Gets the item class.
     *
     * @return the item class
     */
    @SuppressWarnings("unchecked")
    public Class<T> getItemClass() {
        if (mItemClass == null && getType() != null && getType().length() > 0) {
            if (getType().indexOf('.') == -1) {
                mItemClass = (Class<T>) As.getType(getType());
            }
            else {
                mItemClass = getItemClass(getType());
            }
        }
        return mItemClass;
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public AsFilterIf<T> getFilter() {
        String tFilter = get(ATTR_FILTER);
        if (tFilter == null || tFilter.length() == 0) {
            return null;
        }
        if (tFilter.indexOf("=") > 0) {
            try {
                return AsAttributeValueFilter.createHidden(getItemClassFromParent(), tFilter);
            }
            catch (Exception e) {
                e.printStackTrace();
                // continue
            }
        }
        try {
            return getConstructor(tFilter).newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "Error when creating data source filter: " + tFilter);
        }
    }

    /**
     * Creates the member filter.
     *
     * @param pMember the member
     * @return the as filter
     */
    public AsFilter<T> createMemberFilter(String pMember) {
        if (mMemberFilterConstructor != null) {
            try {
                return mMemberFilterConstructor.newInstance(pMember);
            }
            catch (Exception e) {
                throw new RuntimeException(
                        "Error when creating member data source filter: " + get(ATTR_MEMBER_FILTER));
            }
        }
        return null;
    }

    /**
     * Creates the user filter.
     *
     * @param pMember the member
     * @param pUser the user
     * @return the as filter
     */
    public AsFilter<T> createUserFilter(String pMember, String pUser) {
        if (mUserFilterConstructor != null) {
            try {
                return mUserFilterConstructor.newInstance(pMember, pUser);
            }
            catch (Exception e) {
                throw new RuntimeException(
                        "Error when creating user data source filter: " + get(ATTR_USER_FILTER));
            }
        }
        return null;
    }

    /**
     * Gets the factory impl.
     *
     * @return the factory impl
     */
    public AsDataSourceFactoryIf<?> getFactoryImpl() {
        return mFactory;
    }

    /**
     * Inits the factory.
     *
     * @return the as data source factory if
     */
    @SuppressWarnings("unchecked")
    private AsDataSourceFactoryIf<?> initFactory() {
        AsDataSourceFactoryIf<?> tFactory = null;
        Class<?> tFactoryClass = getItemClass(getFactory());
        if (tFactoryClass != null) {
            try {
                tFactory = (AsDataSourceFactoryIf<?>) tFactoryClass.newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException("Can not create datasource factory: " +
                        tFactory + " (" + getId() + ")", e);
            }
            mItemClass = (Class<T>) tFactory.getItemClass();
        }
        return tFactory;
    }

    /**
     * Gets the item class.
     *
     * @param <C> the generic type
     * @param pType the type
     * @return the item class
     */
    @SuppressWarnings("unchecked")
    private <C> Class<C> getItemClass(String pType) {
        if (pType != null && pType.length() > 0) {
            try {
                return (Class<C>) Class.forName(pType);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("Can not create datasource items: " +
                        pType + " (" + getId() + ")", e);
            }
        }
        return null;
    }

    /**
     * Gets the all.
     *
     * @return the all
     */
    public static Collection<AsDataSourceDef<?>> getAll() {
        return cAllDefinitions.values();
    }

    /**
     * Gets the data source def.
     *
     * @param pDataSourceId the data source id
     * @return the data source def
     */
    public static AsDataSourceDef<?> getDataSourceDef(String pDataSourceId) {
        return cAllDefinitions.get(pDataSourceId);
    }

    /**
     * Removes the.
     *
     * @param pDataSourceId the data source id
     */
    public static void remove(String pDataSourceId) {
        cAllDefinitions.remove(pDataSourceId);
    }

    /**
     * Update.
     *
     * @param pDataSourceId the data source id
     * @param pDef the def
     */
    public static void update(String pDataSourceId, CwfDataIf pDef) {
        AsDataSourceDef<?> tDef = cAllDefinitions.get(pDataSourceId);
        if (tDef == null) {
            throw new RuntimeException("Attempting to modify non-existant data source " + pDataSourceId);
        }
        for (String tKey : pDef.getProperties().keySet()) {
            if (cModifiableAttributes.contains(tKey)) {
                tDef.getValues().setProperty(tKey, pDef.getProperty(tKey));
            }
        }
        // Re-initialize factory, filters etc.
        tDef.init();
    }

    /**
     * Gets the sort field.
     *
     * @return the sort field
     */
    public String getSortField() {
        return getValues().getProperty(ATTR_SORT);
    }

    /**
     * Clear.
     */
    public static void clear() {
        cAllDefinitions.clear();
    }

}
