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
package com.cinnober.ciguan.transport.plugin;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.AsRootIf;
import com.cinnober.ciguan.AsSessionDataIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcUserPreferenceAttributesIf;
import com.cinnober.ciguan.client.impl.MvcEventEnum;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.client.impl.MvcRequestEnum;
import com.cinnober.ciguan.data.AsClientSession;
import com.cinnober.ciguan.data.AsClientUserSettings;
import com.cinnober.ciguan.data.AsLocale;
import com.cinnober.ciguan.data.AsResetUserPropertiesReq;
import com.cinnober.ciguan.data.AsSelectLanguageReq;
import com.cinnober.ciguan.data.AsSelectPerspectiveReq;
import com.cinnober.ciguan.data.AsUserPreference;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceOwnerIf;
import com.cinnober.ciguan.datasource.AsDataSourceServiceIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.transport.AsRequestTransformerIf;

/**
 * Plugin handling user session related events such as login and logout.
 */
public class AsSessionPlugin extends AsTransportPlugin implements MvcUserPreferenceAttributesIf {

    // TODO: how to get today's date
    /** The Constant cToday. */
    protected static final String cToday = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    /** The transformer. */
    protected final AsRequestTransformerIf mTransformer;

    /**
     * Instantiates a new as session plugin.
     */
    public AsSessionPlugin() {
        mTransformer = As.getBeanFactory().create(AsRequestTransformerIf.class);
    }

    /**
     * populate session data.
     *
     * @param pConnection the connection
     * @param pData the data
     */
    protected void populateSessionData(AsConnectionIf pConnection, CwfDataIf pData) {
        AsSessionDataIf tSessionData = pConnection.getSessionData();

        // Generate a new request token
        tSessionData.setRequestToken(UUID.randomUUID().toString());

        // Populate response model properties
        pData.setProperty(ATTR_USER_ID, tSessionData.getUser());
        pData.setProperty(ATTR_DATE, cToday);
        pData.setProperty(ATTR_LANGUAGE_CODE, tSessionData.getLocale().toString());
        pData.setProperty(ATTR_REQUEST_TOKEN, tSessionData.getRequestToken());

        // set locale object
        AsListIf<AsLocale> tLocaleList = As.getGlobalDataSources().getDataSource(AsLocale.class);
        AsLocale tAsLocale = tLocaleList.get(tSessionData.getLocale().toString());
        CwfDataIf tLocaleData = pConnection.getRequestService().transform(pConnection, tAsLocale);
        pData.setObject(ATTR_AS_LOCALE, tLocaleData);

        // Set client settings
        AsListIf<AsUserPreference> tPrefList =
            As.getGlobalDataSources().getDataSource(AsUserPreference.class);
        String tPrefId = AsUserPreference.key(pConnection.getSessionData().getUser(), ATTR_CLIENT_USER_SETTINGS);
        AsUserPreference tSettings = tPrefList.get(tPrefId);
        if (tSettings != null) {
//            CwfDataParser tParser = new CwfDataParser(tSettings.getValue());
//            pData.setObject(ATTR_CLIENT_USER_SETTINGS, tParser.parseCwfData());
        }
        else {
            // TODO: Populate the system default settings
            CwfDataIf tClientDefaultSettings = CwfDataFactory.create();
            tClientDefaultSettings.setProperty("showNotificationPopup", true);
            pData.setObject(ATTR_CLIENT_USER_SETTINGS, tClientDefaultSettings);
        }

    }

