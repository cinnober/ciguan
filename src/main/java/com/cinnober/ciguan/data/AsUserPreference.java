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

import com.cinnober.ciguan.client.MvcUserPreferenceAttributesIf;
import com.cinnober.ciguan.impl.AsRefDataObject;

/**
 * Class holding a definition of a user preference.
 */
public class AsUserPreference extends AsRefDataObject implements MvcUserPreferenceAttributesIf {

    /** The user id. */
    private final String mUserId;
    
    /** The perspective id. */
    private final String mPerspectiveId;
    
    /** The slot id. */
    private final String mSlotId;
    
    /** The view id. */
    private final String mViewId;
    
    /** The item id. */
    private final String mItemId;
    
    /** The preference. */
    private final String mPreference;
    
    /** The value. */
    private final String mValue;

    /**
     * Instantiates a new as user preference.
     *
     * @param pUserId the user id
     * @param pPerspectiveId the perspective id
     * @param pSlotId the slot id
     * @param pViewId the view id
     * @param pItemId the item id
     * @param pPreference the preference
     * @param pValue the value
     */
    public AsUserPreference(String pUserId, String pPerspectiveId, String pSlotId,
        String pViewId, String pItemId, String pPreference, String pValue) {
        mUserId = pUserId;
        mPerspectiveId = pPerspectiveId;
        mSlotId = pSlotId;
        mViewId = pViewId;
        mItemId = pItemId;
        mPreference = pPreference;
        mValue = pValue;
    }

    /**
     * Short hand constructor where perspective, slot and view are irrelevant.
     *
     * @param pUserId the user id
     * @param pItemId the item id
     * @param pPreference the preference
     * @param pValue the value
     */
    public AsUserPreference(String pUserId, String pItemId, String pPreference, String pValue) {
        this(pUserId, PREF_NONE, PREF_NONE, PREF_NONE, pItemId, pPreference, pValue);
    }
    
    /**
     * Short hand constructor where perspective, slot, view and item are irrelevant.
     *
     * @param pUserId the user id
     * @param pPreference the preference
     * @param pValue the value
     */
    public AsUserPreference(String pUserId, String pPreference, String pValue) {
        this(pUserId, PREF_NONE, PREF_NONE, PREF_NONE, PREF_NONE, pPreference, pValue);
    }
    
    /**
     * Instantiates a new as user preference.
     *
     * @param pPref the pref
     */
    public AsUserPreference(AsUserPreference pPref) {
        this(pPref.getUserId(), pPref.getPerspectiveId(), pPref.getSlotId(), pPref.getViewId(),
            pPref.getItemId(), pPref.getPreference(), "");
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return mUserId;
    }

    /**
     * Gets the perspective id.
     *
     * @return the perspective id
     */
    public String getPerspectiveId() {
        return mPerspectiveId;
    }

    /**
     * Gets the slot id.
     *
     * @return the slot id
     */
    public String getSlotId() {
        return mSlotId;
    }
    
    /**
     * Gets the view id.
     *
     * @return the view id
     */
    public String getViewId() {
        return mViewId;
    }

    /**
     * Gets the item id.
     *
     * @return the item id
     */
    public String getItemId() {
        return mItemId;
    }

    /**
     * Gets the preference.
     *
     * @return the preference
     */
    public String getPreference() {
        return mPreference;
    }
    
    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return mValue;
    }
    
    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return key(mUserId, mPerspectiveId, mSlotId, mViewId, mItemId, mPreference);
    }
    
    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        StringBuilder tBuffer = new StringBuilder();
        tBuffer.append(getId()).append("=").append(mValue);
        return tBuffer.toString();
    }
    
    /**
     * Construct a long key with all components.
     *
     * @param pUserId the user id
     * @param pPerspectiveId the perspective id
     * @param pSlotId the slot id
     * @param pViewId the view id
     * @param pItemId the item id
     * @param pPreference the preference
     * @return the string
     */
    public static String key(String pUserId, String pPerspectiveId, String pSlotId,
        String pViewId, String pItemId, String pPreference) {
        StringBuilder tBuffer = new StringBuilder();
        tBuffer.append(pUserId).append("+");
        tBuffer.append(pPerspectiveId).append("+");
        tBuffer.append(pSlotId).append("+");
        tBuffer.append(pViewId).append("+");
        tBuffer.append(pItemId).append("+");
        tBuffer.append(pPreference);
        return tBuffer.toString();
    }

    /**
     * Construct a medium key with user, item and preference.
     *
     * @param pUserId the user id
     * @param pItemId the item id
     * @param pPreference the preference
     * @return the string
     */
    public static String key(String pUserId, String pItemId, String pPreference) {
        return key(pUserId, PREF_NONE, PREF_NONE, PREF_NONE, pItemId, pPreference);
    }
    
    /**
     * Construct a short key with user and preference.
     *
     * @param pUserId the user id
     * @param pPreference the preference
     * @return the string
     */
    public static String key(String pUserId, String pPreference) {
        return key(pUserId, PREF_NONE, PREF_NONE, PREF_NONE, PREF_NONE, pPreference);
    }

}
