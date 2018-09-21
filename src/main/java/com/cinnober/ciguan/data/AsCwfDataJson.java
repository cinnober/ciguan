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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfFilterIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * This class holds data which can either be a complete model, or a part of a more
 * complex model. It is the basic client side building block for models.
 *
 * Note that the setProperty and getProperty methods can take attribute names
 * on the form "foo.bar.attribute". In this case, the methods will automatically
 * handle the potentially missing sublevels. In case of setProperty, the missing levels
 * are automatically created, while getProperty will return null if a level is missing.
 *
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_NULL)
class AsCwfDataJson extends JSONObject implements CwfDataIf, Serializable, MvcModelAttributesIf {

    public AsCwfDataJson() {
    }

	public AsCwfDataJson(CwfDataIf pData) {
		super(pData.toString());
	}
	
	@Override
	public String type() {
        return CwfDataTool.type(this);
	}
	
	@Override
	public String getProperty(String pPropertyName) {
		Object object = $get(pPropertyName);
		if (object instanceof JSONString) {
			return ((JSONString) object).toString();
		}
		return object != null ? object.toString() : null;
	}

	@Override
	public Boolean getBooleanProperty(String pPropertyName) {
		Object object = $get(pPropertyName);
		// taking care of legacy config...
		if (object instanceof String) {
			return Boolean.valueOf((String) object).booleanValue();
		}
		return (Boolean) $get(pPropertyName);
	}

	@Override
	public Byte getByte(String pName) {
		return (Byte) $get(pName);
	}

	@Override
	public Character getChar(String pName) {
		return (Character) $get(pName);
	}

	@Override
	public Integer getIntProperty(String pAttributeName) {
		return (Integer) $get(pAttributeName);
	}

	@Override
	public Long getLongProperty(String pAttributeName) {
		return (Long) $get(pAttributeName);
	}

	@Override
	public String[] getStringArray(String pAttributeName) {
		JSONArray array = (JSONArray) $get(pAttributeName);
		List<String> list = new ArrayList<>();
		array.forEach(i -> list.add(i.toString()));
		return list.toArray(new String[list.size()]);
	}

	@Override
	public String[][] getStringDoubleArray(String pAttributeName) {
		JSONArray a1 = (JSONArray) $get(pAttributeName);
		String[][] r1 = new String[a1.length()][];
		for (int i = 0; i < a1.length(); i++) {
			JSONArray a2 = a1.getJSONArray(i);
			r1[i] = new String[a2.length()];
			for (int j = 0; j < a2.length(); j++) {
				r1[i][j] = a2.getString(j);
			}
		}		
		return r1;
	}

	@Override
	public Map<String, String[]> getStringArrayMap(String pAttributeName) {
		JSONObject object = (JSONObject) $get(pAttributeName);
		
		Map<String, String[]> map = new HashMap<>();
		JSONArray names = object.names();
		names.forEach(n -> {
			JSONArray jsonArray = object.getJSONArray(n.toString());
			String[] array = new String[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				array[i] = jsonArray.getString(i);
			}
			map.put(n.toString(), array);
		});
		
		return map;
	}

	@Override
	public int[] getIntArray(String pAttributeName) {
		JSONArray array = (JSONArray) $get(pAttributeName);
		int[] ret = new int[array.length()];
		for (int i = 0; i < array.length(); i++) {
			ret[i] = array.getInt(i);
		}
		return ret;
	}

	@Override
	public CwfDataIf getObject(String pObjectName) {
        int tPos = pObjectName.indexOf(".");
        if (tPos == -1) {
        	try {
        		Object object = get(pObjectName);
        		if (object instanceof CwfDataIf) {
        			return (CwfDataIf) get(pObjectName);
        		}
        	}
        	catch (JSONException e) {
        		// nothing
        	}
        	return null;
        }
        String tChildName = pObjectName.substring(0, tPos);
        CwfDataIf tChild = getObject(tChildName);
        if (tChild == null) {
            return null;
        }
        return tChild.getObject(pObjectName.substring(tPos + 1));
	}

	@Override
	public void setProperty(String pPropertyName, String pValue) {
		$put(pPropertyName, pValue);
	}

	@Override
	public void setProperty(String pAttrStatusCode, Integer pValue) {
		$put(pAttrStatusCode, pValue);
	}

