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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cinnober.ciguan.AsConfigXmlParserIf;
import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.AsModifyViewNotFoundException;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.data.AsContextMenu;
import com.cinnober.ciguan.data.AsDataSourceDef;
import com.cinnober.ciguan.data.AsDisplayDefinition;
import com.cinnober.ciguan.data.AsLocale;
import com.cinnober.ciguan.data.AsMenuItem;
import com.cinnober.ciguan.data.AsPerspectiveData;
import com.cinnober.ciguan.data.AsPluginInfo;
import com.cinnober.ciguan.data.AsViewDefinition;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.datasource.listtree.AsListTreeDefinition;
import com.cinnober.ciguan.datasource.owner.AsGlobalDataSources;
import com.cinnober.ciguan.datasource.tree.AsTreeData;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.impl.AsComponent;
import com.cinnober.ciguan.impl.AsUtil;

/**
 * Implementation of configuration XML parsing.
 */
public class AsConfigXmlParser extends AsComponent implements AsConfigXmlParserIf {

    protected RolesMap mRoles;
    protected final Map<String, AsLocale> mLocales = new HashMap<String, AsLocale>();
    protected final List<AsMenuItem> mMenuItems = new ArrayList<AsMenuItem>();
    protected final List<CwfDataIf> mContextMenus = new ArrayList<CwfDataIf>();
    protected final List<CwfDataIf> mCwfPluginModules = new ArrayList<CwfDataIf>();
    protected List<CwfDataIf> mAsTrees;
    protected List<CwfDataIf> mAsListTrees;
    protected List<CwfDataIf> mAsGetMethods;
    protected final Map<String, CwfDataIf> mPerspectives = new LinkedHashMap<String, CwfDataIf>();
    protected final Map<String, CwfDataIf> mSlotTemplates = new LinkedHashMap<String, CwfDataIf>();
    protected final Map<String, AsViewDefinition> mMvcViews = new LinkedHashMap<String, AsViewDefinition>();
    protected final Map<String, AsDisplayDefinition> mMvcDisplays = new LinkedHashMap<String, AsDisplayDefinition>();
    protected String mLoadModule;
    protected Element mConfigurationDocument;
    protected Map<String, String> mAsParameters = new HashMap<String, String>();
    protected Set<String> mAsComponents = new HashSet<String>();

    /**
     * Instantiates a new XML configuration parser.
     *
     * @param pModuleName the module name to parse
     * @throws AsInitializationException in case an error occurred
     *         during the initialization of the XML configuration parser.
     */
    public AsConfigXmlParser(String pModuleName) throws AsInitializationException {
        mLoadModule = pModuleName;
        parse();
    }

