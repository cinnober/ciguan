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

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.CwfGlobalDataSources;
import com.cinnober.ciguan.datasource.filter.AsAttributeValueFilter;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;
import com.cinnober.ciguan.datasource.listtree.AsListTreeNode;
import com.cinnober.ciguan.datasource.tree.AsTreeNode;
import com.cinnober.ciguan.impl.As;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper class holding menu item definition.
 */
@SuppressWarnings("deprecation")
@JsonInclude(Include.NON_NULL)
public class AsMenuItem extends AsXmlRefData {

	/** The key. */
	@JsonIgnore
	public String key;

	/**
	 * Instantiates a new as menu item.
	 *
	 * @param pXmlDefinition the xml definition
	 */
	public AsMenuItem(CwfDataIf pXmlDefinition) {
		super(pXmlDefinition);
		if (getValues().getProperty(ATTR_ID) == null) {
			getValues().setProperty(ATTR_ID, getValues().getProperty(ATTR_VIEW));
		}
		key = getId();
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return getValues().getProperty(ATTR_ID);
	}

	/**
	 * Gets the expand.
	 *
	 * @return the expand
	 */
	public String getExpand() {
		String tExpand = getValues().getProperty(ATTR_EXPAND);
		if (tExpand != null && tExpand.startsWith("$model.")) {
			tExpand = tExpand.substring(7);
		}
		return tExpand;
	}

	/**
	 * Checks if is auto submit.
	 *
	 * @return true, if is auto submit
	 */
	@JsonInclude(Include.NON_DEFAULT)
	public boolean isAutoSubmit() {
		Boolean tBool = getValues().getBooleanProperty(ATTR_AUTO_SUBMIT);
		return tBool != null ? tBool : false;
	}

	/**
	 * Gets the confirm message.
	 *
	 * @return the confirm message
	 */
	public String getConfirmMessage() {
		return getValues().getProperty(ATTR_CONFIRM_MESSAGE);
	}

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	public String getParameters() {
		return getValues().getProperty(ATTR_PARAMETERS);
	}

	/**
	 * Gets the parameter.
	 *
	 * @param pName the name
	 * @return the parameter
	 */
	@JsonIgnore
	public String getParameter(String pName) {
		String[] tParameters = (getParameters() != null ? getParameters().split(",") : new String[0]);
		for (String tParameter : tParameters) {
			String[] tNameValue = tParameter.split("=");
			if (tNameValue[0].equals(pName)) {
				return tNameValue[1];
			}
		}
		return null;
	}

	/**
	 * Gets the view.
	 *
	 * @return the view
	 */
	@JsonIgnore
	public AsViewDefinition getView() {
		String tViewId = getValues().getProperty(ATTR_VIEW);
		if (tViewId == null) {
			return null;
		}
		AsListIf<?> tList = (AsListIf<?>) As.getGlobalDataSources().
				getDataSource(com.cinnober.ciguan.datasource.CwfGlobalDataSources.MVC_VIEWDEFS_ALL, null, null);
		AsViewDefinition tViewDef = (AsViewDefinition) tList.get(tViewId);
		return tViewDef;
	}
	
	public String getViewId() {
		return getValues().getProperty(ATTR_VIEW);
	}

	/**
	 * Gets the acc service.
	 *
	 * @return the acc service
	 */
	@JsonProperty("auth")
	@JsonInclude(Include.NON_EMPTY)
	public String getAccService() {
		String tAccService = getValues().getProperty(ATTR_ACC_SERVICE);
		if (tAccService != null) {
			return tAccService;
		}
		AsViewDefinition tView = getView();
		return tView == null ? null : tView.getAccService();
	}

	/**
	 * Checks if is included.
	 *
	 * @param pConnection the connection
	 * @param pObjects the objects
	 * @return {@code true}, if is included
	 */
	@JsonIgnore
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean isIncluded(AsConnectionIf pConnection, Object... pObjects) {
		for (Object tObject : pObjects) {
			Object tItem = tObject;
			if (tObject instanceof AsTreeNode) {
				tItem = ((AsTreeNode) tObject).getItem();
			}
			if (tObject instanceof AsListTreeNode) {
				tItem = ((AsListTreeNode) tObject).getItem();
			}
			String tFilterExpression = getValues().getProperty(ATTR_FILTER);
			if (tFilterExpression != null && !tFilterExpression.isEmpty()) {
				AsFilterIf tFilter = createFilter(pConnection, tItem, tFilterExpression);
				if (!tFilter.include(tItem)) {
					return false;
				}
			}

		}
		return true;
	}

	/**
	 * Check access to a specific menu item.
	 *
	 * @param pConnection the connection
	 * @return {@code true}, if is access allowed
	 */
	@JsonIgnore
	public boolean isAccessAllowed(AsConnectionIf pConnection) {
		if (getValues().getObjectList(TAG_MENUITEM).isEmpty()) {
			AsDataSourceIf<AsXmlRefData> tMenuItems = pConnection.
					getDataSourceService().getDataSource(CwfGlobalDataSources.MENUITEMS_ALL, null);
			// If the menu item is not found, the user does not have access to it
			AsXmlRefData tMenuItem = ((AsListIf<AsXmlRefData>) tMenuItems).get(getId());
			return tMenuItem != null;
		}
		return true;
	}

	/**
	 * Checks if is for perspective.
	 *
	 * @param pPerspective the perspective
	 * @return {@code true}, if is for perspective
	 */
	@JsonIgnore
	public boolean isForPerspective(String pPerspective) {
		String tPerspective = getValues().getProperty(ATTR_PERSPECTIVE);
		return tPerspective == null || tPerspective.equals(pPerspective);
	}

	/**
	 * Creates the filter.
	 *
	 * @param pConnection the connection
	 * @param pItem the item
	 * @param pFilterExpression the filter expression
	 * @return the as filter if
	 */
	@SuppressWarnings("rawtypes")
	protected AsFilterIf<?> createFilter(AsConnectionIf pConnection, Object pItem, String pFilterExpression) {
		// Attempt to create a class filter first
		String tFilterClass = pFilterExpression.indexOf('.') >= 0 ? pFilterExpression :
			"com.cinnober.ciguan.datasource.filter." + pFilterExpression;
		try {
			Class<?> tClass = Class.forName(tFilterClass);
			if (!AsFilterIf.class.isAssignableFrom(tClass)) {
				throw new RuntimeException("Menu filter class '" + tFilterClass + "' is not a filter implementation");
			}
			Constructor tConstructor = tClass.getConstructor(AsConnectionIf.class);
			AsFilterIf tFilter = (AsFilterIf) tConstructor.newInstance(pConnection);
			return tFilter;
		}
		catch (ClassNotFoundException e) {
			// Not a class based filter, proceed
		}
		catch (Exception e) {
			throw new RuntimeException("Could not instantiate menu filter of type '" + tFilterClass + "'", e);
		}

		// Default: Use a getter based filter
		return AsAttributeValueFilter.create(pItem.getClass(), pFilterExpression);
	}

}
