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
package com.cinnober.ciguan.conf;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.cinnober.ciguan.annotation.CwfReference;
import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.client.impl.MvcViewTypeEnum;
import com.cinnober.ciguan.conf.base.AsConfigurationTestBase;
import com.cinnober.ciguan.data.AsDictionaryWord;
import com.cinnober.ciguan.datasource.AsGetMethodFactoryIf;
import com.cinnober.ciguan.datasource.AsGetMethodIf;
import com.cinnober.ciguan.datasource.impl.AsMapRefData;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;
import com.cinnober.ciguan.xml.impl.AsXmlTool;

/**
 * Tests the integrity of the ASMvcConfig.xml configuration file
 */
@SuppressWarnings("deprecation")
public class ConfigurationTest extends AsConfigurationTestBase implements MvcModelAttributesIf {

	/**
	 * Default module name to test. Set this to your own module in your BeforeClass initializer method.
	 */
	public static String cModule = "com.cinnober.ciguan.conf.Application";

	/**
	 * Comma separated list of constant group class names (without package) used
	 * in connection with dataSourceId on form fields. This is a work-around since
	 * constant group data sources are not declared in XML and would break the tests if
	 * referenced by configuration, even though it works in runtime.
	 *
	 * Set this at the same time as setting cModule in your BeforeClass initializer method.
	 */
	public static String cReferencedConstantGroupDataSourceIds = "";

	protected static String cNumericBusinessTypes =
			"@type='Amount' or " +
					"@type='BasisPoint' or " +
					"@type='Decimal' or " +
					"@type='InterestRate' or " +
					"@type='Number' or " +
					"@type='Percent' or " +
					"@type='Price' or " +
					"@type='Volume' or " +
					"@type='Integer' or " +
					"@type='Long' or " +
					"@type='Double'";

	protected static Map<String, Element> cAsDataSources;
	protected static Map<String, Element> cAsList;
	protected static Map<String, Element> cAsTree;
	protected static Map<String, Element> cAsListTree;
	protected static Map<String, Element> cMenuItemsIdMap;
	protected static Map<String, Element> cViews;
	protected static Map<String, Element> cPerspectives;
	protected static Map<String, Element> cSlotTemplates;
	protected static List<Element> cMenuItems;
	protected static Map<String, String> cSearchPackages;
	protected static Set<String> cMetaDatas;
	protected static Set<String> cReferencedConstantGroupDataSources;
	protected static List<String> cNoSearchPackage;
	protected static Map<String, Class<?>> cObjectTypes;
	protected static Map<Class<?>, Set<String>> cGetMethods;
	protected static Map<String, Element> cNumericPatterns;

	@BeforeClass
	public static void before() {
		cErrors.clear();
		try {
			setRoot(AsXmlTool.loadCwfModule(cModule).getDocumentElement());
			// load data sources
			cAsDataSources = createIdMap(
					"/Configuration/AsDataSources//AsList[@id]|" +
							"/Configuration/AsDataSources/AsTree|" +
					"/Configuration/AsDataSources/AsListTree");
			createList("/Configuration/AsDataSources//AsList[@type and not(@id)]")
			.stream()
			.forEach(e -> cAsDataSources.put(e.getAttribute("type"), e));
			cAsList = createIdMap("/Configuration/AsDataSources//AsList", false);
			cAsTree = createIdMap("/Configuration/AsDataSources/AsTree", false);
			cAsListTree = createIdMap("/Configuration/AsDataSources/AsListTree", false);
			//
			cMenuItemsIdMap = createIdMap("/Configuration/AsMenus//menuitem[@id]");
			cViews = createIdMap("/Configuration/AsMvc/view[@id]");
			cPerspectives = createIdMap("/Configuration/AsPerspectives/perspective[@id]", false);
			cSlotTemplates = createIdMap("/Configuration/AsPerspectives/slot-template[@id]", false);
			cMenuItems = createList("/Configuration/AsMenus//menuitem");
			cSearchPackages = createSearchPackageMap("/Configuration/AsMeta/SearchPackage");
			cMetaDatas = createSet("/Configuration/AsMeta/MetaData/@className");
			cNoSearchPackage = Arrays.asList(new String[] { "" });
			cNumericPatterns = createIdMap("/Configuration/AsLocales/AsDefaultPatterns/pattern[" +
					cNumericBusinessTypes + "]", false, ATTR_TYPE);
			cReferencedConstantGroupDataSources = new HashSet<String>(
					Arrays.asList(cReferencedConstantGroupDataSourceIds.split(",")));
			cObjectTypes = new HashMap<String, Class<?>>();
			cObjectTypes.put(null, null);
			cObjectTypes.put("", null);
			cObjectTypes.put("Session", Object.class);
			cObjectTypes.put("SessionModel", Object.class);
			cObjectTypes.put("MenuParameters", AsXmlRefData.class);
			cObjectTypes.put("TypeAheadFilter", AsXmlRefData.class);
			cObjectTypes.put("ListItemRequest", Object.class);
			findGetMethods();
			cLoadedOk = true;
		}
		catch (Throwable e) {
			// TODO: handle file not found and other io problems
			e.printStackTrace();
			Assert.fail("Error loading configuration");
		}
	}

