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
package com.cinnober.ciguan.datasource.filter;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.CwfBusinessTypeIf;
import com.cinnober.ciguan.client.util.StringEscape;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.RpcFilterCriteriaIf;
import com.cinnober.ciguan.datasource.getter.AsGetMethod;
import com.cinnober.ciguan.impl.CwfBusinessTypes;

/**
 * Generic attribute value comparison implementation of a server side filter
 * @param <T> The type of the object being filtered
 */
public abstract class AsAttributeValueFilter<T> extends AsFilter<T> {

    protected final AsGetMethodIf<T> mMethod;
    private final String mToString;
    private boolean mHidden;
    
    protected AsAttributeValueFilter(AsGetMethodIf<T> pMethod, RpcFilterCriteriaIf pFilterCriteria) {
        super(pFilterCriteria); 
        mMethod = pMethod;
        mToString = pFilterCriteria.toString();
    }

    @Override
    public boolean include(T pObject) {
        Object tValue = mMethod.getObject(pObject);
        // Sets and maps are treated special
        if (tValue instanceof Set<?>) {
            if (((Set<?>) tValue).contains(getFilterCriteria().getValue())) {
                return true;
            }
        }
        if (tValue instanceof Map<?, ?>) {
            if (((Map<?, ?>) tValue).containsKey(getFilterCriteria().getValue())) {
                return true;
            }
        }
        return testValue(tValue);
    }
    
    protected abstract boolean testValue(Object pValue);

    @Override
    public String toString() {
        return mHidden ? "" : mToString;
    }
    
    private static <T> AsFilterIf<T> falseFilter() {
        return new AsFilterIf<T>() {
            @Override
            public boolean include(Object pObject) {
                return false;
            }
        };
    }

    protected void setHidden() {
        mHidden = true;
    }
    
    public static <T> AsFilterIf<T> createHidden(Class<T> pClass, String pFilterExpression) {
        AsFilterIf<T> tFilter = create(pClass, pFilterExpression);
        if (tFilter instanceof AsAttributeValueFilter<?>) {
            ((AsAttributeValueFilter<?>) tFilter).setHidden();
        }
        if (tFilter instanceof AsArrayFilter<?>) {
            ((AsArrayFilter<?>) tFilter).setHidden();
        }
        return tFilter;
    }
    