    /**
     * Parse AS parameters.
     *
     * @param pNode the node to parse from
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    public void parseAsParameters(Element pNode) throws AsInitializationException {
        NodeList tNodeList = getAllNodesByName(pNode, TAG_AS_PARAMETERS);
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            CwfDataIf tTree = AsUtil.parse((Element) tNode);
            for (CwfDataIf tParameter : tTree.getObjectList(TAG_PARAMETER)) {
                String tParameterName = tParameter.getProperty(ATTR_NAME);
                String tParameterValue = tParameter.getProperty(ATTR_VALUE);
                mAsParameters.put(tParameterName, tParameterValue);
            }
        }
    }

    /**
     * Parse AS components.
     *
     * @param pNode the node to parse from
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    public void parseAsComponents(Element pNode) throws AsInitializationException {
        NodeList tNodeList = getAllNodesByName(pNode, TAG_AS_COMPONENTS);
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            CwfDataIf tTree = AsUtil.parse((Element) tNode);
            for (CwfDataIf tComponent : tTree.getObjectList(TAG_COMPONENT)) {
                String tClassName = tComponent.getProperty(ATTR_CLASSNAME);
                mAsComponents.add(tClassName);
            }
        }
    }

    /**
     * Parse the CWF plugin configuration.
     *
     * @param pNode the node to parse from
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    protected void parseCwfPluginConfiguration(Element pNode) throws AsInitializationException {
        NodeList tNodeList = getAllNodesByName(pNode, TAG_CWF_PLUGIN);
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            CwfDataIf tPluginInfo = AsUtil.parse((Element) tNode);
            mCwfPluginModules.add(tPluginInfo);
        }
    }

    /**
     * Parse the perspective XML data.
     *
     * @param pNode the node to parse from
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    protected void parsePerspectiveConfiguration(Element pNode) throws AsInitializationException {
        mRoles = new RolesMap();
        NodeList tNodeList = getAllNodesByName(pNode, TAG_AS_PERSPECTIVES);
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            CwfDataIf tTree = AsUtil.parse((Element) tNode);
            for (CwfDataIf tPerspective : tTree.getObjectList(TAG_PERSPECTIVE)) {
                if (tPerspective.getProperty(ATTR_REMOVE) != null) {
                    mPerspectives.remove(tPerspective.getProperty(ATTR_REMOVE));
                }
                else if (tPerspective.getProperty(ATTR_MODIFY) != null) {
                    CwfDataIf tCurrentPerspective = mPerspectives.get(tPerspective.getProperty(ATTR_MODIFY));
                    modifyPerspective(tCurrentPerspective, tPerspective);
                }
                else {
                    mPerspectives.put(tPerspective.getProperty(ATTR_ID), tPerspective);
                }
            }
            mRoles.addAll(tTree.getObjectList(TAG_ROLE));

            // Perspective slot templates
            for (CwfDataIf tSlotTemplate : tTree.getObjectList(TAG_SLOT_TEMPLATE)) {
                mSlotTemplates.put(tSlotTemplate.getProperty(ATTR_ID), tSlotTemplate);
            }
        }
    }

    /**
     * Modify perspective.
     *
     * @param pPerspective the perspective to apply modifications to
     * @param pPerspectiveModifications the perspective modifications
     */
    protected void modifyPerspective(CwfDataIf pPerspective, CwfDataIf pPerspectiveModifications) {
        if (pPerspective == null) {
            return;
        }
        for (CwfDataIf tSlot : pPerspectiveModifications.getObjectList(TAG_SLOT)) {
            CwfDataIf tExistingSlot = CwfDataFactory.findObject(pPerspective, TAG_SLOT, ATTR_ID, tSlot.getProperty(ATTR_REF));
            if (tExistingSlot != null) {
                for (CwfDataIf tView : tSlot.getObjectList(TAG_VIEW)) {
                    String tName = tView.getProperty(ATTR_ID);
                    if (tName == null) {
                        tName = tView.getProperty(ATTR_REMOVE);
                    }
                    if (tName != null) {
                        if (tView.getProperty(ATTR_REMOVE) != null) {
                            for (CwfDataIf tExistingView : tExistingSlot.getObjectList(TAG_VIEW)) {
                                if (tName.equals(tExistingView.getProperty(ATTR_ID))) {
                                    tExistingSlot.removeObject(TAG_VIEW, tExistingView);
                                }
                            }
                        }
                        else {
                            tExistingSlot.addObject(TAG_VIEW, tView);
                        }
                    }
                }
            }
        }
    }

