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
package com.cinnober.ciguan.datasource.summary;

import java.lang.reflect.Constructor;

import com.cinnober.ciguan.AsConfigXmlParserIf;
import com.cinnober.ciguan.AsFormatIf;
import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.data.AsViewDefinition;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.AsViewportSummaryHandlerIf;
import com.cinnober.ciguan.datasource.tree.AsViewField;

/**
 *
 * Base class for viewport summary handlers
 * 
 * @param <T> the type of object that the handler operates on
 */
public abstract class AsViewportSummaryHandler<T> implements AsViewportSummaryHandlerIf<T> {

    /** The value getter. */
    protected final AsGetMethodIf<T> mValueGetter;
    
    /** The service. */
    protected final AsDataSourceServiceIf mService;
    
    /**
     * Instantiates a new as viewport summary handler.
     *
     * @param pValueGetter the value getter
     * @param pService the service
     */
    public AsViewportSummaryHandler(AsGetMethodIf<T> pValueGetter, AsDataSourceServiceIf pService) {
        mValueGetter = pValueGetter;
        mService = pService;
    }

    @Override
    public CwfBusinessTypeIf getBusinessType() {
        return mValueGetter.getBusinessType();
    }
    
    @Override
    public String getBusinessSubtype() {
        return mValueGetter.getBusinessSubtype();
    }
    
    @Override
    public String getText() {
        return AsFormatIf.Singleton.get().format(getValue(), getBusinessType(), mService.getLocale());
    }
    
    /**
     * Get the underlying value.
     *
     * @return the value
     */
    protected abstract Object getValue();
    
    /**
     * Creates the handler.
     *
     * @param <T> the generic type
     * @param pViewId the view id
     * @param pGetMethod the get method
     * @param pService the service
     * @return the handler
     */
    public static <T> AsViewportSummaryHandlerIf<T> create(
        String pViewId, AsGetMethodIf<T> pGetMethod, AsDataSourceServiceIf pService) {
        AsViewDefinition tViewDefinition = AsConfigXmlParserIf.SINGLETON.get().getViewDefinitions().get(pViewId);
        if (tViewDefinition != null) {
            String tFieldName = pGetMethod.getAttributeName();
            for (AsViewField tField : tViewDefinition.getFields()) {
                if (tField.getName().equals(tFieldName) &&
                    tField.getSummary() != null &&
                    !tField.getSummary().isEmpty()) {
                    String tSummary = tField.getSummary();
                    String[] tParts = tSummary.split(":");
                    HandlerType tType = HandlerType.valueOf(tParts[0]);
                    if (tType == HandlerType.custom) {
                        return createHandler(tParts[1], pGetMethod, pService);
                    }
                    else {
                        return createHandler(tType.getHandlerType().getName(), pGetMethod, pService);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Creates the handler.
     *
     * @param <T> the generic type
     * @param pType the type
     * @param pGetMethod the get method
     * @param pService the service
     * @return the handler
     */
    @SuppressWarnings("unchecked")
    private static <T> AsViewportSummaryHandlerIf<T> createHandler(
        String pType, AsGetMethodIf<T> pGetMethod, AsDataSourceServiceIf pService) {
        try {
            Class<?> tClass = Class.forName(pType);
            if (tClass == null) {
                throw new ClassNotFoundException("Summary class not found: " + pType);
            }
            if (!AsViewportSummaryHandlerIf.class.isAssignableFrom(tClass)) {
                throw new RuntimeException("Summary class "  + pType +
                    " must implement the AsViewportSummaryHandlerIf interface");
            }
            Constructor<?> tConstructor =
                tClass.getConstructor(AsGetMethodIf.class, AsDataSourceServiceIf.class);
            AsViewportSummaryHandlerIf<T> tNewInstance =
                (AsViewportSummaryHandlerIf<T>) tConstructor.newInstance(pGetMethod, pService);
            return (AsViewportSummaryHandlerIf<T>) tNewInstance;
        }
        catch (Exception e) {
            throw new RuntimeException("Error instantiating summary class "  + pType, e);
        }
    }
    
}