	private static void findGetMethods() {
		cGetMethods = new HashMap<Class<?>, Set<String>>();
		for (Element tElement : createList("/Configuration/AsMeta/AsGetMethods/AsGetMethod")) {
			String tType = tElement.getAttribute(ATTR_TYPE);
			String tName = tElement.getAttribute(ATTR_NAME);
			String tExpression = tElement.getAttribute(ATTR_EXPRESSION);
			Class<?> tClass = ensureType(tType, tElement);

			// Deal with expression based getters first
			if (tExpression.length() > 0) {
				Set<String> tSet = cGetMethods.get(tClass);
				if (tSet == null) {
					cGetMethods.put(tClass, tSet = new HashSet<String>());
				}
				tSet.add(tName);
				continue;
			}

			// Type class must be found
			if (tClass == null) {
				continue;
			}

			try {
				Object tGetter = tClass.newInstance();
				if (tGetter instanceof AsGetMethodFactoryIf) {
					AsGetMethodFactoryIf tFactory = (AsGetMethodFactoryIf) tGetter;
					for (AsGetMethodIf<?> tGetMethod : tFactory.getGetMethods()) {
						Set<String> tSet = cGetMethods.get(tGetMethod.getItemClass());
						if (tSet == null) {
							cGetMethods.put(tGetMethod.getItemClass(), tSet = new HashSet<String>());
						}
						tSet.add(tGetMethod.getAttributeName());
					}
				}
				if (tGetter instanceof AsGetMethodIf<?>) {
					AsGetMethodIf<?> tGetMethod = (AsGetMethodIf<?>) tGetter;
					Set<String> tSet = cGetMethods.get(tGetMethod.getItemClass());
					if (tSet == null) {
						cGetMethods.put(tGetMethod.getItemClass(), tSet = new HashSet<String>());
					}
					tSet.add(tGetMethod.getAttributeName());
				}
			}
			catch (InstantiationException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		for (Element tElement : createList("/Configuration/AsMeta/AsGetMethods/AsGetMethodSource")) {
			String tType = tElement.getAttribute(ATTR_SOURCE);
			String tName = tElement.getAttribute(ATTR_NAME);
			String tField = tElement.getAttribute(ATTR_FIELD);
			ensureType(tType, tElement);
			if (!tField.isEmpty()) {
				ensureFieldExist(tType, "", tField, tElement);
			}
			for (Element tGetMethod : createList(TAG_AS_GET_METHOD, tElement)) {
				tType = tGetMethod.getAttribute(ATTR_TYPE);
				tField = tGetMethod.getAttribute(ATTR_FIELD);
				Class<?> tClass = ensureType(tType, tGetMethod);
				ensureFieldExist(tType, "", tField, tGetMethod);
				Set<String> tSet = cGetMethods.get(tClass);
				if (tSet == null) {
					cGetMethods.put(tClass, tSet = new HashSet<String>());
				}
				if (tGetMethod.getAttribute(ATTR_NAME).isEmpty()) {
					tSet.add(tName);
				}
				else {
					tSet.add(tGetMethod.getAttribute(ATTR_NAME));
				}
			}
		}
	}

	@AfterClass
	public static void after() {
		StringBuilder tBuffer = new StringBuilder();
		if (!cLoadedOk) {
			return;
		}
		System.out.println("");
		cLoadedOk = false;
		try {
			if (cErrors.isEmpty()) {
				System.out.println("Found no errors\n");
			}
			else {
				tBuffer.append(("Found " + cErrors.size() + " errors:\n"));
				Collections.sort(cErrors);
				for (String tError : cErrors) {
					tBuffer.append("  ").append(tError).append("\n");
				}
				System.out.println(tBuffer.toString());
			}
			tBuffer.setLength(0);
			if (cWarnings.isEmpty()) {
				System.out.println("Found no warnings\n");
			}
			else {
				tBuffer.append(("Found " + cWarnings.size() + " warnings:\n"));
				Collections.sort(cWarnings);
				for (String tWarning : cWarnings) {
					tBuffer.append("  ").append(tWarning).append("\n");
				}
				System.out.println(tBuffer.toString());
			}
		}
		catch (Exception e) {
			//TODO: handle io problems
		}
		if (!cErrors.isEmpty()) {
			Assert.fail();
		}
	}

	@Test
	public void testDataSources() {

		if (!cLoadedOk) {
			return;
		}
		// TODO
		// AsList/@type
		// AsList/@key
		// AsList/@text

		// test object type references
		ensureType("/Configuration/AsDataSources//AsList/@type");
		ensureType("/Configuration/AsDataSources//AsList/@factory");
		ensureType("/Configuration/AsDataSources/AsTree//*/@type");
		ensureType("/Configuration/AsMenus/contextmenus/contextmenu/@type");
		ensureType("/Configuration/AsMeta/AsGetMethods/AsGetMethodSource/@source");
		ensureType("/Configuration/AsMeta/AsGetMethods/AsGetMethodSource/AsGetMethod/@type");
		ensureType("/Configuration/AsMeta/AsGetMethods/AsGetMethod/@type");
		ensureType("/Configuration/AsMvc/view//*/@model");
		ensureType("/Configuration/AsMvc/view/context/@type");
		ensureType("/Configuration/AsMeta/MetaData/@className");

		// test data source references
		ensureDataSource("/Configuration/AsDataSources//*/@source");
		ensureDataSource("/Configuration/AsMvc/view/@dataSourceId");
		ensureDataSource("/Configuration/AsMvc/view/layout/fieldset/field/@dataSourceId");

		// Test key and text attributes
		ensureDataSourceFields("/Configuration/AsDataSources//AsList[@type]", "key", "text");

		// Test all data source factory item types
		for (Element tList : createList("/Configuration/AsDataSources//AsList[@factory]")) {
			Class<?> tFactoryClass = find(tList.getAttribute(ATTR_FACTORY));
			try {
				Method tMethod = tFactoryClass.getMethod("getItemClass");
				ParameterizedType tReturnType = (ParameterizedType) tMethod.getGenericReturnType();
				Type tType0 = tReturnType.getActualTypeArguments()[0];
				String tParameterType = ((Class<?>) tType0).getSimpleName();
				ensureType(tParameterType, tList);
			}
			catch (Exception e) {
				error("Exception while determining data source factory item type", tList);
			}
		}
	}

	private void ensureType(String pXpath) {
		List<Attr> tList = createAttrList(pXpath);
		int tCount = cErrors.size();
		System.out.print("Type: " + pXpath + " (" + tList.size() + ")");
		for (Attr tAttr : tList) {
			ensureType(tAttr.getNodeValue(), tAttr.getOwnerElement());
		}
		if (tCount != cErrors.size()) {
			System.out.print(" - " + (cErrors.size() - tCount) + " problems");
		}
		System.out.println();
	}

	private static Class<?> find(String pClassName) {
		try {
			return  Class.forName(pClassName);
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static void ensureMetaData(String pXpath) {
		List<Attr> tList = createAttrList(pXpath);
		int tCount = cErrors.size();
		System.out.print("Model: " + pXpath + " (" + tList.size() + ")");
		for (Attr tAttr : tList) {
			Class<?> tClass = ensureType(tAttr.getNodeValue(), tAttr.getOwnerElement());
			if (tClass == null || !cMetaDatas.contains(tClass.getName())) {
				error("Metadata not found: " + tClass, tAttr.getOwnerElement());
			}
		}
		if (tCount != cErrors.size()) {
			System.out.print(" - " + (cErrors.size() - tCount) + " problems");
		}
		System.out.println();
	}

	private static Class<?> ensureType(String pType, Element pSource) {
		Class<?> tType = cObjectTypes.get(pType);
		if (tType != null) {
			return tType;
		}
		if (pType.endsWith("_list")) {
			pType = pType.replace("_list", "");
			if (cAsList.containsKey(pType)) {
				tType = Object.class;
			}
		}
		if (pType.endsWith("[]")) {
			pType = pType.replace("[]", "");
		}
		if (tType == null && pType.startsWith("com.")) {
			cObjectTypes.put(pType, tType = find(pType));
		}
		else if (tType == null) {
			int tPos = pType.indexOf(":");
			String tSimpleName = pType.substring(tPos + 1);
			String tNamespace = tPos == -1 ? "" : pType.substring(0, tPos);
			for (Map.Entry<String, String> tPackage : cSearchPackages.entrySet()) {
				if (tNamespace.equals(tPackage.getValue())) {
					tType = find(tPackage.getKey() + "." + tSimpleName);
					if (tType != null) {
						cObjectTypes.put(pType, tType);
						break;
					}
				}
			}
		}
		if (tType == null) {
			error("Type not found: " + pType, pSource);
		}
		return tType;
	}

	private static void ensureFieldExist(String pType, String pPath, String pField, Element pSource) {
		Class<?> tClass = ensureType(pType, pSource);
		if (tClass == null || tClass == AsDictionaryWord.class || AsXmlRefData.class.isAssignableFrom(tClass)) {
			return;
		}
		if (pField.startsWith("_")) {
			return;
		}
		try {
			String[] tFullPath =
					((pPath.isEmpty() ? "" : pPath + ".") + pField.replaceAll("\\[\\d+\\]", "")).split("\\.");
			Class<?> tCurrent = tClass;
			for (int i = 0; i < tFullPath.length; i++) {
				if (tCurrent == Object.class) {
					return;
				}
				for (Class<?> tKey : cGetMethods.keySet()) {
					if (tKey.isAssignableFrom(tCurrent)) {
						Set<String> tSet = cGetMethods.get(tKey);
						if (tSet != null && tSet.contains(tFullPath[i])) {
							//ok
							return;
						}
					}
				}
				try {
					Field tField = tCurrent.getField(tFullPath[i]);
					tCurrent = tField.getType();
				}
				catch (NoSuchFieldException e) {
					if (isDisplayFieldByReference(tCurrent, tFullPath[i])) {
						tCurrent = String.class;
					}
					else {
						Method tMethod = tCurrent.getMethod("get"
								+ tFullPath[i].substring(0, 1).toUpperCase() + tFullPath[i].substring(1));
						tCurrent = tMethod.getReturnType();
					}
				}
				if (tCurrent.isArray()) {
					tCurrent = tCurrent.getComponentType();
				}
			}
		}
		catch (Exception e) {
			error("[" + pField + "] no such field for " + pType, pSource);
		}
	}

	private static boolean isDisplayFieldByReference(Class<?> pType, String pField) {
		String tIdField;
		if (pField.endsWith("Name")) {
			tIdField = pField.substring(0, pField.length() - 4);
		}
		else {
			tIdField = pField + "Id";
		}
		try {
			Field tField = pType.getField(tIdField);
			if (tField.getAnnotation(CwfReference.class) != null) {
				return true;
			}
		}
		catch (NoSuchFieldException | SecurityException e) {
			// continue
		}
		return false;
	}

	private static void ensureDataSource(String pXpath) {
		List<Attr> tList = createAttrList(pXpath);
		System.out.println("DataSource: " + pXpath + " count=" + tList.size());
		for (Attr tNode : tList) {
			ensureDataSource(tNode.getNodeValue(), tNode.getOwnerElement());
		}
	}

	private static void ensureDataSourceFields(String pXpath, String... pFieldNames) {
		List<Element> tList = createList(pXpath);
		for (Element tNode : tList) {
			String tType = tNode.getAttribute(ATTR_TYPE);
			Class<?> tClass = ensureType(tType, tNode);
			// Test all non-map based lists
			if (tClass != null && !AsMapRefData.class.isAssignableFrom(tClass)) {
				for (String tFieldName : pFieldNames) {
					String tField = tNode.getAttribute(tFieldName);
					if (!tField.isEmpty()) {
						String[] tParts = tField.split(",");
						for (String tPart : tParts) {
							ensureFieldExist(tType, "", tPart, tNode);
						}
					}
				}
			}
		}
	}

	private static void ensureDataSource(String pId, Element pElement) {
		if (!cAsDataSources.containsKey(pId) &&
				!cReferencedConstantGroupDataSources.contains(pId)) {
			error(pElement.getTagName() + " reference to non existing data source: " + pId, pElement);
		}
	}

	protected String getDataSourceType(String pDataSource) {
		Element tDataSource = cAsDataSources.get(pDataSource);
		if (tDataSource == null) {
			return null;
		}
		String tType = tDataSource.getAttribute(ATTR_TYPE);
		while (tType.isEmpty()) {
			if (!tDataSource.getAttribute(ATTR_SOURCE).isEmpty()) {
				tDataSource = cAsDataSources.get(tDataSource.getAttribute(ATTR_SOURCE));
			}
			else {
				tDataSource = (Element) tDataSource.getParentNode();
				if (!tDataSource.getTagName().equals(TAG_AS_LIST)) {
					break;
				}
			}
			tType = tDataSource.getAttribute(ATTR_TYPE);
		}
		return tType;
	}

	@Test
	public void testViews() throws Exception {
		if (!cLoadedOk) {
			return;
		}
		for (Element tView : createList(
				"/Configuration/AsMvc/view[@type!='table' and @type!='chart' and @type!='highchart'][display]")) {
			error(tView.getAttribute("id") + " - display: only for view of type table or chart", tView);
		}
		for (Element tView : createList("/Configuration/AsMvc/view[@dataSourceId]")) {
			String tType = getDataSourceType(tView.getAttribute(ATTR_DATASOURCE_ID));
			if (tType == null || tType.isEmpty()) {
				continue;
			}
			for (Attr tField : createAttrList("display/field/@name", tView)) {
				ensureFieldExist(tType, "", tField.getNodeValue(), tField.getOwnerElement());
			}
			for (Attr tField : createAttrList("display/field/@tooltip", tView)) {
				ensureFieldExist(tType, "", tField.getNodeValue(), tField.getOwnerElement());
			}
		}
		for (Element tView : createList("/Configuration/AsMvc/view[@model | @extends | @dataSourceId]")) {
			String tModel = tView.getAttribute(ATTR_MODEL);
			String tExtends = tView.getAttribute(ATTR_EXTENDS);
			String tType = tView.getAttribute(ATTR_TYPE);
			Element tBaseView = tView;
			while (tType.isEmpty()) {
				String tXPath = "/Configuration/AsMvc/view[@id='" + tExtends + "']";
				tBaseView = ((Element) cCachedXPathAPI.selectSingleNode(cRoot, tXPath));
				if (tBaseView == null) {
					error("Base view " + tExtends + " does not exist", tView);
					return;
				}
				if (tModel.isEmpty()) {
					tModel = tBaseView.getAttribute(ATTR_MODEL);
				}
				tExtends = tBaseView.getAttribute(ATTR_EXTENDS);
				tType = tBaseView.getAttribute(ATTR_TYPE);
			}
			// Fielsets without context
			for (Element tFieldSet : createList("layout/fieldset[not(@context)]", tView)) {
				String tPath = tFieldSet.getAttribute(ATTR_PATH);
				for (Attr tField : createAttrList("field/@name | field/@remove", tFieldSet)) {
					String tContextType = tField.getOwnerElement().getAttribute(ATTR_CONTEXT);
					if (!tContextType.isEmpty()) {
						ensureFieldExist(tContextType, "", tField.getNodeValue(), tField.getOwnerElement());
					}
					else {
						ensureFieldExist(tModel, tPath, tField.getNodeValue(), tField.getOwnerElement());
					}
				}
			}
			// Fielsets with context
			for (Element tFieldSet : createList("layout/fieldset[@context]", tView)) {
				String tContextType = tFieldSet.getAttribute(ATTR_CONTEXT);
				String tContextPath = "";
				int tPathSep = tContextType.indexOf('.');
				if (tPathSep >= 0) {
					tContextPath = tContextType.substring(tPathSep + 1);
					tContextType = tContextType.substring(0, tPathSep);
					ensureFieldExist(tContextType, "", tContextPath, tFieldSet);
				}

				for (Attr tField : createAttrList("field/@name | field/@remove", tFieldSet)) {
					ensureFieldExist(tContextType, tContextPath, tField.getNodeValue(), tField.getOwnerElement());
				}
			}
			// <context type="RtcSchedule" source="rtcScheduleId" target="pointInTime.rtcScheduleId"/>
			for (Element tContext : createList(TAG_CONTEXT, tView)) {
				String tSource = tContext.getAttribute(ATTR_SOURCE);
				if (tSource != null && !tSource.isEmpty()) {
					ensureFieldExist(tContext.getAttribute(ATTR_TYPE), "", tSource, tContext);
				}
				String tTarget = tContext.getAttribute(ATTR_TARGET);
				if (tTarget != null && !tTarget.isEmpty()) {
					if (tModel != null && !tModel.isEmpty()) {
						ensureFieldExist(tModel, "", tTarget, tContext);
					}
					else if (!"chart".equals(tType) && !"table".equals(tType)) {
						error("Target set without model for view type " + tType, tContext);
					}
				}
				// TODO: test target type = source type
			}
			// <set target="pointInTime.rtcScheduleId" value="xxx"/>
			for (Element tSet : createList(TAG_SET, tView)) {
				String tTarget = tSet.getAttribute(ATTR_TARGET);
				if (tTarget != null && !tTarget.isEmpty()) {
					if (tModel != null && !tModel.isEmpty()) {
						ensureFieldExist(tModel, "", tTarget, tSet);
					}
					else if (!"chart".equals(tType) && !"table".equals(tType)) {
						error("Target set without model for view type " + tType, tSet);
					}
				}
			}
			// <clear target="pointInTime.rtcScheduleId"/>
			for (Element tClear : createList(TAG_CLEAR, tView)) {
				String tTarget = tClear.getAttribute(ATTR_TARGET);
				if (tTarget != null && !tTarget.isEmpty()) {
					if (tModel != null && !tModel.isEmpty()) {
						ensureFieldExist(tModel, "", tTarget, tClear);
					}
					else if (!"chart".equals(tType) && !"table".equals(tType)) {
						error("Target set without model for view type " + tType, tClear);
					}
				}
			}
			// Check target view for search views
			if (tType.equals(MvcViewTypeEnum.search.name())) {
				String tTargetView = tBaseView.getAttribute(ATTR_TARGET_VIEW);
				if (!tTargetView.isEmpty() && !cViews.containsKey(tTargetView)) {
					error("Search view target reference to non existing view: " + tTargetView, tBaseView);
				}
			}
			// Check event references
			for (Element tEvent : createList("click-event/target", tView)) {
				String tTargetView = tEvent.getAttribute(ATTR_VIEW);
				if (!tTargetView.isEmpty() && !cViews.containsKey(tTargetView)) {
					error("Reference to non existing view: " + tTargetView, tEvent);
				}
			}
			for (Element tField : createList("layout/fieldset/field", tView)) {
				// Check that clearDisabled is used together with disabled
				if (!tField.getAttribute(ATTR_CLEAR_DISABLED).isEmpty() &&
						tField.getAttribute(ATTR_DISABLED).isEmpty()) {
					error("Using '" + ATTR_CLEAR_DISABLED +
							"' without '" + ATTR_DISABLED + "' is not allowed", tField);
				}
				// Check that position has a valid value
				String tPosition = tField.getAttribute(ATTR_POSITION);
				if (!tPosition.isEmpty()) {
					if (tPosition.startsWith("after:") ||
							tPosition.startsWith("before:")) {
						// TODO: Test the referenced field (tricky, involves extension)
					}
					else if (!"first".equals(tPosition)) {
						error("Invalid position value '" + tPosition + "'", tField);
					}
				}
			}
			// Check layout group field set references
			Set<String> tFieldsetIds = new HashSet<String>();
			List<Element> tViewHierarchy = getViewInheritanceHierarchy(tView);
			for (Element tView2 : tViewHierarchy) {
				tFieldsetIds.addAll(createSet("layout/fieldset/@id", tView2));
			}
			for (Element tFieldsetRef : createList("layout/group//fieldset[@ref]", tView)) {
				String tRef = tFieldsetRef.getAttribute(ATTR_REF);
				if (!tFieldsetIds.contains(tRef)) {
					error("Layout refers to non-existant field set: " + tRef, tFieldsetRef);
				}
			}
			// Check link validity
			for (Element tLink : createList("layout/fieldset/field[@link]", tView)) {
				String tTargetView = tLink.getAttribute(ATTR_LINK);
				if (!tTargetView.isEmpty() && !cViews.containsKey(tTargetView)) {
					error("Link reference to non existing view: " + tTargetView, tLink);
				}
			}
			// Search views must specify a target view
			if ("search".equals(tView.getAttribute(ATTR_TYPE)) && tView.getAttribute(ATTR_TARGET_VIEW).isEmpty()) {
				error("Search view must specify a target view", tView);
			}
		}
	}

	@Test
	public void testMenuItems() throws Exception {
		if (!cLoadedOk) {
			return;
		}
		for (Element tMenuItem : createList("/Configuration/AsMenus//menuitem[@view]")) {
			String tViewId = tMenuItem.getAttribute(ATTR_VIEW);
			if (!cViews.containsKey(tViewId)) {
				error("MenuItem reference to non existing view: " + tViewId, tMenuItem);
			}
		}
		for (Element tMenuItem : createList("/Configuration/AsMenus//menuitem[@ref]")) {
			String tMenuId = tMenuItem.getAttribute(ATTR_REF);
			if (!cMenuItemsIdMap.containsKey(tMenuId)) {
				error("MenuItem reference to non existing menu: " + tMenuId, tMenuItem);
			}
		}
		for (Element tMenu : createList("/Configuration/AsMenus//menu", "/Configuration/AsMenus//menuitem")) {
			String tPosition = tMenu.getAttribute(ATTR_POSITION);
			if (!tPosition.isEmpty()) {
				if (tPosition.startsWith("after:") ||
						tPosition.startsWith("before:")) {
					// TODO: Test the referenced field (tricky, involves extension)
				}
				else if (!"first".equals(tPosition)) {
					error("Invalid position value '" + tPosition + "'", tMenu);
				}
			}
		}
	}

	@Test
	public void testPerspectiveDefinitions() throws Exception {
		if (!cLoadedOk) {
			return;
		}
		for (Element tSlotReference : createList(
				"/Configuration/AsPerspectives/perspective//slot",
				"/Configuration/AsPerspectives/slot-template",
				"/Configuration/AsPerspectives/slot-template//slot")) {
			String tDisplayMode = tSlotReference.getAttribute(ATTR_DISPLAY_MODE);
			String tFixed = tSlotReference.getAttribute(ATTR_FIXED);
			String tColumns = tSlotReference.getAttribute(ATTR_COLUMNS);
			String tColumnSpacing = tSlotReference.getAttribute(ATTR_COLUMN_SPACING);
			String tTemplate = tSlotReference.getAttribute(ATTR_TEMPLATE);
			if (!tDisplayMode.equals("grid") && (!tColumns.isEmpty() || !tColumnSpacing.isEmpty())) {
				error("Use of columns or columnSpacing is restricted to displayMode 'grid'", tSlotReference);
			}
			if (!tFixed.isEmpty() && !"tab|split".contains(tDisplayMode)) {
				error("Use of fixed is restricted to display modes 'tab' and 'split'", tSlotReference);
			}
			if (!tTemplate.isEmpty() && !cSlotTemplates.containsKey(tTemplate)) {
				error("Slot references non-existant slot template: " + tTemplate, tSlotReference);
			}
		}
	}

	@Test
	public void testPerspectiveViewReferences() throws Exception {
		if (!cLoadedOk) {
			return;
		}
		for (Element tViewReference : createList("/Configuration/AsPerspectives/perspective//view[@id]")) {
			String tViewId = tViewReference.getAttribute(ATTR_ID);
			if (!cViews.containsKey(tViewId)) {
				error("Perspective reference to non existing view: " + tViewId, tViewReference);
			}
		}
	}

	@Test
	public void testPerspectiveModifications() throws Exception {
		if (!cLoadedOk) {
			return;
		}
		for (Element tPerspectiveRemoval : createList("/Configuration/AsPerspectives//perspective[@remove]")) {
			String tPerspectiveId = tPerspectiveRemoval.getAttribute(ATTR_REMOVE);
			if (!cPerspectives.containsKey(tPerspectiveId)) {
				error("Perspective removal references non existing perspective: " +
						tPerspectiveId, tPerspectiveRemoval);
			}
		}
		for (Element tPerspectiveModification : createList("/Configuration/AsPerspectives//perspective[@modify]")) {
			String tPerspectiveId = tPerspectiveModification.getAttribute(ATTR_MODIFY);
			if (!cPerspectives.containsKey(tPerspectiveId)) {
				error("Perspective modification references non existing perspective: " +
						tPerspectiveId, tPerspectiveModification);
			}
		}
	}

	String getModule(Element pElement) {
		String tModule = pElement.getAttribute("module");
		if (tModule.length() > 0) {
			return tModule;
		}
		return getModule((Element) pElement.getParentNode());
	}
	String t(Element pElement) {
		return getModule(pElement) + "... " + pElement.getTagName();
	}

	@Test
	public void testTableViews() throws Exception {
		// Make sure that sort references an existing field in the viewport
		// Also make sure that field is not editable
		for (Element tView : createList("/Configuration/AsMvc/view[@type='table']")) {
			for (Element tSort : createList(TAG_SORT, tView)) {
				String tSortField = tSort.getAttribute(ATTR_FIELD);
				if (!createList("display", tView).isEmpty()) {
					List<Element> tFields = createList(
							"display[@type='table' or @type='editable']/field[@name='" + tSortField + "']", tView);
					if (tFields.isEmpty()) {
						error("Sort references non existing field: " + tSortField, tSort);
					}
					List<Element> tEditableFields = createList(
							"display[@type='editable']/field[@name='" + tSortField + "' and not(@readOnly)]", tView);
					if (!tEditableFields.isEmpty()) {
						error("Sort references editable field: " + tSortField, tSort);
					}
				}
			}
		}
		// Check that addView is only used for editable displays
		for (Element tDisplay : createList(
				"/Configuration/AsMvc/view[@type='table']/display[@type!='editable'][@addView]")) {
			error("Add view is only intended for editable tables", tDisplay);
		}
		// Check that addView points to a valid view
		for (Element tDisplay : createList(
				"/Configuration/AsMvc/view[@type='table']/display[@type='editable'][@addView]")) {
			String tAddViewId = tDisplay.getAttribute(ATTR_ADD_VIEW);
			Element tAddView = cViews.get(tAddViewId);
			if (tAddView == null) {
				error("Add view references a nonexistant view: " + tAddViewId, tDisplay);
			}
			else if (!"form".equals(tAddView.getAttribute(ATTR_TYPE))) {
				error("Add view is not a form: " + tAddViewId, tDisplay);
			}
		}
		// Test nested views
		for (Element tNestedViews : createList("/Configuration/AsMvc/view/nested-views")) {
			// Check that ref points to a valid view
			for (Element tView : createList("view[(@ref) or (@remove) or (@modify)]", tNestedViews)) {
				String tRef = tView.getAttribute(ATTR_REF);
				String tModify = tView.getAttribute(ATTR_MODIFY);
				String tRemove = tView.getAttribute(ATTR_REMOVE);
				if (!tRef.isEmpty() && (!tModify.isEmpty() || !tRemove.isEmpty()) ||
						!tModify.isEmpty() && (!tRef.isEmpty() || !tRemove.isEmpty()) ||
						!tRemove.isEmpty() && (!tRef.isEmpty() || !tModify.isEmpty())) {
					error("Only one of 'ref', 'modify' or 'remove' may be set", tView);
				}
				if (!tRef.isEmpty() && !cViews.containsKey(tRef)) {
					error("Invalid nested view reference: " + tRef, tView);
				}
				if (!tModify.isEmpty() && !cViews.containsKey(tModify)) {
					error("Invalid nested view modification: " + tRef, tView);
				}
				if (!tRemove.isEmpty() && !cViews.containsKey(tRemove)) {
					error("Invalid nested view removal: " + tRemove, tView);
				}
				// Check modify validity, must exist in modified or extended view
				if (!tModify.isEmpty()) {
					Element tParent = (Element) tNestedViews.getParentNode();
					tModify = tParent.getAttribute(ATTR_MODIFY);
					String tExtends = tParent.getAttribute(ATTR_EXTENDS);
					if (tModify.isEmpty() && tExtends.isEmpty()) {
						error("Cannot modify nested views in a base view definition", tView);
					}
				}
				// Check remove validity, must exist in modified or extended view
				if (!tRemove.isEmpty()) {
					Element tParent = (Element) tNestedViews.getParentNode();
					tRemove = tParent.getAttribute(ATTR_MODIFY);
					String tExtends = tParent.getAttribute(ATTR_EXTENDS);
					if (tRemove.isEmpty() && tExtends.isEmpty()) {
						error("Cannot remove nested views in a base view definition", tView);
					}
				}
				// TODO: Modify and remove should check that the view exists somewhere in the inheritance chain
				// This is however far from trivial...
			}
		}
		// Test fields in extended table views
		for (Element tView : createList("/Configuration/AsMvc/view[@extends]")) {
			Set<String> tFields = new HashSet<String>();
			List<Element> tViewHierarchy = getViewInheritanceHierarchy(tView);
			String tBaseType = tViewHierarchy.get(tViewHierarchy.size() - 1).getAttribute(ATTR_TYPE);
			String tBaseDataSourceId = tViewHierarchy.get(tViewHierarchy.size() - 1).getAttribute(ATTR_DATASOURCE_ID);
			if (MvcViewTypeEnum.table.name().equals(tBaseType)) {
				String tType = getDataSourceType(tBaseDataSourceId);
				for (Element tView2 : tViewHierarchy) {
					tFields.addAll(createSet("display/field/@name", tView2));
				}
				for (String tField : tFields) {
					ensureFieldExist(tType, "", tField, tView);
				}
			}
		}

	}

	@Test
	public void testNumericPatterns() {
		for (Map.Entry<String, Element> tEntry : cNumericPatterns.entrySet()) {
			String tPattern = tEntry.getValue().getAttribute(ATTR_VALUE);
			try {
				new DecimalFormat(tPattern);
			}
			catch (IllegalArgumentException e) {
				error(e.getMessage(), tEntry.getValue());
			}
		}
	}

	@Test
	public void testServicePlugins() {
		for (Element tPlugin : createList("/Configuration/AsPlugins/Plugin")) {
			String tPluginClass = tPlugin.getAttribute("pluginClass");
			if (tPluginClass.isEmpty()) {
				tPluginClass = tPlugin.getAttribute(ATTR_REMOVE);
			}
			if (!tPluginClass.isEmpty()) {
				try {
					Class.forName(tPluginClass);
				}
				catch (Exception e) {
					error("Service plug-in class not found: " + tPluginClass, tPlugin);
				}
			}
			else {
				error("Service plug-in class not specified", tPlugin);
			}
		}
	}

}