    /**
     * Parse the menu XML data.
     *
     * @param pNode the node to parse from
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    @SuppressWarnings("deprecation")
    protected void parseMenuConfiguration(Element pNode) throws AsInitializationException {

        NodeList tNodeList = getAllNodesByName(pNode, TAG_AS_MENUS);
        HashMap<String, CwfDataIf> tIdMap = new HashMap<String, CwfDataIf>();
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            AsUtil.makeIdMap(tNode, tIdMap);
        }
        ContextMenuMap tMap = new ContextMenuMap();
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            CwfDataIf tTree = AsUtil.parseAndReplaceRef((Element) tNode, tIdMap);
            for (CwfDataIf tMenuItems : tTree.getObjectList(TAG_MENU_ITEMS)) {
                for (CwfDataIf tItem : tMenuItems.getObjectList(TAG_MENUITEM)) {
                    mMenuItems.add(new AsMenuItem(tItem));
                }
            }
            for (CwfDataIf tData : tTree.getObjectList(TAG_CONTEXT_MENUS)) {
                tMap.addAll(tData.getObjectList(TAG_CONTEXT_MENU));
            }
        }
        mContextMenus.addAll(tMap.values());
    }

    /**
     * Parse the MVC configuration.
     *
     * @param pNode the node to parse from
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    protected void parseMvcConfiguration(Element pNode) throws AsInitializationException {
        NodeList tNodeList = getAllNodesByName(pNode, TAG_AS_MVC);
        List<CwfDataIf> tExtendedViews = new ArrayList<CwfDataIf>();
        List<CwfDataIf> tModifyViews = new ArrayList<CwfDataIf>();
        List<CwfDataIf> tModifyExtendedViews = new ArrayList<CwfDataIf>();
        List<CwfDataIf> tRemoveViews = new ArrayList<CwfDataIf>();

        for (int i = 0; i < tNodeList.getLength(); i++) {
            Element tNode = (Element) tNodeList.item(i);
            String tModule = tNode.getAttribute(ATTR_MODULE);
            CwfDataIf tTree = AsUtil.parse(tNode);
            for (CwfDataIf tViewData : tTree.getObjectList(TAG_VIEW)) {
                tViewData.setProperty(ATTR_MODULE, tModule);
                if (tViewData.getProperty(ATTR_EXTENDS) != null) {
                    tExtendedViews.add(tViewData);
                }
                else if (tViewData.getProperty(ATTR_MODIFY) != null) {
                    tModifyViews.add(tViewData);
                }
                else if (tViewData.getProperty(ATTR_REMOVE) != null) {
                    tRemoveViews.add(tViewData);
                }
                else {
                    // if tree definition inside view definition, create data
                    // source definition for the tree
                    List<CwfDataIf> tTreeDef = tViewData.getObjectList(TAG_AS_TREE);
                    if (!tTreeDef.isEmpty()) {
                        String tDataSourceId = tViewData.getProperty(ATTR_ID);
                        tViewData.setProperty(ATTR_DATASOURCE_ID, tDataSourceId);
                        tTreeDef.get(0).setProperty(ATTR_ID, tDataSourceId);
                        mAsTrees.add(tTreeDef.get(0));
                    }
                    tTreeDef = tViewData.getObjectList(TAG_AS_LIST_TREE);
                    if (!tTreeDef.isEmpty()) {
                        String tDataSourceId = tViewData.getProperty(ATTR_ID);
                        tViewData.setProperty(ATTR_DATASOURCE_ID, tDataSourceId);
                        tTreeDef.get(0).setProperty(ATTR_ID, tDataSourceId);
                        mAsListTrees.add(tTreeDef.get(0));
                    }
                    AsViewDefinition tViewDef = new AsViewDefinition(tViewData, mMvcViews);
                    mMvcViews.put(tViewDef.getId(), tViewDef);
                }
            }
            for (CwfDataIf tDisplayData : tTree.getObjectList(TAG_DISPLAY)) {
                AsDisplayDefinition tDisplayDef = new AsDisplayDefinition(tDisplayData);
                mMvcDisplays.put(tDisplayDef.getId(), tDisplayDef);
            }
        }
        // 1) Modify views after all views are loaded
        for (CwfDataIf tViewData : tModifyViews) {
            try {
                AsViewDefinition tViewDef = new AsViewDefinition(tViewData, mMvcViews);
                mMvcViews.put(tViewDef.getId(), tViewDef);
            }
            catch (AsModifyViewNotFoundException e) {
                // Retry after processing view extensions
                tModifyExtendedViews.add(tViewData);
            }
        }
        // 2) Extend views after modify views is done
        for (CwfDataIf tViewData : tExtendedViews) {
            AsViewDefinition tViewDef = new AsViewDefinition(tViewData, mMvcViews);
            mMvcViews.put(tViewDef.getId(), tViewDef);
        }
        // 3) Modify views after processing view extensions
        for (CwfDataIf tViewData : tModifyExtendedViews) {
            AsViewDefinition tViewDef = new AsViewDefinition(tViewData, mMvcViews);
            mMvcViews.put(tViewDef.getId(), tViewDef);
        }
        // 4) Remove views after modifications and extensions are done
        for (CwfDataIf tViewData : tRemoveViews) {
            mMvcViews.remove(tViewData.getProperty(ATTR_REMOVE));
        }
    }

    /**
     * Parse the locale configuration.
     *
     * @param pNode the node to parse from
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    protected void parseLocales(Element pNode) throws AsInitializationException {
        AsLocale tDefaultPatterns = null;

        NodeList tNodeList = getAllNodesByName(pNode, "AsLocales/" + TAG_AS_DEFAULTPATTERNS);
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            CwfDataIf tData = AsUtil.parse((Element) tNode);
            if (tDefaultPatterns == null) {
                tDefaultPatterns = new AsLocale(tData);
            }
            else {
                tDefaultPatterns = new AsLocale(tData, tDefaultPatterns.patterns);
            }
        }

        tNodeList = getAllNodesByName(pNode, "AsLocales/" + TAG_AS_LOCALE);
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            CwfDataIf tData = AsUtil.parse((Element) tNode);
            if (tData.getProperty(ATTR_REMOVE) != null) {
                mLocales.remove(tData.getProperty(ATTR_REMOVE));
            }
            else {
                AsLocale tLocale = new AsLocale(tData, tDefaultPatterns.patterns);
                mLocales.put(tLocale.id, tLocale);
            }
        }
    }

    /**
     * Parse data source XML data.
     *
     * @param pNode the node to parse from
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    protected void parseDataSourceConfiguration(Element pNode) throws AsInitializationException {
        mAsTrees = new ArrayList<CwfDataIf>();
        mAsListTrees = new ArrayList<CwfDataIf>();
        NodeList tNodeList = getAllNodesByName(pNode, TAG_AS_DATASOURCES);
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            CwfDataIf tTree = AsUtil.parse((Element) tNode);
            mAsTrees.addAll(tTree.getObjectList(TAG_AS_TREE));
            mAsListTrees.addAll(tTree.getObjectList(TAG_AS_LIST_TREE));
            parseDataSourceRecursive(null, tTree);
        }
    }

    /**
     * Parses the data source recursive.
     *
     * @param <T> the generic type
     * @param pParent the parent data source definition
     * @param pNode the node
     */
    protected <T> void parseDataSourceRecursive(AsDataSourceDef<T> pParent, CwfDataIf pNode) {
        for (CwfDataIf tData : pNode.getObjectList(TAG_AS_LIST)) {
            String tModifyRef = tData.getProperty(ATTR_MODIFY);
            String tRemoveRef = tData.getProperty(ATTR_REMOVE);
            if (tRemoveRef != null) {
                // Data source definition removed
                AsDataSourceDef.remove(tRemoveRef);
            }
            else if (tModifyRef != null) {
                // Data source definition redefined
                AsDataSourceDef.update(tModifyRef, tData);
            }
            else {
                // Normal definition, process
                AsDataSourceDef<T> tDef = new AsDataSourceDef<T>(pParent, tData);
                parseDataSourceRecursive(tDef, tData);
            }
        }
    }

