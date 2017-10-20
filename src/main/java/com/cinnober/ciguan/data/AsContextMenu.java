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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;
import com.cinnober.ciguan.datasource.tree.AsViewField;

/**
 * Wrapper class holding context menu definition.
 */
@SuppressWarnings("deprecation")
public class AsContextMenu extends AsXmlRefData {

    /** The m sub menus. */
    private List<AsContextMenu> mSubMenus = new ArrayList<AsContextMenu>();

    /** The m menu items. */
    private List<AsMenuItem> mMenuItems = new ArrayList<AsMenuItem>();

    /** The m perspectives. */
    private List<String> mPerspectives;

    /**
     * Instantiates a new as context menu.
     *
     * @param pXmlDefinition the xml definition
     */
    public AsContextMenu(CwfDataIf pXmlDefinition) {
        super(pXmlDefinition);
        String tPerspective = pXmlDefinition.getProperty(ATTR_PERSPECTIVE);
        if (tPerspective != null) {
            mPerspectives = Arrays.asList(tPerspective.split(","));
        }
        for (CwfDataIf tSubMenu : getValues().getObjectList(TAG_MENU)) {
            mSubMenus.add(new AsContextMenu(tSubMenu));
        }
        for (CwfDataIf tMenu : getValues().getObjectList(TAG_MENUITEM)) {
            if (tMenu != null) {
                mMenuItems.add(new AsMenuItem(tMenu));
            }
        }
        getValues().setProperty(ATTR_CONTEXT_MENU_KEY, createKey());
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
     * Gets the sub menus.
     *
     * @return the sub menus
     */
    public List<AsContextMenu> getSubMenus() {
        return mSubMenus;
    }

    /**
     * Gets the menu items.
     *
     * @return the menu items
     */
    public List<AsMenuItem> getMenuItems() {
        return mMenuItems;
    }

    /**
     * Gets the sub menus.
     *
     * @param pPerspective the perspective
     * @return the sub menus
     */
    public List<AsContextMenu> getSubMenus(String pPerspective) {
        List<AsContextMenu> tList = new ArrayList<AsContextMenu>();
        for (AsContextMenu tMenu : mSubMenus) {
            if (tMenu.hasPerspective(pPerspective)) {
                tList.add(tMenu);
            }
        }
        return tList;
    }

    /**
     * Checks for perspective.
     *
     * @param pPerspective the perspective
     * @return true, if successful
     */
    public boolean hasPerspective(String pPerspective) {
        return mPerspectives == null || mPerspectives.contains(pPerspective);
    }

    /**
     * Gets the menu.
     *
     * @param pConnection the connection
     * @param pContext the context
     * @return the menu
     */
    public CwfDataIf getMenu(AsConnectionIf pConnection, AsXmlRefData pContext) {
        String tPerspective = pContext.getValues().getProperty(ATTR_PERSPECTIVE);
        CwfDataIf tData = CwfDataFactory.create();
        tData.setProperty(ATTR_TAG_NAME, TAG_MENU);
        tData.setProperty(ATTR_ID, getId());
        for (AsContextMenu tSubMenu : getSubMenus(tPerspective)) {
            CwfDataIf tFilteredMenu = createSubmenu(pConnection, tSubMenu, pContext);
            if (tFilteredMenu != null && hasChildren(tFilteredMenu)) {
                tData.addObject(TAG_MENU, tFilteredMenu);
            }
        }
        return tData;
    }

    /**
     * Creates the submenu.
     *
     * @param pConnection the connection
     * @param pMenu the menu
     * @param pContext the context
     * @return the cwf data
     */
    private CwfDataIf createSubmenu(AsConnectionIf pConnection, AsContextMenu pMenu, AsXmlRefData pContext) {
        CwfDataIf tRet = null;
        tRet = CwfDataFactory.create();
        tRet.setProperty(ATTR_TAG_NAME, TAG_MENU);
        tRet.setProperty(ATTR_ID, pMenu.getId());
        String tPerspective = pContext.getValues().getProperty(ATTR_PERSPECTIVE);
        for (AsContextMenu tSubMenu : pMenu.getSubMenus()) {
            CwfDataIf tFilteredMenu = createSubmenu(pConnection, tSubMenu, pContext);
            if (tFilteredMenu != null && hasChildren(tFilteredMenu)) {
                tRet.addObject(TAG_MENU, tFilteredMenu);
            }
        }
        for (AsMenuItem tMenuItem : pMenu.getMenuItems()) {
            if (tMenuItem.isForPerspective(tPerspective) &&
                tMenuItem.isAccessAllowed(pConnection) &&
                tMenuItem.isIncluded(pConnection, pContext)) {

                String tExpandField = tMenuItem.getExpand();
                if (tExpandField != null) {
                    AsViewDefinition tView = tMenuItem.getView();
                    for (AsViewField tField : tView.getFields()) {
                        if (tField.getFullName().equals(tExpandField)) {
                            AsListIf<Object> tList = (AsListIf<Object>) pConnection
                                .getDataSourceService().getDataSource(tField.getDataSourceId(), null);

                            for (Object tObject : tList.values()) {
                                CwfDataIf tData = CwfDataFactory.create();
                                tData.setProperty(ATTR_TAG_NAME, TAG_MENUITEM);
                                tData.setProperty(ATTR_ID, tView.getId() + "-" + tList.getText(tObject, null));
                                tData.setProperty(ATTR_VIEW, tView.getId());
                                tData.setProperty(ATTR_AUTO_SUBMIT, tMenuItem.isAutoSubmit());
                                tData.setProperty(ATTR_PARAMETERS, tExpandField + "=" + tList.getKey(tObject));
                                tRet.addObject(TAG_MENUITEM, tData);
                            }
                        }
                    }
                }
                else {
                    tRet.addObject(TAG_MENUITEM, tMenuItem.getValues());
                }
            }
        }
        return tRet;
    }

    /**
     * Checks for children.
     *
     * @param pFilteredMenu the filtered menu
     * @return true, if successful
     */
    private boolean hasChildren(CwfDataIf pFilteredMenu) {
        List<CwfDataIf> tSubMenus = pFilteredMenu.getObjectList(TAG_MENU);
        List<CwfDataIf> tMenuItems = pFilteredMenu.getObjectList(TAG_MENUITEM);
        if (!tSubMenus.isEmpty()) {
            return true;
        }
        // Search for menu items other than ref="separator"
        for (CwfDataIf tMenuItem : tMenuItems) {
            String tView = tMenuItem.getProperty(ATTR_VIEW);
            if (tView == null) {
                tView = tMenuItem.getProperty(ATTR_ID);
            }
            if (tView != null && !tView.isEmpty() && !tView.equals("separator")) {
                return true;
            }
            String tRef = tMenuItem.getProperty(ATTR_REF);
            if (tRef != null && !tRef.equals("separator")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the key.
     *
     * @return the string
     */
    private String createKey() {
        String tPerspective = getValues().getProperty(ATTR_PERSPECTIVE);
        String tView = getValues().getProperty(ATTR_VIEW);
        String tType = getValues().getProperty(ATTR_TYPE);
        boolean tNoPerspective = tPerspective == null || tPerspective.isEmpty();
        boolean tNoView = tView == null || tView.isEmpty();
        if (tNoPerspective && tNoView) {
            return tType;
        }
        return (tNoPerspective ? "*" : tPerspective) + "." + (tNoView ? "*" : tView) + "." + tType;
    }

}
