package com.cinnober.ciguan.conf.base;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.CachedXPathAPI;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigTestUtil {

	public static Map<Class<?>, Set<Class<?>>> cValidAssignments = new HashMap<Class<?>, Set<Class<?>>>();
	public static Map<Class<?>, Class<?>> cPrimitives = new HashMap<Class<?>, Class<?>>();
	private static CachedXPathAPI cCachedXPathAPI = new CachedXPathAPI();
	private static Map<String, Class<?>> cTapTypeMap;
	private static List<String> cSearchPackages;

	static {
		cPrimitives.put(int.class, Integer.class);
		cPrimitives.put(long.class, Long.class);
		cPrimitives.put(double.class, Double.class);
		cPrimitives.put(float.class, Float.class);
		cPrimitives.put(short.class, Short.class);
		cPrimitives.put(boolean.class, Boolean.class);
		cPrimitives.put(char.class, Character.class);
		cPrimitives.put(byte.class, Byte.class);

		// Copying from numeric types to String is always valid
		HashSet<Class<?>> tSourceTypes = new HashSet<Class<?>>();
		tSourceTypes.add(int.class);
		tSourceTypes.add(Integer.class);
		tSourceTypes.add(long.class);
		tSourceTypes.add(Long.class);
		tSourceTypes.add(float.class);
		tSourceTypes.add(Float.class);
		tSourceTypes.add(double.class);
		tSourceTypes.add(Double.class);
		cValidAssignments.put(String.class, tSourceTypes);
	}

	public static List<String> getAccDefinitions() throws Exception {
		InputStream tStream = getResourceAsStream("/com/cinnober/ciguan/conf/ASTransportConfig.xml");
		Document tDocument = getDocument(tStream);
		NodeList tElem = cCachedXPathAPI.selectNodeList(tDocument.getDocumentElement(), "//AccDefinitions");
		List<String> tAccDefinitins = new ArrayList<String>();
		for (int i = 0; i < tElem.getLength(); i++) {
			Node tNode = tElem.item(i);
			if (tNode.getAttributes().getNamedItem("className") != null) {
				tAccDefinitins.add(tNode.getAttributes().getNamedItem("className").getNodeValue());
			}
		}
		return tAccDefinitins;
	}

	public static Document getDocument(InputStream pStream) throws Exception {
		DocumentBuilderFactory tFactory = DocumentBuilderFactory.newInstance();
		tFactory.setIgnoringComments(true);
		tFactory.setCoalescing(true); // Convert CDATA to Text nodes
		tFactory.setNamespaceAware(false); // No namespaces: this is default
		tFactory.setValidating(false); // Don't validate DTD: also default

		DocumentBuilder tParser = tFactory.newDocumentBuilder();

		return tParser.parse(pStream);
	}

	public static Class<?> getTapClass(String pClassShortName) throws Exception {

		if (cTapTypeMap == null) {
			cTapTypeMap = new HashMap<String, Class<?>>();
			cSearchPackages = new ArrayList<String>();
			InputStream tStream = getResourceAsStream("/com/cinnober/as/conf/ASTransportConfig.xml");
			Document tDocument = getDocument(tStream);
			NodeList tNodes = XPathAPI.selectNodeList(tDocument.getDocumentElement(), "//SearchPackage");
			for (int i = 0; i < tNodes.getLength(); i++) {
				Element tNode = (Element) tNodes.item(i);
				cSearchPackages.add(tNode.getAttribute("packageName"));
			}
			// Last resort, try some standard Java packages
			cSearchPackages.add("java.lang");
			cSearchPackages.add("java.util");
		}
		if (cTapTypeMap.containsKey(pClassShortName)) {
			return cTapTypeMap.get(pClassShortName);
		}
		Class<?> tClass = null;
		for (String tPackage : cSearchPackages) {
			try {
				tClass = Class.forName(tPackage + "." + pClassShortName);
				cTapTypeMap.put(pClassShortName, tClass);
				return tClass;
			}
			catch (ClassNotFoundException e) {
				// No action
			}
		}
		// no class found!
		cTapTypeMap.put(pClassShortName, null);
		return null;
	}

	public static boolean isSameClass(Class<?> pClass1, Class<?> pClass2) {
		if (pClass1.isPrimitive() || pClass2.isPrimitive()) {
			if (pClass1.isPrimitive() && pClass2.isPrimitive()) {
				return pClass1.equals(pClass2);
			}
			if (pClass1.isPrimitive()) {
				return cPrimitives.get(pClass1).equals(pClass2);
			}
			if (pClass2.isPrimitive()) {
				return cPrimitives.get(pClass2).equals(pClass1);
			}

		}
		return pClass1.equals(pClass2);
	}

	/**
	 * Attempt to load a resource from classpath
	 * Note that global resources should not start with "/", so strip it from the input
	 * @param pResource
	 * @return
	 */
	public static InputStream getResourceAsStream(String pResource) {
		String tResource = (pResource.startsWith("/") ? pResource.substring(1) : pResource);
		InputStream tStream = ConfigTestUtil.class.getClassLoader().getResourceAsStream(tResource);
		if (tStream != null) {
			return tStream;
		}
		return ClassLoader.getSystemResourceAsStream(tResource);
	}

	/**
	 * Check if an attempt to assign an attribute of one type to an attribute of another type
	 * is valid or not.
	 * @param pSource
	 * @param pTarget
	 * @return
	 */
	public static boolean isValidAssignment(Class<?> pSource, Class<?> pTarget) {
		// Check validity tables first
		Set<Class<?>> tSourceTypes = cValidAssignments.get(pTarget);
		if (tSourceTypes != null && tSourceTypes.contains(pSource)) {
			return true;
		}

		// Default, do a hard type check
		return isSameClass(pSource, pTarget);
	}

	public static boolean isValidAccService(String pAccService) throws Exception {
		// Empty string is ok, but print a warning since it should only be used
		// for views accessible when not logged in
		if (pAccService != null && pAccService.length() == 0) {
			System.err.println("WARNING! Empty ACC service specified, please review your configuration");
			return true;
		}

		Class<?> tClass = null;
		boolean tHasAttribute = false;
		// Create the object in attribute model
		for (String tClassName : ConfigTestUtil.getAccDefinitions()) {
			try {
				tClass = Class.forName(tClassName);
				Field tField = tClass.getField("ACC_" + pAccService);
				if (tField != null) {
					tHasAttribute = true;
					break;
				}
			}
			catch (Exception e) {
				// Empty By Design
			}
		}
		if (!tHasAttribute) {
			return false;
		}
		return true;
	}

}