    /**
     * Parse get methods XML data.
     *
     * @param pNode the node to parse from
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    protected void parseGetMethodsConfiguration(Element pNode) throws AsInitializationException {
        mAsGetMethods = new ArrayList<CwfDataIf>();
        NodeList tNodeList = getAllNodesByName(pNode, "AsMeta/" + TAG_AS_GET_METHODS);
        for (int i = 0; i < tNodeList.getLength(); i++) {
            Node tNode = tNodeList.item(i);
            CwfDataIf tTree = AsUtil.parse((Element) tNode);
            mAsGetMethods.addAll(tTree.getObjectList(TAG_AS_GET_METHOD));
            mAsGetMethods.addAll(tTree.getObjectList(TAG_AS_GET_METHOD_SOURCE));
        }
    }

    /**
     * Selects nodes by name.
     *
     * @param pNode the node to parse from
     * @param pTagName the tag name to select
     * @return a list of nodes matching the given name
     * @throws AsInitializationException in case an exception occurred
     *         during the parsing of the specified element.
     */
    protected NodeList getAllNodesByName(Element pNode, String pTagName) throws AsInitializationException {
        try {
            // no xml name space
            return XPathAPI.selectNodeList(pNode, pTagName);
        }
        catch (TransformerException e) {
            throw new AsInitializationException("Error selecting " + pTagName + " source node", e);
        }
    }

