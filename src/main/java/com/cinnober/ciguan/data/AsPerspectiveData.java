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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cinnober.ciguan.AsInitializationException;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.impl.MvcDeviceTypeEnum;
import com.cinnober.ciguan.client.impl.MvcScreenOrientationEnum;
import com.cinnober.ciguan.datasource.impl.AsMapRefData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Class holding data about a perspective.
 */
@JsonInclude(Include.NON_NULL)
public class AsPerspectiveData extends AsMapRefData {

    /** The id counter. */
    private static int cIdCounter = 1000;

    /** The roles list. */
    private final List<String> mRolesList;

    /** The device types. */
    private Set<String> mDeviceTypes = new HashSet<String>();

    /** The screen orientations. */
    private Set<String> mScreenOrientations = new HashSet<String>();

    /**
     * Instantiates a new as perspective data.
     *
     * @param pValues the values
     * @param pRoles the roles
     * @param pSlotTemplates all existing perspective slot templates
     * @throws AsInitializationException if the perspective configuration has errors
     */
    public AsPerspectiveData(CwfDataIf pValues, List<CwfDataIf> pRoles, Map<String, CwfDataIf> pSlotTemplates)
        throws AsInitializationException {
        super(pValues);
        pValues.setProperty(ATTR_KEY, cIdCounter++);
        String tId = pValues.getProperty(ATTR_ID);
        mRolesList = new ArrayList<String>();
        for (CwfDataIf tRole : pRoles) {
            for (CwfDataIf tPerspective : tRole.getObjectList("perspective")) {
                if (tId.equals(tPerspective.getProperty(ATTR_REF))) {
                    mRolesList.add(tRole.getProperty(ATTR_NAME));
                }
            }
        }
        // Initialize supported device types
        String tType = getValues().getProperty(ATTR_DEVICE_TYPE);
        if (tType == null) {
            for (MvcDeviceTypeEnum tDeviceType : MvcDeviceTypeEnum.values()) {
                mDeviceTypes.add(tDeviceType.name());
            }
        }
        else {
            String[] tTypes = tType.split(",");
            for (String tStr : tTypes) {
                mDeviceTypes.add(tStr);
            }
        }
        // Initialize supported screen orientations
        for (CwfDataIf tContent : getValues().getObjectList(TAG_CONTENT)) {
            if (tContent.getProperty(ATTR_SCREEN_ORIENTATION) != null) {
                String[] tTypes = tContent.getProperty(ATTR_SCREEN_ORIENTATION).split(",");
                for (String tStr : tTypes) {
                    mScreenOrientations.add(tStr);
                }
            }
        }
        // Default to all orientations if no orientations are defined for any of the device types
        if (mScreenOrientations.isEmpty()) {
            for (MvcScreenOrientationEnum tOrientation : MvcScreenOrientationEnum.values()) {
                mScreenOrientations.add(tOrientation.name());
            }
        }

        // Apply slot templates for the outermost group
        for (CwfDataIf tContent : getValues().getObjectList(TAG_CONTENT)) {
            for (CwfDataIf tGroup : tContent.getObjectList(TAG_GROUP)) {
                applySlotTemplates(tGroup, pSlotTemplates);
            }
        }
    }

    public SlotModel getSlotModel() {
        List<CwfDataIf> slots = getValues().getObjectList(TAG_SLOT);
        for (CwfDataIf slot : slots) {
            // There can only be one or none...
            return new SlotModel(slot);
        }
        // no slot tag found... assume we have old configuration. 
        // Transform!
        return transform(getValues().getObjectList(TAG_CONTENT));
    }

    @Deprecated
    private SlotModel transform(List<CwfDataIf> content) {
        // TODO! ...
        return null;
    }