	@Override
	public void setProperty(String pAttributeName, int[] pValues) {
		$put(pAttributeName, new JSONArray(pValues));
	}

	@Override
	public void setProperty(String pAttributeName, Boolean pValue) {
		$put(pAttributeName, pValue);
	}

	@Override
	public void setProperty(String pAttributeName, Long pValue) {
		$put(pAttributeName, pValue);
	}

	@Override
	public void setProperty(String pAttributeName, String[][] pValues) {
		JSONArray a1 = new JSONArray();
		for (int i = 0; i < pValues.length; i++) {
			JSONArray a2 = new JSONArray();
			for (int j = 0; j < pValues[i].length; j++) {
				a2.put(pValues[i][j]);
			}
			a1.put(a2);
		}
		$put(pAttributeName, a1);
	}

	@Override
	public void setProperty(String pAttributeName, String[] pValues) {
		$put(pAttributeName, new JSONArray(pValues));
	}

	@Override
	public void setProperty(String pAttributeName, Map<String, String[]> pValues) {
		JSONObject object = new JSONObject();
		for (Entry<String, String[]> entry : pValues.entrySet()) {
			JSONArray array = new JSONArray();
			for (String string : entry.getValue()) {
				array.put(string);
			}
			object.put(entry.getKey(), array);
		}
		$put(pAttributeName, object);
	}

	@Override
	public void setObject(String pObjectName, CwfDataIf pValue) {
		$put(pObjectName, pValue);
	}

	@Override
	public void addObject(String pName, CwfDataIf pObject, int... pPosition) {
		JSONArray array = (JSONArray) $get(pName);
		if (array == null) {
			array = new JSONArray();
			$put(pName, array);
		}
		if (pPosition == null || pPosition.length == 0) {
			array.put(pObject);
		}
		else {
			array.put(pPosition[0], pObject);
		}
	}

	@Override
	public void removeObject(String pName, int pIndex) {
		JSONArray array = (JSONArray) $get(pName);
		array.remove(pIndex);
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> map = new HashMap<>();
		String[] names = getNames(this);
		for (String name : names) {
			try {
				Object object = get(name);
				if (object instanceof JSONString) {
					map.put(name, ((JSONString) object).toString());
				}
			}
			catch (JSONException e) {
				// nothing
			}
		}
		return map;
	}

	@Override
	public Map<String, CwfDataIf> getObjects() {
		Map<String, CwfDataIf> map = new HashMap<>();
		String[] names = getNames(this);
		for (String name : names) {
			try {
				Object object = get(name);
				if (object instanceof JSONObject) {
					map.put(name, (CwfDataIf) object);
				}
			}
			catch (JSONException e) {
				// nothing
			}
		}
		return map;
	}

	@Override
	public Map<String, List<CwfDataIf>> getObjectListMap() {
		Map<String, List<CwfDataIf>> map = new HashMap<>();
		String[] names = getNames(this);
		for (String name : names) {
			try {
				Object object = get(name);
				if (object instanceof JSONArray) {
					List<CwfDataIf> list = new ArrayList<>();
					((JSONArray) object).forEach(o -> list.add((CwfDataIf) o));
					map.put(name, list);
				}
			}
			catch (JSONException e) {
				// nothing
			}
		}
		return map;
	}

	@Override
	public List<CwfDataIf> getObjectList(String pAttrItems) {
		Object object = $get(pAttrItems);
		if (object instanceof JSONArray) {
			List<CwfDataIf> list = new ArrayList<>();
			((JSONArray) object).forEach(o -> list.add((CwfDataIf) o));
			return list;
		}
		return Collections.emptyList();
	}