    @Override
    public void allComponentsStarted() throws AsInitializationException {
        // Post-adjustment of data source types
        for (AsDataSourceDef<?> tDef : AsDataSourceDef.getAll()) {
            if (tDef.getType().startsWith("@")) {
                Class<Object> tClass = As.getMetaDataHandler().getType(tDef.getType().substring(1));
                String tType = As.getTypeName(tClass);
                tDef.getValues().setProperty(ATTR_TYPE, tType);
            }
        }
        for (AsViewDefinition tDef : mMvcViews.values()) {
            String tType = tDef.getValues().getProperty(ATTR_DATASOURCE_TYPE);
            if (tType != null && tType.startsWith("@")) {
                Class<Object> tClass = As.getMetaDataHandler().getType(tType.substring(1));
                tType = As.getTypeName(tClass);
                tDef.getValues().setProperty(ATTR_DATASOURCE_TYPE, tType);
            }
        }

        // Submit the parsed data to its destinations
        As.getGlobalDataSources().addDataSourceDefinitions(AsDataSourceDef.getAll());
        submitMenuConfiguration();
        submitPerspectiveConfiguration();
        submitDataSourceConfiguration();
        submitMvcConfiguration();
        submitPluginConfiguration();
        submitLocaleConfiguration();
        super.allComponentsStarted();
    }

    /**
     * Submit plugin configuration.
     */
    protected void submitPluginConfiguration() {
        for (CwfDataIf tData : mCwfPluginModules) {
            As.getBdxHandler().broadcast(new AsPluginInfo(tData));
        }
    }

    /**
     * Submit locale configuration.
     */
    protected void submitLocaleConfiguration() {
        for (AsLocale tLocale : mLocales.values()) {
            As.getBdxHandler().broadcast(tLocale);
        }
    }

    @Override
    public Collection<AsLocale> getLocales() {
        return mLocales.values();
    }

    @Override
    public Collection<String> getConfiguredComponents() {
        return mAsComponents;
    }

    /**
     * Submit the menu configuration.
     */
    protected void submitMenuConfiguration() {
        for (AsMenuItem tMenu : mMenuItems) {
            As.getBdxHandler().broadcast(tMenu);
        }
        for (CwfDataIf tData : mContextMenus) {
            As.getBdxHandler().broadcast(new AsContextMenu(tData));
        }

    }

    /**
     * Submit the mvc configuration.
     */
    protected void submitMvcConfiguration() {
        for (AsViewDefinition tViewDef : mMvcViews.values()) {
            As.getBdxHandler().broadcast(tViewDef);
            AsMenuItem tViewMenu = tViewDef.getMenuItem();
            if (tViewMenu != null) {
                As.getBdxHandler().broadcast(tViewMenu);
            }
        }
        for (AsDisplayDefinition tDisplayDef : mMvcDisplays.values()) {
            As.getBdxHandler().broadcast(tDisplayDef);
        }
    }

    /**
     * Submit the perspective configuration.
     * @throws AsInitializationException if the perspective configuration has errors
     */
    protected void submitPerspectiveConfiguration() throws AsInitializationException {
        for (CwfDataIf tData : mPerspectives.values()) {
            As.getBdxHandler().broadcast(
                new AsPerspectiveData(tData, new ArrayList<CwfDataIf>(mRoles.values()), mSlotTemplates));
        }
    }

