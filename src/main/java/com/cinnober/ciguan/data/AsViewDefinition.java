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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cinnober.ciguan.AsModifyViewNotFoundException;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcViewTypeIf;
import com.cinnober.ciguan.client.impl.MvcViewTypeEnum;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.impl.AsXmlRefData;
import com.cinnober.ciguan.datasource.tree.AsViewContext;
import com.cinnober.ciguan.datasource.tree.AsViewField;
import com.cinnober.ciguan.impl.As;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper class containg the xml definition of a view.
 */
@SuppressWarnings("deprecation")
@JsonInclude(Include.NON_NULL)
public class AsViewDefinition extends AsXmlRefData {

    /** The type. */
    private final MvcViewTypeIf mType;

    /**
     * Instantiates a new as view definition.
     *
     * @param pXmlDefinition the xml definition
     * @param pDefinedViews the defined views
     */
    public AsViewDefinition(CwfDataIf pXmlDefinition, Map<String, AsViewDefinition> pDefinedViews) {
        super(pXmlDefinition);
        if (pXmlDefinition.getProperty(ATTR_EXTENDS) != null) {
            extend(pDefinedViews.get(pXmlDefinition.getProperty(ATTR_EXTENDS)));
        }
        else if (pXmlDefinition.getProperty(ATTR_MODIFY) != null) {
            AsViewDefinition tViewToModify = pDefinedViews.get(pXmlDefinition.getProperty(ATTR_MODIFY));
            if (tViewToModify == null) {
                throw new AsModifyViewNotFoundException(
                    "View to be modified cannot be found: " + pXmlDefinition.getProperty(ATTR_MODIFY));
            }
            modify(tViewToModify);
        }
        mType = MvcViewTypeEnum.valueOf(pXmlDefinition.getProperty(ATTR_TYPE));
        String tDataSourceId = getValues().getProperty(ATTR_DATASOURCE_ID);
        if (tDataSourceId != null) {
            AsDataSourceDef<?> tDef = AsDataSourceDef.getDataSourceDef(tDataSourceId);
            if (tDef != null) {
                getValues().setProperty(ATTR_DATASOURCE_TYPE, tDef.getType());
                getValues().setProperty(ATTR_IS_QUERY, tDef.isQueryDataSource());
            }
        }
        if (getType() == MvcViewTypeEnum.chart) {
            List<CwfDataIf> tList = getValues().getObjectList(TAG_CHART);
            if (tList.size() > 0) {
                tList = tList.get(0).getObjectList(TAG_CHART_DATA);
                if (tList.size() > 0) {
                    tDataSourceId = tList.get(0).getProperty(ATTR_DATASOURCE_ID);
                    if (tDataSourceId != null) {
                        AsDataSourceDef<?> tDef = AsDataSourceDef.getDataSourceDef(tDataSourceId);
                        if (tDef != null) {
                            tList.get(0).setProperty(ATTR_DATASOURCE_TYPE, tDef.getType());
                        }
                    }
                }
            }
        }
        if (getType() == MvcViewTypeEnum.tree && getDataSourceId() == null) {
            getValues().setProperty(ATTR_DATASOURCE_ID, getId());
        }
        if (getType() == MvcViewTypeEnum.fileupload) {
            // Prevent docking of the file upload form
            getValues().setProperty(ATTR_DOCKABLE, Boolean.FALSE);
        }

        // For detail views, automatically add a context of the same type as the model if not already present
        addDefaultDetailContext();

        // Clean out all nested views flagged for removal or modification
        for (CwfDataIf tNestedViews : getValues().getObjectList(TAG_NESTED_VIEWS)) {
            Iterator<CwfDataIf> tIterator = tNestedViews.getObjectList(TAG_VIEW).iterator();
            while (tIterator.hasNext()) {
                CwfDataIf tView = tIterator.next();
                if (tView.getProperty(ATTR_REMOVE) != null || tView.getProperty(ATTR_MODIFY) != null) {
                    tIterator.remove();
                }
            }
        }
    }

    /**
     * Add a default context to detail views unless already present.
     * This context should be of the same type as the model and have source and target set to ".".
     */
    protected void addDefaultDetailContext() {
        if (getType() == MvcViewTypeEnum.detail && getModel() != null) {
            boolean tAddDefaultContext = true;
            for (CwfDataIf tContext : getValues().getObjectList(TAG_CONTEXT)) {
                if (getModel().equals(tContext.getProperty(ATTR_TYPE)) &&
                    ".".equals(tContext.getProperty(ATTR_SOURCE)) &&
                    ".".equals(tContext.getProperty(ATTR_TARGET))) {
                    tAddDefaultContext = false;
                }
            }
            if (tAddDefaultContext) {
                CwfDataIf tDefaultContext = CwfDataFactory.create();
                tDefaultContext.setProperty(ATTR_TAG_NAME, TAG_CONTEXT);
                tDefaultContext.setProperty(ATTR_TYPE, getModel());
                tDefaultContext.setProperty(ATTR_SOURCE, ".");
                tDefaultContext.setProperty(ATTR_TARGET, ".");
                getValues().addObject(TAG_CONTEXT, tDefaultContext);
            }
        }
    }