    @Override
    public void onMessage(AsConnectionIf pConnection, CwfMessage pMessage) {

        CwfDataIf tData = pMessage.getData();
        // Query session state
        if (pMessage.getName().equals(MvcModelNames.SessionModel.name())) {
            // Populate the session data and store a copy of it
            populateSessionData(pConnection, tData);
            pConnection.getSessionData().setClientSession(tData);
            // Return the response and exit
            pConnection.getTransportService().addClientMessage(pMessage);
            return;
        }

        // Login
        if (pMessage.getName().equals(MvcModelNames.SessionRequest.name())) {
            String tObjectType = pMessage.getData().getProperty(ATTR_OBJECT_NAME);
            if (tObjectType.equals(As.getTypeName(AsSelectLanguageReq.class))) {
                setLocale(pConnection, pMessage);
                return;
            }
            if (tObjectType.equals(As.getTypeName(AsSelectPerspectiveReq.class))) {
                setPerspective(pConnection, pMessage);
                return;
            }
            if (tObjectType.equals(As.getTypeName(AsResetUserPropertiesReq.class))) {
                resetUserProperties(pConnection, pMessage);
                return;
            }
            if (tObjectType.equals(As.getTypeName(AsClientUserSettings.class))) {
                saveClientUserSettings(pConnection, pMessage);
                return;
            }
        }

        // Personalization request, store in the data source
        if (pMessage.getName().equals(MvcRequestEnum.UserPreferenceRequest.name())) {
            String tPerspective = tData.getProperty(ATTR_PERSPECTIVE);
            String tSlot = tData.getProperty(ATTR_SLOT);
            String tView = tData.getProperty(ATTR_VIEW);
            String tItem = tData.getProperty(ATTR_ITEM);
            String tPreference = tData.getProperty(ATTR_PREFERENCE);
            String tValue = tData.getProperty(ATTR_VALUE);
            if (tValue == null) {
//                tValue = SerializationUtil.toJson(tData.getObject(ATTR_LAYOUT));
            }
            if (tValue != null) {
                AsUserPreference tPref = new AsUserPreference(pConnection.getSessionData().getUser(),
                    tPerspective, tSlot, tView, tItem, tPreference, tValue);
                if (isValidPreference(pConnection, tPref)) {
                    As.getBdxHandler().broadcast(tPref);
                }
            }
            sendOkResponse(pConnection, pMessage.getHandle());
            return;
        }

        // Personalization request, reset perspective
        if (pMessage.getName().equals(MvcRequestEnum.ResetPerspectiveRequest.name())) {
            String tPerspective = tData.getProperty(ATTR_PERSPECTIVE);

            AsDataSourceServiceIf tDsSvc = pConnection.getDataSourceService();
            AsDataSourceIf<AsUserPreference> tDs = tDsSvc.getDataSource("USER_PREFERENCES_ALL", null);
            AsListIf<AsUserPreference> tList = (AsListIf<AsUserPreference>) tDs;

            List<AsUserPreference> tToRemove = new ArrayList<AsUserPreference>();
            for (AsUserPreference tPref : tList.values()) {
                if (tPref.getPerspectiveId().equals(tPerspective)) {
                    tToRemove.add(tPref);
                }
            }
            for (AsUserPreference tPref : tToRemove) {
                tPref.deleted = true;
                As.getBdxHandler().broadcast(tPref);
            }

            // Return the request and exit
            pConnection.getTransportService().addClientMessage(pMessage);
            return;
        }

        // TODO: Move to another plugin?
        if (pMessage.getName().equals(MvcRequestEnum.ReloadConfigurationRequest.name())) {
            As.reloadConfiguration();

            // Push a page reload event
            CwfDataIf tEvent = CwfDataFactory.create(MvcEventEnum.PageReloadEvent);
            CwfMessage tMessage = new CwfMessage(tEvent, 0);
            pConnection.getTransportService().addClientMessage(tMessage);
            return;
        }
    }

    /**
     * Test whether a user preference is valid - used as a stub, can be expanded on if needed
     *
     * NB. This method should in theory not be necessary, but in some odd browser instances,
     * a pixel width, which should be an integral value, is returned as a floating point number.
     * So far we have been unable to reproduce it despite numerous attempts with a large number
     * of different browsers and using various zoom levels.
     *
     * @param pConnection the application server connection
     * @param pPreference the preference to be stored
     * @return {@code true} if the preference is valid, otherwise false
     */
    protected boolean isValidPreference(AsConnectionIf pConnection, AsUserPreference pPreference) {

        // CWFDEV-66 pain killer, try to see if a column width is a floating point number.
        // If it is, log as much as possible to the console as this problem has eluded replication
        // in local development environments
        if (pPreference.getId().endsWith("+width")) {
            try {
                Integer.parseInt(pPreference.getValue());
                return true;
            }
            catch (NumberFormatException e) {
                AsClientSession tClientSession = AsRootIf.Singleton.get().getClientSession(pConnection.getSessionId());
                AsLoggerIf.Singleton.get().log("FLOATING POINT COLUMN WIDTH DETECTED!\n" +
                    "Session ID:      " + pConnection.getSessionId() + "\n" +
                    "User ID:         " + pConnection.getSessionData().getUser() + "\n" +
                    "Session start:   " + tClientSession.createdAt + "\n" +
                    "Browser details: " + browserDetails(tClientSession));
                return false;
            }
        }
        return true;
    }