    public static <T> AsFilterIf<T> create(Class<T> pClass, String pFilterExpression) {
        if (pFilterExpression.contains(",")) {
            return new AsArrayFilter<T>(pClass, pFilterExpression);
        }
        
        RpcFilterCriteriaIf tFilterCriteria = RpcAttributeFilterCriteria.fromString(pFilterExpression);
        RpcFilterOperator tOperator = tFilterCriteria.getOperator();
        AsGetMethodIf<T> tMethod = AsGetMethod.create(pClass, tFilterCriteria.getAttributeName());
        String tCompareValue = StringEscape.unescape(tFilterCriteria.getValue());
        CwfBusinessTypeIf tBusinessType = tMethod.getBusinessType();
        
        if (tFilterCriteria.getOperator() == RpcFilterOperator.IsNull) {
            tOperator = RpcFilterOperator.Equals;
            tCompareValue = "";
        }
        else if (tFilterCriteria.getOperator() == RpcFilterOperator.IsNotNull) {
            tOperator = RpcFilterOperator.NotEquals;
            tCompareValue = "";
        }

        if (tCompareValue.isEmpty()) {
            switch (tOperator) {
                case Contains:
                case GreaterThanOrEqual:
                case LessThanOrEqual:
                case StartsWith:
                case Equals:
                    if (tBusinessType.getUnderlyingType() == String.class) {
                        return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                            @Override
                            protected boolean testValue(Object pValue) {
                                return pValue == null || pValue.toString().isEmpty();
                            }
                        };
                    }
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue == null;
                        }
                    };
                case NotEquals:
                    if (tBusinessType.getUnderlyingType() == String.class) {
                        return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                            @Override
                            protected boolean testValue(Object pValue) {
                                return pValue != null && !pValue.toString().isEmpty();
                            }
                        };
                    }
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null;
                        }
                    };
                default:
                    return falseFilter();
            }
        }
        if (tBusinessType.getUnderlyingType() == String.class) {
            final String tValue = tCompareValue;
            switch (tOperator) {
                case Contains:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        List<String> mValues = Arrays.asList(tValue.split(","));
                        @Override
                        protected boolean testValue(Object pValue) {
                            return mValues.contains(pValue == null ? "" : pValue.toString());
                        }
                    };
                case Equals:
                    if (tBusinessType == CwfBusinessTypes.Constant && tValue.contains("|")) {
                        return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                            List<String> mValues = Arrays.asList(tValue.split("\\|"));
                            @Override
                            protected boolean testValue(Object pValue) {
                                return mValues.contains(pValue == null ? "" : pValue.toString());
                            }
                        };
                    }
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return tValue.equals(pValue == null ? "" : pValue.toString());
                        }
                    };
                case NotEquals:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return !tValue.equals(pValue == null ? "" : pValue.toString());
                        }
                    };
                case GreaterThanOrEqual:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null && pValue.toString().compareTo(tValue) >= 0;
                        }
                    };
                case LessThanOrEqual:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null && pValue.toString().compareTo(tValue) <= 0;
                        }
                    };
                case StartsWith:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        /**
                         * Mimick "startsWithIgnoreCase", which isn't there
                         */
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null && pValue.toString().regionMatches(
                                true, 0, tValue, 0, tValue.length());
                        }
                    };
                default:
                    return falseFilter();
            }
        }
        
        if (tBusinessType.getUnderlyingType() == Long.class ||
            tBusinessType.getUnderlyingType() == BigInteger.class) {
            final Long tValue = Long.valueOf(tCompareValue);
            switch (tOperator) {
                case Equals:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null && tValue.longValue() == ((Number) pValue).longValue();
                        }
                    };
                case NotEquals:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue == null || tValue.longValue() != ((Number) pValue).longValue();
                        }
                    };
                case GreaterThanOrEqual:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null && ((Number) pValue).longValue() >= tValue.longValue();
                        }
                    };
                case LessThanOrEqual:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null && ((Number) pValue).longValue() <= tValue.longValue();
                        }
                    };
                default:
                    return falseFilter();
            }
        }

        if (tBusinessType.getUnderlyingType() == Integer.class) {
            final Integer tValue = Integer.valueOf(tCompareValue);
            switch (tOperator) {
                case Equals:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null && tValue.intValue() == ((Number) pValue).intValue();
                        }
                    };
                case NotEquals:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue == null || tValue.intValue() != ((Number) pValue).intValue();
                        }
                    };
                case GreaterThanOrEqual:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null && ((Number) pValue).intValue() >= tValue.intValue();
                        }
                    };
                case LessThanOrEqual:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return pValue != null && ((Number) pValue).intValue() <= tValue.intValue();
                        }
                    };
                default:
                    return falseFilter();
            }
        }
        if (tBusinessType.getUnderlyingType() == Boolean.class) {
            final Boolean tValue = Boolean.valueOf(tCompareValue);
            switch (tOperator) {
                case Equals:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return tValue.equals(pValue);
                        }
                    };
                case NotEquals:
                    return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                        @Override
                        protected boolean testValue(Object pValue) {
                            return !tValue.equals(pValue);
                        }
                    };
                default:
                    return falseFilter();
            }
        }
        if (tBusinessType.getUnderlyingType() == Object.class) {
            final String tValue = tCompareValue;
            if ("Set".equals(tMethod.getBusinessSubtype())) {
                switch (tOperator) {
                    case Equals:
                        return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                            @Override
                            protected boolean testValue(Object pValue) {
                                return ((Set<?>) pValue).contains(tValue);
                            }
                        };
                    case NotEquals:
                        return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                            @Override
                            protected boolean testValue(Object pValue) {
                                return !((Set<?>) pValue).contains(tValue);
                            }
                        };
                    default:
                        return falseFilter();
                }
            }
            if ("Map".equals(tMethod.getBusinessSubtype())) {
                switch (tOperator) {
                    case Equals:
                        return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                            @Override
                            protected boolean testValue(Object pValue) {
                                return ((Map<?, ?>) pValue).containsKey(tValue);
                            }
                        };
                    case NotEquals:
                        return new AsAttributeValueFilter<T>(tMethod, tFilterCriteria) {
                            @Override
                            protected boolean testValue(Object pValue) {
                                return !((Map<?, ?>) pValue).containsKey(tValue);
                            }
                        };
                    default:
                        return falseFilter();
                }
            }
            return falseFilter();
        }
        
        throw new RuntimeException("Filter (" + tOperator + ") not implemented");
    }

    /**
     * Array implementation of a filter
     */
    private static class AsArrayFilter<T> implements AsFilterIf<T> {

        private final AsFilterIf<T>[] mFilters;
        private final String mFilterExpression;
        private boolean mHidden;
        
        @SuppressWarnings("unchecked")
        public AsArrayFilter(Class<T> pClass, String pFilterExpression) {
            mFilterExpression = pFilterExpression;
            String[] tParts = pFilterExpression.split(",");
            mFilters = new AsFilterIf[tParts.length];
            for (int i = 0; i < tParts.length; i++) {
                mFilters[i] = create(pClass, tParts[i]);
            }
        }
     
        @Override
        public boolean include(T pObject) {
            for (AsFilterIf<T> tFilter : mFilters) {
                if (!tFilter.include(pObject)) {
                    return false;
                }
            }
            return true;
        };
        
        @Override
        public String toString() {
            return mHidden ? "" : mFilterExpression;
        }
        
        public void setHidden() {
            mHidden = true;
        }
        
    }
    
}