	@Override
	public boolean test(CwfFilterIf pCondition) {
        if (pCondition == null) {
            return true;
        }
        String tCurrentValue = getProperty(pCondition.getName());
        String tCompareValue = pCondition.getValue();
        switch (pCondition.getOperator()) {
            case Equals:
                return tCompareValue.equals(tCurrentValue);
            case GreaterThan:
                return tCompareValue.compareTo(tCurrentValue) < 0;
            case GreaterThanOrEqual:
                return tCompareValue.compareTo(tCurrentValue) <= 0;
            case IsNull:
                return tCurrentValue == null;
            case IsNotNull:
                return tCurrentValue != null;
            case LessThan:
                return tCompareValue.compareTo(tCurrentValue) > 0;
            case LessThanOrEqual:
                return tCompareValue.compareTo(tCurrentValue) >= 0;
            case NotEquals:
                return !tCompareValue.equals(tCurrentValue);
            case StartsWith:
                return tCurrentValue != null ? tCurrentValue.startsWith(tCompareValue) : false;
            case Contains:
                return tCurrentValue != null && tCompareValue != null && tCurrentValue.contains(tCompareValue);
            default:
                throw new IllegalArgumentException("Operator " +
                    pCondition.getOperator().getExpression() + " not supported");
        }
	}

	@Override
	public List<CwfDataIf> getObjects(String pName) {
		return getObjectList(pName);
	}

	@Override
	public void setObject(String pAttrName, Collection<CwfDataIf> pData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addObject(String pAttrName, Collection<CwfDataIf> pData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String removeProperty(String pProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CwfDataIf removeObject(String pName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeObject(String pListName, CwfDataIf pData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replaceObject(String pTagField, CwfDataIf pOldData, CwfDataIf pNewData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<CwfDataIf> removeObjectList(String pName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CwfDataIf> getAllChildNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	protected final void $put(String pAttributeName, Object pValue) {
        int tPos = pAttributeName.indexOf(".");
        if (tPos == -1) {
            if (pAttributeName.endsWith("[0]")) {
                $put(pAttributeName.substring(0, pAttributeName.length() - 3), new Object[] { pValue });
                return;
            }
            put(pAttributeName, pValue);
            return;
        }
        String tChildName = pAttributeName.substring(0, tPos);
        CwfDataIf tChild;
        if (tChildName.matches("^.*\\[\\d+\\]$")) {
            int tRowNumber = Integer.valueOf(tChildName.substring(tChildName.lastIndexOf('[') + 1,
                tChildName.lastIndexOf(']')));
            tChildName = tChildName.substring(0, tChildName.length() - Integer.toString(tRowNumber).length() - 2);
            if (has(tChildName)) {
                JSONArray tList = (JSONArray) get(tChildName);
                if (tRowNumber > tList.length()) {
                    throw new ArrayIndexOutOfBoundsException(tRowNumber);
                }

                if (tRowNumber == tList.length()) {
                    tList.put(CwfDataFactory.create());
                }

                tChild = (CwfDataIf) tList.get(tRowNumber);
            }
            else {
                if (tRowNumber != 0) {
                    throw new ArrayIndexOutOfBoundsException(tRowNumber);
                }
                tChild = CwfDataFactory.create();
                addObject(tChildName, tChild);
            }
        }
        else {
            tChild = getObject(tChildName);
            if (tChild == null) {
                tChild = CwfDataFactory.create();
                $put(tChildName, tChild);
            }
        }
        ((AsCwfDataJson) tChild).$put(pAttributeName.substring(tPos + 1), pValue);
	}

	protected final Object $get(String pAttributeName) {
		int tPos = pAttributeName.indexOf(".");
	    if (tPos == -1) {
	    	try {
	    		return get(pAttributeName);
	    	}
	    	catch (JSONException e) {
	    		// nothing
	    	}
	    	return null;
	    }
	    String tChildName = pAttributeName.substring(0, tPos);
	    
	    if (tChildName.matches("^.*\\[\\d+\\]$")) {
	        int tRowNumber = Integer.valueOf(tChildName.substring(tChildName.lastIndexOf('[') + 1,
	            tChildName.lastIndexOf(']')));
	        tChildName = tChildName.substring(0, tChildName.length() - Integer.toString(tRowNumber).length() - 2);
	        JSONArray tChild = (JSONArray) get(tChildName);
	        if (tChild == null || tRowNumber >= tChild.length()) {
	            return null;
	        }
	        return ((AsCwfDataJson) tChild.get(tRowNumber)).$get(pAttributeName.substring(tPos + 1));
	    }
	    else {
	        CwfDataIf tChild = getObject(tChildName);
	        if (tChild == null) {
	            return null;
	        }
	        return ((AsCwfDataJson) tChild).$get(pAttributeName.substring(tPos + 1));
	    }
	}
	
}