    /**
     * Get the browser details from the client session - need to use reflection to be backwards compatible
     * since browser details were added quite recently.
     *
     * @param pClientSession the client session
     * @return browser details, or an empty string
     */
    protected String browserDetails(AsClientSession pClientSession) {
        try {
            Field tField = pClientSession.getClass().getField("browserDetails");
            Object tDetails = tField.get(pClientSession);
            return tDetails != null ? tDetails.toString() : "(null)";
        }
        catch (Exception e) {
            return "(not available)";
        }
    }

    /**
     * Reset user properties.
     *
     * @param pConnection the connection
     * @param pMessage the message
     */
    protected void resetUserProperties(AsConnectionIf pConnection, CwfMessage pMessage) {
        // Empty the user data source
        // NOTE: We cannot do a clear on the list since it is a child, instead we have to emulate
        // remove broadcasts
        CwfDataIf tData = pMessage.getData();
        String tUserId = tData.getProperty(ATTR_USER_ID);
        AsDataSourceOwnerIf tOwner = As.getUserDataSources(getMemberId(tUserId), tUserId);
        AsDataSourceIf<AsUserPreference> tPrefs = tOwner.getDataSource("USER_PREFERENCES_ALL", null, null);

        AsListIf<AsUserPreference> tList = (AsListIf<AsUserPreference>) tPrefs;
        for (AsUserPreference tPref : new ArrayList<AsUserPreference>(tList.values())) {
            AsUserPreference tRemovedPref = new AsUserPreference(tPref);
            tRemovedPref.deleted = true;
            As.getBdxHandler().broadcast(tRemovedPref);
        }

        // Let the property persister clear the persisted properties too
        As.getUserPropertyPersister().resetUserProperties(pMessage.getData().getProperty(ATTR_USER_ID));

        // Return a success response and exit
        sendOkResponse(pConnection, pMessage.getHandle());
    }

    /**
     * Retrieve the ID of the member to which the user belongs
     * @param pUserId the ID of the user
     * @return a member ID
     */
    protected String getMemberId(String pUserId) {
        // FIXME: What should we return here?
        return null;
    }

    /**
     * Save client user settings.
     *
     * @param pConnection the connection
     * @param pMessage the message
     */
    protected void saveClientUserSettings(AsConnectionIf pConnection, CwfMessage pMessage) {
//        CwfDataIf tPreferences = pMessage.getData();
//        AsUserPreference tPreference = new AsUserPreference(
//            pConnection.getSessionData().getUser(), PREF_CLIENT_USER_SETTINGS, SerializationUtil.toJson(tPreferences));
//        As.getBdxHandler().broadcast(tPreference);

        // Return a success response and exit
        sendOkResponse(pConnection, pMessage.getHandle());
    }

    /**
     * Sets the locale.
     *
     * @param pConnection the connection
     * @param pMessage the message
     */
    protected void setLocale(AsConnectionIf pConnection, CwfMessage pMessage) {
        String tLocale = pMessage.getData().getProperty(ATTR_LANGUAGE_CODE);
        pConnection.setLocale(tLocale);
        CwfDataIf tData = CwfDataFactory.create(MvcModelNames.SelectLocaleResponse);
        if (pConnection.getLocale() != null) {
            tData.setProperty(ATTR_LANGUAGE_CODE, pConnection.getLocale().toString());
            tData.setProperty(ATTR_STATUS_CODE, As.STATUS_CODE_OK);
        }
        CwfMessage tResponse = new CwfMessage(tData, pMessage.getHandle());
        pConnection.getTransportService().addClientMessage(tResponse);
    }

    /**
     * Sets the perspective.
     *
     * @param pConnection the connection
     * @param pMessage the message
     */
    protected void setPerspective(AsConnectionIf pConnection, CwfMessage pMessage) {
        String tPerspective = pMessage.getData().getProperty(ATTR_PERSPECTIVE);

        CwfDataIf tData = CwfDataFactory.create(MvcModelNames.SelectPerspectiveResponse);
        tData.setProperty(ATTR_PERSPECTIVE, tPerspective);
        tData.setProperty(ATTR_STATUS_CODE, As.STATUS_CODE_OK);
        CwfMessage tResponse = new CwfMessage(tData, pMessage.getHandle());
        pConnection.getTransportService().addClientMessage(tResponse);
    }

    /**
     * Transform the response and post it to the client queue.
     *
     * @param pConnection the connection
     * @param pHandle the handle
     * @param pRsp the response
     */
    protected void createResponse(AsConnectionIf pConnection, int pHandle, Object pRsp) {
        CwfDataIf tData = mTransformer.transform(pConnection, pRsp);
        CwfMessage tResponse = new CwfMessage((CwfDataIf) tData, pHandle);
        pConnection.getTransportService().addClientMessage(tResponse);
    }

}