    /**
     * Submit the data source configuration.
     */
    protected void submitDataSourceConfiguration() {
        for (CwfDataIf tData : mAsTrees) {
            As.getBdxHandler().broadcast(new AsTreeData(tData));
        }
        for (CwfDataIf tData : mAsListTrees) {
            As.getBdxHandler().broadcast(new AsListTreeDefinition(tData));
        }
        for (AsDataSourceDef<?> tData : AsDataSourceDef.getAll()) {
            As.getBdxHandler().broadcast(tData);
        }
    }

    /**
     * Get the ID of the element.
     *
     * @param pElement the element from which to retrieve the ID
     * @return the ID of the element
     */
    protected String getElementId(final Element pElement) {
        NamedNodeMap tAttributes = pElement.getAttributes();
        Node tNode = tAttributes.getNamedItem(ATTR_ID);
        return (tNode != null ? tNode.getNodeValue() : null);
    }

    /**
     * Serialize the given node as a string.
     *
     * @param pNode the node to serialize
     * @return a serialized string of the node
     */
    protected String xmlToString(Node pNode) {
        try {
            Source tSource = new DOMSource(pNode);
            StringWriter tStringWriter = new StringWriter();
            Result tResult = new StreamResult(tStringWriter);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tTransformer = tFactory.newTransformer();
            tTransformer.transform(tSource, tResult);

            return tStringWriter.getBuffer().toString();
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Transformer configuration exception", e);
        }
        catch (TransformerException e) {
            throw new RuntimeException("Exception while transforming XML document to string", e);
        }
    }

    /**
     * Parses the XML configuration.
     *
     * @throws AsInitializationException when no load module is specified, or when parse errors occur
     */
    protected void parse() throws AsInitializationException {

        // Sanity check
        if (mLoadModule == null || mLoadModule.isEmpty()) {
            throw new AsInitializationException("No configuration module configured, aborting startup");
        }

        try {
            // AsLoggerIf.Singleton.get().log("Loading configuration from module '"
            // + mLoadModule + "'");
            mConfigurationDocument = AsXmlTool.loadCwfModule(mLoadModule).getDocumentElement();
        }
        catch (Exception e) {
            throw new AsInitializationException("Exception when loading module " + mLoadModule, e);
        }

        // Go ahead with the load process
        parseAsParameters(mConfigurationDocument);
        parseCwfPluginConfiguration(mConfigurationDocument);
        parsePerspectiveConfiguration(mConfigurationDocument);
        parseMenuConfiguration(mConfigurationDocument);
        parseDataSourceConfiguration(mConfigurationDocument);
        parseGetMethodsConfiguration(mConfigurationDocument);
        parseMvcConfiguration(mConfigurationDocument);
        parseLocales(mConfigurationDocument);
        parseAsComponents(mConfigurationDocument);
    }

    @Override
    public List<CwfDataIf> getAsGetMethods() {
        return mAsGetMethods;
    }

    @Override
    public String getAsParameter(String pParameterName) {
        return mAsParameters.get(pParameterName);
    }

    @Override
    public Map<String, AsViewDefinition> getViewDefinitions() {
        return mMvcViews;
    }

    @Override
    public Element getConfigurationDocument() {
        return mConfigurationDocument;
    }

    @Override
    public void reloadConfiguration() {
        // Empty the configuration related data sources
        AsGlobalDataSources tDs = As.getGlobalDataSources();
        tDs.getDataSource(AsContextMenu.class).clear();
        tDs.getDataSource(AsDisplayDefinition.class).clear();
        tDs.getDataSource(AsMenuItem.class).clear();
        tDs.getDataSource(AsViewDefinition.class).clear();
        tDs.getDataSource(AsPerspectiveData.class).clear();
        tDs.getDataSource(AsPluginInfo.class).clear();
        // Empty the local data structures
        mAsParameters.clear();
        mCwfPluginModules.clear();
        mRoles.clear();
        mMenuItems.clear();
        mContextMenus.clear();
        mAsTrees.clear();
        mAsListTrees.clear();
        mAsGetMethods.clear();
        mPerspectives.clear();
        mMvcViews.clear();
        mMvcDisplays.clear();
        mLocales.clear();

        // Now re-parse from the element
        try {
            mConfigurationDocument = AsXmlTool.loadCwfModule(mLoadModule).getDocumentElement();
            parseAsParameters(mConfigurationDocument);
            parseCwfPluginConfiguration(mConfigurationDocument);
            parsePerspectiveConfiguration(mConfigurationDocument);
            parseMenuConfiguration(mConfigurationDocument);
            parseGetMethodsConfiguration(mConfigurationDocument);
            parseMvcConfiguration(mConfigurationDocument);
            parseLocales(mConfigurationDocument);
            submitMenuConfiguration();
            submitPerspectiveConfiguration();
            submitDataSourceConfiguration();
            submitMvcConfiguration();
            submitPluginConfiguration();
            submitLocaleConfiguration();
        }
        catch (Exception e) {
            AsLoggerIf.Singleton.get().logThrowable("Error during configuration reload", e);
        }
    }