    /**
     * Apply slot templates recursively for the given group
     * @param pGroup the perspective group to process
     * @param pSlotTemplates all existing perspective slot templates
     * @throws AsInitializationException if the perspective configuration has errors
     */
    protected void applySlotTemplates(CwfDataIf pGroup, Map<String, CwfDataIf> pSlotTemplates)
        throws AsInitializationException {
        for (CwfDataIf tSlot : pGroup.getObjectList(TAG_SLOT)) {
            if (tSlot.getProperty(ATTR_TEMPLATE) != null) {
                // Do the replacement
                CwfDataIf tTemplate = pSlotTemplates.get(tSlot.getProperty(ATTR_TEMPLATE));
                if (tTemplate != null) {
                    merge(tSlot, tTemplate);
                }
            }
            // Recursively process all groups in the slot
            for (CwfDataIf tGroup : tSlot.getObjectList(TAG_GROUP)) {
                applySlotTemplates(tGroup, pSlotTemplates);
            }
        }
    }

    /**
     * Merge a slot template into a slot
     * @param pSlot the slot to merge into
     * @param pSlotTemplate the slot template to merge
     * @throws AsInitializationException if the perspective configuration has errors
     */
    protected void merge(CwfDataIf pSlot, CwfDataIf pSlotTemplate) throws AsInitializationException {
        // TODO: Can we merge slots in a controlled way?
        if (!pSlot.getObjectList(TAG_SLOT).isEmpty() ||
            !pSlot.getObjectList(TAG_GROUP).isEmpty()) {
            throw new AsInitializationException(
                "Slot '" + pSlot.getProperty(ATTR_ID) + "' refencing template '" +
                pSlot.getProperty(ATTR_TEMPLATE) + "' cannot have slots or groups");
        }

        // Create a copy and remove unwanted properties
        CwfDataIf tSlotTemplate = CwfDataFactory.copy(pSlotTemplate);
        tSlotTemplate.removeProperty(ATTR_TAG_NAME);

        // Copy all properties which are not already set
        for (String tProperty : tSlotTemplate.getProperties().keySet()) {
            if (pSlot.getProperty(tProperty) == null) {
                pSlot.setProperty(tProperty, tSlotTemplate.getProperty(tProperty));
            }
        }

        // Copy all object lists
        if (tSlotTemplate.getObjectListMap() != null) {
            for (String tList : tSlotTemplate.getObjectListMap().keySet()) {
                pSlot.addObject(tList, tSlotTemplate.getObjectList(tList));
            }
        }
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
     * Gets the max width.
     *
     * @return the max width
     */
    @JsonIgnore
    public Integer getMaxWidth() {
        String tValue = getValues().getProperty(ATTR_MAX_WIDTH);
        return tValue != null ? Integer.valueOf(tValue) : null;
    }

    /**
     * Gets the device type.
     *
     * @return the device type
     */
    @JsonIgnore
    public Set<String> getDeviceType() {
        return mDeviceTypes;
    }

    /**
     * @return the supported screen orientations
     */
    @JsonIgnore
    public Set<String> getScreenOrientation() {
        return mScreenOrientations;
    }

    /**
     * Get the roles that have access to this perspective.
     *
     * @return a list of roles allowed to access the perspective
     */
    public List<String> getRoles() {
        return mRolesList;
    }

    
    @JsonInclude(Include.NON_NULL)
    public static class SlotModel {
        public String type;
        @JsonInclude(Include.NON_DEFAULT)
        public boolean fixed;
        public String size;
        public Set<SlotModel> slotModel;
        public String[] viewId;
        
        public SlotModel(CwfDataIf slot) {
            type = slot.getProperty(ATTR_TYPE);
            fixed = slot.getBooleanProperty(ATTR_FIXED) != null ? slot.getBooleanProperty(ATTR_FIXED) : false;
            size = slot.getProperty(ATTR_SIZE);
            List<CwfDataIf> slots = slot.getObjectList(TAG_SLOT);
            for (int i = 0; i < slots.size(); i++) {
                addSlot(new SlotModel(slots.get(i)));
            }
            List<CwfDataIf> views = slot.getObjectList(TAG_VIEW);
            viewId = views.isEmpty() ? null : new String[views.size()];
            for (int i = 0; views != null && i < views.size(); i++) {
                viewId[i] = views.get(i).getProperty(ATTR_ID);
            }
        }
        
        public void addSlot(SlotModel slot) {
            if (slotModel == null) {
                slotModel = new LinkedHashSet<>();
            }
            slotModel.add(slot);
        }

    }

}