    /**
     * Modify the view specified by {@code pOriginalView}.
     *
     * @param pOriginalView the original view
     */
    protected void modify(AsViewDefinition pOriginalView) {
        extend(pOriginalView);
        getValues().setProperty(ATTR_ID, pOriginalView.getId());
    }

    /**
     * Extend the view specified by {@code pOriginalView}.
     *
     * @param pOriginalView the original view
     */
    protected void extend(AsViewDefinition pOriginalView) {
        inheritProperty(pOriginalView, ATTR_TYPE);
        inheritProperty(pOriginalView, ATTR_MODEL);
        inheritProperty(pOriginalView, ATTR_DATASOURCE_ID);
        inheritProperty(pOriginalView, ATTR_ON_SUCCESS);
        inheritProperty(pOriginalView, ATTR_TARGET_VIEW);
        inheritProperty(pOriginalView, ATTR_FOCUS);
        inheritProperty(pOriginalView, ATTR_PINNABLE);
        inheritProperty(pOriginalView, ATTR_DOCKABLE);
        inheritProperty(pOriginalView, ATTR_RESIZABLE);
        inheritProperty(pOriginalView, ATTR_SORTABLE);
        inheritProperty(pOriginalView, ATTR_FILTERABLE);
        inheritProperty(pOriginalView, ATTR_ACC_SERVICE);
        inheritProperty(pOriginalView, ATTR_MULTI_SELECT);
        inheritProperty(pOriginalView, ATTR_MODAL);
        inheritProperty(pOriginalView, ATTR_DRAG_SOURCE);
        inheritProperty(pOriginalView, ATTR_FORM_HANDLER);
        inheritProperty(pOriginalView, ATTR_SINGLETON);

        // Copy (add) contexts, sets and clears
        for (CwfDataIf tData : pOriginalView.getValues().getObjectList(TAG_CONTEXT)) {
            getValues().addObject(TAG_CONTEXT, tData);
        }
        for (CwfDataIf tData : pOriginalView.getValues().getObjectList(TAG_SET)) {
            getValues().addObject(TAG_SET, tData);
        }
        for (CwfDataIf tData : pOriginalView.getValues().getObjectList(TAG_CLEAR)) {
            getValues().addObject(TAG_CLEAR, tData);
        }

        // Copy (add) sorting
        for (CwfDataIf tData : pOriginalView.getValues().getObjectList(TAG_SORT)) {
            getValues().addObject(TAG_SORT, tData);
        }


        // Create a list of all own nested views
        List<CwfDataIf> tOwnNestedViews = new ArrayList<CwfDataIf>();
        for (CwfDataIf tNestedViews : getValues().getObjectList(TAG_NESTED_VIEWS)) {
            for (CwfDataIf tView : tNestedViews.getObjectList(TAG_VIEW)) {
                tOwnNestedViews.add(tView);
            }
        }

        // Copy (add) nested views
        for (CwfDataIf tNestedViews : pOriginalView.getValues().getObjectList(TAG_NESTED_VIEWS)) {
            for (CwfDataIf tView : tNestedViews.getObjectList(TAG_VIEW)) {
                // Create a copy of the original view element
                CwfDataIf tCopy = CwfDataFactory.copy(tView);
                // Add the original nested-views element if not present, minus its view children
                if (getValues().getObjectList(TAG_NESTED_VIEWS).size() == 0) {
                    CwfDataIf tEmptyList = CwfDataFactory.copy(tNestedViews);
                    tEmptyList.removeObjectList(TAG_VIEW);
                    getValues().addObject(TAG_NESTED_VIEWS, tEmptyList);
                }
                else {
                    // Inherit original allowMultiExpansion if not set
                    CwfDataIf tNestedViews0 = getValues().getObjectList(TAG_NESTED_VIEWS).get(0);
                    if (tNestedViews0.getProperty(ATTR_ALLOW_MULTI_EXPANSION) == null &&
                        tNestedViews.getProperty(ATTR_ALLOW_MULTI_EXPANSION) != null) {
                        tNestedViews0.setProperty(ATTR_ALLOW_MULTI_EXPANSION,
                            tNestedViews.getProperty(ATTR_ALLOW_MULTI_EXPANSION));
                    }
                }
                // Add the view unless it is set for removal
                String tRef = tCopy.getProperty(ATTR_REF);
                boolean tToBeAdded = true;
                for (CwfDataIf tOwnView : tOwnNestedViews) {
                    String tRemove = tOwnView.getProperty(ATTR_REMOVE);
                    String tModify = tOwnView.getProperty(ATTR_MODIFY);
                    if (tRemove != null && tRemove.equals(tRef)) {
                        tToBeAdded = false;
                    }
                    else if (tModify != null && tModify.equals(tRef)) {
                        if (tOwnView.getProperty(ATTR_EXPANDED) != null) {
                            tCopy.setProperty(ATTR_EXPANDED, tOwnView.getProperty(ATTR_EXPANDED));
                        }
                    }
                }
                if (tToBeAdded) {
                    CwfDataIf tNestedViews0 = getValues().getObjectList(TAG_NESTED_VIEWS).get(0);
                    tNestedViews0.addObject(TAG_VIEW, tCopy);
                }
            }
        }

        // Copy (add) layout
        CwfDataIf tNewLayout = CwfDataFactory.copy(pOriginalView.getLayout());
        HashMap<String, CwfDataIf> tMap = new HashMap<String, CwfDataIf>();
        if (tNewLayout != null) {
            for (CwfDataIf tExistingFieldSet : tNewLayout.getObjectList(TAG_FIELDSET)) {
                tMap.put(tExistingFieldSet.getProperty(ATTR_ID), tExistingFieldSet);
            }
            if (getLayout() != null) {
                for (CwfDataIf tFieldSet : getLayout().getObjectList(TAG_FIELDSET)) {
                    if (tFieldSet.getProperty(ATTR_REMOVE) != null) {
                        CwfDataIf tCurrentFieldSet = tMap.get(tFieldSet.getProperty(ATTR_REMOVE));
                        if (tCurrentFieldSet != null) {
                            tNewLayout.removeObject(TAG_FIELDSET, tCurrentFieldSet);
                            tMap.remove(tCurrentFieldSet.getProperty(ATTR_ID));
                        }
                        continue;
                    }
                    CwfDataIf tCurrentFieldSet = tMap.get(tFieldSet.getProperty(ATTR_ID));
                    if (tCurrentFieldSet != null) {
                        for (CwfDataIf tField : tFieldSet.getObjectList(TAG_FIELD)) {
                            String tName = tField.getProperty(ATTR_NAME);
                            if (tName == null) {
                                tName = tField.getProperty(ATTR_REMOVE);
                            }
                            CwfDataIf tExistingField = null;
                            for (CwfDataIf tCurrentField : tCurrentFieldSet.getObjectList(TAG_FIELD)) {
                                if (tCurrentField.getProperty(ATTR_NAME).equals(tName)) {
                                    tExistingField = tCurrentField;
                                    break;
                                }
                            }
                            if (tExistingField == null) {
                                if (tField.getProperty(ATTR_NAME) != null) {
                                    int tAddPosition = parseInsertPosition(tCurrentFieldSet, tField);
                                    if (tAddPosition >= 0) {
                                        tCurrentFieldSet.addObject(TAG_FIELD, tField, tAddPosition);
                                    }
                                    else {
                                        tCurrentFieldSet.addObject(TAG_FIELD, tField);
                                    }
                                }
                            }
                            else if (tField.getProperty(ATTR_REMOVE) != null) {
                                tCurrentFieldSet.removeObject(TAG_FIELD, tExistingField);
                            }
                            else {
                                tCurrentFieldSet.replaceObject(TAG_FIELD, tExistingField, tField);
                            }
                        }
                    }
                    else {
                        tNewLayout.addObject(TAG_FIELDSET, tFieldSet);
                    }
                }
                // New button panel should replace existing button panel
                boolean tOldButtons = !tNewLayout.getObjectList(TAG_BUTTONPANEL).isEmpty();
                boolean tNewButtons = !getLayout().getObjectList(TAG_BUTTONPANEL).isEmpty();
                if (tOldButtons && tNewButtons) {
                    tNewLayout.removeObject(TAG_BUTTONPANEL, 0);
                }
                if (tNewButtons) {
                    tNewLayout.addObject(TAG_BUTTONPANEL, getLayout().getObjectList(TAG_BUTTONPANEL));
                }

                // New grouping should replace extisting grouping
                boolean tOldGroup = !tNewLayout.getObjectList(TAG_GROUP).isEmpty();
                boolean tNewGroup = !getLayout().getObjectList(TAG_GROUP).isEmpty();
                if (tOldGroup && tNewGroup) {
                    tNewLayout.removeObject(TAG_GROUP, 0);
                }
                if (tNewGroup) {
                    tNewLayout.addObject(TAG_GROUP, getLayout().getObjectList(TAG_GROUP));
                }
            }
            getValues().removeObject(TAG_LAYOUT, 0);
            getValues().addObject(TAG_LAYOUT, tNewLayout);
        }

        // Viewports, add/modify/remove display fields
        for (CwfDataIf tOldDisplay : pOriginalView.getValues().getObjectList(TAG_DISPLAY)) {
            // Make a copy of the original display first, in order not to accidentally modify it
            CwfDataIf tCurrentDisplay = CwfDataFactory.copy(tOldDisplay);
            CwfDataIf tNewDisplay = null;

            // Check if the display type already exists
            for (CwfDataIf tDisplay : getValues().getObjectList(TAG_DISPLAY)) {
                if (tCurrentDisplay.getProperty(ATTR_TYPE).equals(tDisplay.getProperty(ATTR_TYPE))) {
                    tNewDisplay = tDisplay;
                }
            }
            if (tNewDisplay != null) {
                // Replace the new display with a copy of the old one and work from
                // the new one
                getValues().replaceObject(TAG_DISPLAY, tNewDisplay, tCurrentDisplay);
                for (CwfDataIf tField : tNewDisplay.getObjectList(TAG_FIELD)) {
                    String tName = tField.getProperty(ATTR_NAME);
                    if (tName == null) {
                        tName = tField.getProperty(ATTR_REMOVE);
                    }
                    CwfDataIf tExistingField = null;
                    for (CwfDataIf tCurrentField : tCurrentDisplay.getObjectList(TAG_FIELD)) {
                        if (tCurrentField.getProperty(ATTR_NAME).equals(tName)) {
                            tExistingField = tCurrentField;
                            break;
                        }
                    }
                    if (tExistingField == null) {
                        if (tField.getProperty(ATTR_NAME) != null) {
                            int tAddPosition = parseInsertPosition(tCurrentDisplay, tField);
                            if (tAddPosition >= 0) {
                                tCurrentDisplay.addObject(TAG_FIELD, tField, tAddPosition);
                            }
                            else {
                                tCurrentDisplay.addObject(TAG_FIELD, tField);
                            }

                        }
                    }
                    else if (tField.getProperty(ATTR_REMOVE) != null) {
                        tCurrentDisplay.removeObject(TAG_FIELD, tExistingField);
                    }
                    else {
                        tCurrentDisplay.replaceObject(TAG_FIELD, tExistingField, tField);
                    }
                }
            }
            else {
                // Copy original display
                getValues().addObject(TAG_DISPLAY, tCurrentDisplay);
            }

        }

    }