    /**
     * The Class ContextMenuMap.
     */
    @SuppressWarnings("serial")
    private static class ContextMenuMap extends HashMap<String, CwfDataIf> {

        /**
         * Add or merge context menus
         * @param pObjects
         */
        public void addAll(List<CwfDataIf> pObjects) {
            if (pObjects != null) {
                for (CwfDataIf tData : pObjects) {
                    String tKey = tData.getProperty(ATTR_TYPE) + ","
                        + tData.getProperty(ATTR_PERSPECTIVE) + ","
                        + tData.getProperty(ATTR_VIEW);
                    CwfDataIf tOld = get(tKey);
                    if (tOld != null) {
                        mergeInto(tData, tOld);
                    }
                    else {
                        put(tKey, tData);
                    }
                }
            }
        }

        /**
         * Merge one context menu into another
         * @param pNew the context menu to merge
         * @param pOld the existing menu to merge into
         */
        private void mergeInto(CwfDataIf pNew, CwfDataIf pOld) {
            mergeMenuItems(pNew, pOld);
            mergeMenus(pNew, pOld);
        }

        /**
         * Merge menu items.
         * @param pNew the context menu to merge
         * @param pOld the existing menu to merge into
         */
        private void mergeMenuItems(CwfDataIf pNew, CwfDataIf pOld) {
            for (CwfDataIf tNewMenuItem : pNew.getObjectList(TAG_MENUITEM)) {
                String tRemove = tNewMenuItem.getProperty(ATTR_REMOVE);
                String tPosition = tNewMenuItem.getProperty(ATTR_POSITION);
                if (tRemove != null) {
                    for (CwfDataIf tData : pOld.getObjectList(TAG_MENUITEM)) {
                        if (tRemove.equals(tData.getProperty(ATTR_ID)) ||
                            tRemove.equals(tData.getProperty(ATTR_VIEW))) {
                            pOld.removeObject(TAG_MENUITEM, tData);
                            break;
                        }
                    }
                }
                else if (tPosition == null) {
                    pOld.addObject(TAG_MENUITEM, tNewMenuItem);
                }
                else if (tPosition.equals("first")) {
                    pOld.addObject(TAG_MENUITEM, tNewMenuItem, 0);
                }
                else if (tPosition.startsWith("after:")) {
                    String tId = tPosition.substring(6);
                    for (CwfDataIf tData : pOld.getObjectList(TAG_MENUITEM)) {
                        if (tId.equals(tData.getProperty(ATTR_ID)) ||
                            tId.equals(tData.getProperty(ATTR_VIEW))) {
                            int tIndex = pOld.getObjectList(TAG_MENUITEM).indexOf(tData);
                            pOld.addObject(TAG_MENUITEM, tNewMenuItem, tIndex + 1);
                            break;
                        }
                    }
                }
                else if (tPosition.startsWith("before:")) {
                    String tId = tPosition.substring(7);
                    for (CwfDataIf tData : pOld.getObjectList(TAG_MENUITEM)) {
                        if (tId.equals(tData.getProperty(ATTR_ID)) ||
                            tId.equals(tData.getProperty(ATTR_VIEW))) {
                            int tIndex = pOld.getObjectList(TAG_MENUITEM).indexOf(tData);
                            pOld.addObject(TAG_MENUITEM, tNewMenuItem, tIndex);
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Merge menus.
         * @param pNew the context menu to merge
         * @param pOld the existing menu to merge into
         */
        private void mergeMenus(CwfDataIf pNew, CwfDataIf pOld) {
            for (CwfDataIf tMenu : pNew.getObjectList(TAG_MENU)) {
                String tRemove = tMenu.getProperty(ATTR_REMOVE);
                String tPosition = tMenu.getProperty(ATTR_POSITION);
                if (tRemove != null) {
                    String tId = tMenu.getProperty(ATTR_REMOVE);
                    for (CwfDataIf tObject : pOld.getObjectList(TAG_MENU)) {
                        if (tObject.getProperty(ATTR_ID).equals(tId)) {
                            pOld.removeObject(TAG_MENU, tObject);
                            break;
                        }
                    }
                }
                else {
                    // Check if the menu already exists
                    String tId = tMenu.getProperty(ATTR_ID);
                    CwfDataIf tExistingMenu = null;
                    for (CwfDataIf tObject : pOld.getObjectList(TAG_MENU)) {
                        if (tObject.getProperty(ATTR_ID).equals(tId)) {
                            tExistingMenu = tObject;
                            break;
                        }
                    }
                    if (tExistingMenu != null) {
                        mergeInto(tMenu, tExistingMenu);
                    }
                    else if (tPosition == null) {
                        pOld.addObject(TAG_MENU, tMenu);
                    }
                    else if (tPosition.equals("first")) {
                        pOld.addObject(TAG_MENU, tMenu, 0);
                    }
                    else if (tPosition.startsWith("after:")) {
                        String tRefId = tPosition.substring(6);
                        for (CwfDataIf tObject : pOld.getObjectList(TAG_MENU)) {
                            if (tObject.getProperty(ATTR_ID).equals(tRefId)) {
                                int tIndex = pOld.getObjectList(TAG_MENU).indexOf(tObject);
                                pOld.addObject(TAG_MENU, tMenu, tIndex + 1);
                                break;
                            }
                        }
                    }
                    else if (tPosition.startsWith("before:")) {
                        String tRefId = tPosition.substring(7);
                        for (CwfDataIf tObject : pOld.getObjectList(TAG_MENU)) {
                            if (tObject.getProperty(ATTR_ID).equals(tRefId)) {
                                int tIndex = pOld.getObjectList(TAG_MENU).indexOf(tObject);
                                pOld.addObject(TAG_MENU, tMenu, tIndex);
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Class to simplify mapping between roles and perspectives
     */
    @SuppressWarnings("serial")
    private static class RolesMap extends LinkedHashMap<String, CwfDataIf> {

        // turn role configuration upside down

        /**
         * Add role mappings
         * @param pRoles the roles to add mappings for
         * @return true
         */
        public boolean addAll(List<CwfDataIf> pRoles) {
            if (pRoles != null) {
                for (CwfDataIf tData : pRoles) {
                    String tKey = tData.getProperty(ATTR_NAME);
                    CwfDataIf tEntry = get(tKey);
                    if (tEntry == null) {
                        put(tKey, tData);
                    }
                    else {
                        List<CwfDataIf> tMappingsToAdd = tData.getObjectList(ATTR_PERSPECTIVE);
                        List<CwfDataIf> tExistingMappings = tEntry.getObjectList(ATTR_PERSPECTIVE);
                        for (CwfDataIf tNewEntry : tMappingsToAdd) {
                            String tPerspectiveRefToAdd = tNewEntry.getProperty(ATTR_REF);
                            boolean tExists = false;
                            for (CwfDataIf tExistingEntry : tExistingMappings) {
                                if (tExistingEntry.getProperty(ATTR_REF).equals(tPerspectiveRefToAdd)) {
                                    tExists = true;
                                }
                            }
                            if (!tExists) {
                                tExistingMappings.add(tNewEntry);
                            }
                        }
                    }
                }
            }
            return true;
        }

    }

}
