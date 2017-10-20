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
package com.cinnober.ciguan.xml.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class storing a metadata definition
 *
 * <pre>
 * Example:
 *
 * {@code
 * <MetaData className="com.cinnober.ciguan.datasource.owner.AsNotification">
 *     <Attribute attributeName="foo" businessType="Volume"/>
 * </MetaData>
 * }
 * </pre>
 */
public class AsDefMetaData extends AsDef {

	/** The m class name. */
	private String mClassName;

	/** The m server request name. */
	private String mServerRequestName;

	/** The m state. */
	private String mState;

	/** The m attributes. */
	private Map<String, String> mAttributes = new LinkedHashMap<String, String>();

	/**
	 * Instantiates a new as metadata definition.
	 *
	 * @param pNode the node
	 */
	public AsDefMetaData(Element pNode) {
		this(pNode.getAttribute("className"));
		mServerRequestName = pNode.getAttribute("serverRequestName");
		mState = pNode.getAttribute("state");
		if (mServerRequestName.isEmpty()) {
			mServerRequestName = null;
		}
		NodeList tNodes = pNode.getElementsByTagName("Attribute");
		for (int i = 0; i < tNodes.getLength(); i++) {
			Element tAttr = (Element) tNodes.item(i);
			String tAttributeName = tAttr.getAttribute("attributeName");
			String tBusinessType = tAttr.getAttribute("businessType");
			mAttributes.put(tAttributeName, tBusinessType);
		}
	}

	/**
	 * Instantiates a new metadata definition.
	 *
	 * @param pNode the node
	 * @param pOldDef the old def
	 */
	public AsDefMetaData(Element pNode, AsDefMetaData pOldDef) {
		this(pNode);
		if (mServerRequestName == null && pOldDef.getServerRequestName() != null) {
			mServerRequestName = pOldDef.getServerRequestName();
		}
		if (mState.isEmpty() && !pOldDef.getState().isEmpty()) {
			mState = pOldDef.getState();
		}
		for (Map.Entry<String, String> tAttribute : pOldDef.getAttributes().entrySet()) {
			if (!mAttributes.containsKey(tAttribute.getKey())) {
				mAttributes.put(tAttribute.getKey(), tAttribute.getValue());
			}
		}
	}

	/**
	 * Instantiates a new metadata definition.
	 *
	 * @param pType the type
	 */
	public AsDefMetaData(String pType) {
		mClassName = pType;
	}

	/**
	 * Gets the class name.
	 *
	 * @return the class name
	 */
	public String getClassName() {
		return mClassName;
	}

	/**
	 * Gets the server request name.
	 *
	 * @return the server request name
	 */
	public String getServerRequestName() {
		return mServerRequestName;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public String getState() {
		return mState;
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() {
		return mAttributes;
	}

	/**
	 * Gets the business type.
	 *
	 * @param pField the field
	 * @return the business type
	 */
	public String getBusinessType(String pField) {
		return mAttributes.get(pField);
	}

}