    /**
     * Attempt to locate the insert position for the given field if it has a position indicator.
     *
     * @param pParent the field set or display parent
     * @param pField the field being added
     * @return the insert position, or -1 to append
     */
    protected int parseInsertPosition(CwfDataIf pParent, CwfDataIf pField) {
        String tPosition = pField.getProperty(ATTR_POSITION);
        if (tPosition != null) {
            if ("first".equals(tPosition)) {
                return 0;
            }
            else if (tPosition.startsWith("after:")) {
                String tId = tPosition.substring(6);
                List<? extends CwfDataIf> tFields = pParent.getObjectList(ATTR_FIELD);
                for (CwfDataIf tField : tFields) {
                    if (tId.equals(tField.getProperty(ATTR_NAME))) {
                        return tFields.indexOf(tField) + 1;
                    }
                }
            }
            else if (tPosition.startsWith("before:")) {
                String tId = tPosition.substring(7);
                List<? extends CwfDataIf> tFields = pParent.getObjectList(ATTR_FIELD);
                for (CwfDataIf tField : tFields) {
                    if (tId.equals(tField.getProperty(ATTR_NAME))) {
                        return tFields.indexOf(tField);
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Only inherit if original view's property is set but not current.
     *
     * @param pOriginalView the original view
     * @param pProperty the property
     */
    protected void inheritProperty(AsViewDefinition pOriginalView, String pProperty) {
        String tOriginalValue = pOriginalView.getValues().getProperty(pProperty);
        String tExtendedValue = getValues().getProperty(pProperty);
        if (tOriginalValue != null && tExtendedValue == null) {
            getValues().setProperty(pProperty, tOriginalValue);
        }
    }

    /**
     * Gets the acc service.
     *
     * @return the acc service
     */
    @JsonProperty("auth")
    @JsonInclude(Include.NON_EMPTY)
    public String getAccService() {
        return getValues().getProperty(ATTR_ACC_SERVICE);
    }

    @JsonInclude(Include.NON_EMPTY)
    public String getTitle() {
        return getValues().getProperty(ATTR_TITLE);
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
     * Gets the type.
     *
     * @return the type
     */
    public MvcViewTypeIf getType() {
        return mType;
    }

    /**
     * Gets the data source id.
     *
     * @return the data source id
     */
    public String getDataSourceId() {
//        if (getType() == MvcViewTypeEnum.chart) {
//            List<CwfDataIf> tList = getValues().getObjectList(TAG_CHART);
//            if (tList.size() > 0) {
//                tList = tList.get(0).getObjectList(TAG_CHART_DATA);
//                if (tList.size() > 0) {
//                    return tList.get(0).getProperty(ATTR_DATASOURCE_ID);
//                }
//            }
//        }
        return getValues().getProperty(ATTR_DATASOURCE_ID);
    }

    /**
     * Gets the on success.
     *
     * @return the on success
     */
    public String getOnSuccess() {
        return getValues().getProperty(ATTR_ON_SUCCESS);
    }

    /**
     * Gets the target view.
     *
     * @return the target view
     */
    public String getTargetView() {
        return getValues().getProperty(ATTR_TARGET_VIEW);
    }

    /**
     * Checks if is dockable.
     *
     * @return {@code true}, if is dockable
     */
    @JsonInclude(Include.NON_DEFAULT)
    public boolean isDockable() {
        Boolean tDockable = getValues().getBooleanProperty(ATTR_DOCKABLE);
        return tDockable != null ? tDockable : true;
    }

    /**
     * Checks if is resizable.
     *
     * @return {@code true}, if is resizable
     */
    @JsonInclude(Include.NON_DEFAULT)
    public boolean isResizable() {
        Boolean tResizable = getValues().getBooleanProperty(ATTR_RESIZABLE);
        return tResizable != null ? tResizable : true;
    }

    /**
     * Gets the layout.
     *
     * @return the layout
     */
    private CwfDataIf getLayout() {
        List<CwfDataIf> tList = getValues().getObjectList(TAG_LAYOUT);
        return tList.isEmpty() ? null : tList.get(0);
    }

    /**
     * Gets the data source.
     *
     * @param <T> the generic type
     * @return the data source
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> AsListIf<T> getDataSource() {
        if (mType != MvcViewTypeEnum.table) {
            return null;
        }
        return (AsListIf<T>) As.getGlobalDataSources().getDataSource(getDataSourceId(), null, null);
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public String getModel() {
        return getValues().getProperty(ATTR_MODEL);
    }

    /**
     * Gets the view contexts.
     *
     * @return the view contexts
     */
    @JsonInclude(Include.NON_EMPTY)
    public List<AsViewContext> getViewContexts() {
        ArrayList<AsViewContext> tList = new ArrayList<AsViewContext>();
        for (CwfDataIf tField : getValues().getObjectList(TAG_CONTEXT)) {
            tList.add(new AsViewContext(tField, getModel()));
        }
        return tList;
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    @JsonInclude(Include.NON_EMPTY)
    public List<AsViewField> getFields() {
        ArrayList<AsViewField> tList = new ArrayList<AsViewField>();
        for (CwfDataIf tDisplay : getValues().getObjectList(TAG_DISPLAY)) {
            for (CwfDataIf tField : tDisplay.getObjectList(TAG_FIELD)) {
                tList.add(new AsViewField(null, tField));
            }
        }
        for (CwfDataIf tLayout : getValues().getObjectList(TAG_LAYOUT)) {
            for (CwfDataIf tFieldSet : tLayout.getObjectList(TAG_FIELDSET)) {
                for (CwfDataIf tField : tFieldSet.getObjectList(TAG_FIELD)) {
                    tList.add(new AsViewField(tFieldSet.getProperty(ATTR_PATH), tField));
                }
            }
        }
        return tList;
    }

    /**
     * Gets the menu item.
     *
     * @return the menu item
     */
    @JsonIgnore
    public AsMenuItem getMenuItem() {
        CwfDataIf tMenu = CwfDataFactory.create();
        tMenu.setProperty(ATTR_TAG_NAME, TAG_MENUITEM);
        tMenu.setProperty(ATTR_ID, getId());
        tMenu.setProperty(ATTR_VIEW, getId());
        return new AsMenuItem(tMenu);
    }

}
