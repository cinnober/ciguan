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

import java.util.HashMap;
import java.util.Map;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.impl.MvcEventEnum;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.handler.AsFormHandlerIf;
import com.cinnober.ciguan.impl.As;

/**
 *
 * Plug-in responsible for processing form handler events.
 *
 * NOTE: Both the FormStartEvent and FormContextLookupEvent messages must make sure
 * that the form handler is registered, and if not, create and register it. This is because
 * we cannot determine which of these two events that arrives first. (It depends on how
 * the form is configured)
 *
 */
public class AsFormHandlerPlugin extends AsTransportPlugin {

    /** The form handlers. */
    protected Map<String, AsFormHandlerIf> mFormHandlers = new HashMap<String, AsFormHandlerIf>();

    @Override
    public void onMessage(AsConnectionIf pConnection, CwfMessage pMessage) {

        // Form startup
        if (MvcEventEnum.FormStartEvent.name().equals(pMessage.getName())) {
            onFormStart(pConnection, pMessage);
            return;
        }

        // Form context lookup
        if (MvcEventEnum.FormContextLookupEvent.name().equals(pMessage.getName())) {
            onFormContextLookup(pConnection, pMessage);
            return;
        }

        // Form submit
        if (MvcEventEnum.FormSubmitEvent.name().equals(pMessage.getName())) {
            onFormSubmit(pConnection, pMessage);
            return;
        }

        // Form shutdown
        if (MvcEventEnum.FormDestroyEvent.name().equals(pMessage.getName())) {
            onFormDestroy(pConnection, pMessage);
            return;
        }

        // Session model - page reloaded, remove all stray form handlers
        if (MvcModelNames.SessionModel.name().equals(pMessage.getName())) {
            clear();
            return;
        }

    }

    /**
     * Ensures that the form handler gets created and registered.
     *
     * @param pMessage the message from the client
     * @return the form handler instance, or null if an error occurred
     */
    protected AsFormHandlerIf ensureFormHandler(CwfMessage pMessage) {

        AsFormHandlerIf tFormHandler = null;
        try {
            String tMvcInstanceId = pMessage.getData().getProperty(ATTR_MVC_INSTANCE_ID);
            assert tMvcInstanceId != null && !tMvcInstanceId.isEmpty();

            // If the handler does not exist, attempt to create and register it
            tFormHandler = mFormHandlers.get(tMvcInstanceId);
            if (tFormHandler == null) {

                // Instantiate a form handler
                String tFormHandlerClass = pMessage.getData().getProperty(ATTR_FORM_HANDLER);
                assert tFormHandlerClass != null && !tFormHandlerClass.isEmpty();

                Class<?> tClass = Class.forName(tFormHandlerClass);
                if (!AsFormHandlerIf.class.isAssignableFrom(tClass)) {
                    throw new IllegalArgumentException("Type '" + tClass.getName() +
                        "' is not a valid form handler");
                }
                Object tObject = tClass.newInstance();
                tFormHandler = (AsFormHandlerIf) tObject;

                // Save the form handler instance
                mFormHandlers.put(tMvcInstanceId, tFormHandler);
            }
        }
        catch (Throwable e) {
            tFormHandler = null;
        }
        return tFormHandler;
    }

    /**
     * Form startup.
     *
     * @param pConnection the connection
     * @param pMessage the message
     */
    protected void onFormStart(AsConnectionIf pConnection, CwfMessage pMessage) {

        String tFormHandlerClass = pMessage.getData().getProperty(ATTR_FORM_HANDLER);
        String tMvcInstanceId = pMessage.getData().getProperty(ATTR_MVC_INSTANCE_ID);
        AsFormHandlerIf tFormHandler = ensureFormHandler(pMessage);

        if (tFormHandler != null) {
            try {
                // Execute the start-up
                CwfDataIf tReturnContext = tFormHandler.onFormStart(pConnection, pMessage);

                // If the handler returned something, pass it as a context event, otherwise just
                // send a normal Ok response
                if (tReturnContext != null) {
                    tReturnContext.setProperty(ATTR_MODEL_NAME, MvcEventEnum.FormContextEvent.name());
                    pConnection.getTransportService().addClientMessage(
                        new CwfMessage(tReturnContext, pMessage.getHandle()));
                }
                else {
                    sendOkResponse(pConnection, pMessage.getHandle());
                }
            }
            catch (Throwable e) {
                String tMessage = "Exception during onFormStart() in form handler of type " + tFormHandlerClass;
                AsLoggerIf.Singleton.get().logThrowable(tMessage, e);
                sendErrorResponse(pConnection, pMessage.getHandle(),
                    As.STATUS_CODE_NOK, tMessage + ": " + e.getMessage());
            }
        }
        else {
            String tMessage = "Form handler of type " + tFormHandlerClass +
                " not found during onFormStart(), MVC instance ID=" + tMvcInstanceId;
            AsLoggerIf.Singleton.get().log(tMessage);
            sendErrorResponse(pConnection, pMessage.getHandle(), As.STATUS_CODE_NOK, tMessage);
        }
    }

    /**
     * Form context lookup.
     *
     * @param pConnection the connection
     * @param pMessage the message
     */
    protected void onFormContextLookup(AsConnectionIf pConnection, CwfMessage pMessage) {

        String tFormHandlerClass = pMessage.getData().getProperty(ATTR_FORM_HANDLER);
        String tMvcInstanceId = pMessage.getData().getProperty(ATTR_MVC_INSTANCE_ID);
        AsFormHandlerIf tFormHandler = ensureFormHandler(pMessage);

        if (tFormHandler != null) {
            try {
                tFormHandler.onFormContextLookup(pConnection, pMessage);
                // No ok message in this case since we may only send one response to the handle
            }
            catch (Throwable e) {
                String tMessage =
                    "Exception while handling form context lookup, form handler type=" +
                    tFormHandler.getClass().getName() +
                    ", MVC instance ID=" + tMvcInstanceId;
                AsLoggerIf.Singleton.get().logThrowable(tMessage, e);
                sendErrorResponse(pConnection, pMessage.getHandle(), As.STATUS_CODE_NOK,
                    tMessage + ": " + e.getMessage());
            }
        }
        else {
            String tMessage = "Form handler of type " + tFormHandlerClass +
                " not found during onFormContextLookup(), MVC instance ID=" + tMvcInstanceId;
            AsLoggerIf.Singleton.get().log(tMessage);
            sendErrorResponse(pConnection, pMessage.getHandle(), As.STATUS_CODE_NOK, tMessage);
        }
    }

    /**
     * Form submission.
     *
     * @param pConnection the connection
     * @param pMessage the message
     */
    protected void onFormSubmit(AsConnectionIf pConnection, CwfMessage pMessage) {
        String tMvcInstanceId = pMessage.getData().getProperty(ATTR_MVC_INSTANCE_ID);
        assert tMvcInstanceId != null && !tMvcInstanceId.isEmpty();
        assert mFormHandlers.containsKey(tMvcInstanceId);

        // Get the form handler and run the onFormDestroy method
        AsFormHandlerIf tFormHandler = mFormHandlers.get(tMvcInstanceId);
        if (tFormHandler != null) {
            try {
                tFormHandler.onFormSubmit(pConnection, pMessage);
                sendOkResponse(pConnection, pMessage.getHandle());
            }
            catch (Throwable e) {
                String tMessage =
                    "Exception while handling form submit, form handler type=" +
                    tFormHandler.getClass().getName() +
                    ", MVC instance ID=" + tMvcInstanceId;
                AsLoggerIf.Singleton.get().logThrowable(tMessage, e);
                sendErrorResponse(pConnection, pMessage.getHandle(), As.STATUS_CODE_NOK,
                    tMessage + ": " + e.getMessage());
            }
        }
        else {
            String tMessage =
                "Form handler not found while handling form submit, MVC instance ID=" + tMvcInstanceId;
            AsLoggerIf.Singleton.get().log(tMessage);
            sendErrorResponse(pConnection, pMessage.getHandle(), As.STATUS_CODE_NOK, tMessage);
        }
    }

    /**
     * Form shutdown.
     *
     * @param pConnection the connection
     * @param pMessage the message
     */
    protected void onFormDestroy(AsConnectionIf pConnection, CwfMessage pMessage) {
        String tMvcInstanceId = pMessage.getData().getProperty(ATTR_MVC_INSTANCE_ID);
        assert tMvcInstanceId != null && !tMvcInstanceId.isEmpty();
        assert mFormHandlers.containsKey(tMvcInstanceId);

        // Get (and remove) the form handler and run the onFormDestroy method
        AsFormHandlerIf tFormHandler = mFormHandlers.remove(tMvcInstanceId);
        if (tFormHandler != null) {
            try {
                tFormHandler.onFormDestroy(pConnection, pMessage);
                tFormHandler.destroy();
                sendOkResponse(pConnection, pMessage.getHandle());
            }
            catch (Throwable e) {
                String tMessage =
                    "Exception while handling form destruction, form handler type=" +
                    tFormHandler.getClass().getName() +
                    ", MVC instance ID=" + tMvcInstanceId;
                AsLoggerIf.Singleton.get().logThrowable(tMessage, e);
                sendErrorResponse(pConnection, pMessage.getHandle(), As.STATUS_CODE_NOK,
                    tMessage + ": " + e.getMessage());
            }
        }
        else {
            String tMessage =
                "Form handler not found while handling form destruction, MVC instance ID=" + tMvcInstanceId;
            AsLoggerIf.Singleton.get().log(tMessage);
            sendErrorResponse(pConnection, pMessage.getHandle(), As.STATUS_CODE_NOK, tMessage);
        }
    }

    /**
     * Toss references to all form handlers.
     */
    protected void clear() {
        // Toss references to all form handlers (should normally be empty)
        for (AsFormHandlerIf tHandler : mFormHandlers.values()) {
            AsLoggerIf.Singleton.get().log("Warning, stray form handler detected: " +
                tHandler.getClass().getName());
            tHandler.destroy();
        }
        mFormHandlers.clear();
    }

    /**
     * {@inheritDoc}
     *
     * During the reset process, all the references to the form handlers are cleared as well.
     * @see #clear()
     */
    @Override
    public void reset(AsConnectionIf pConnection) {
        super.reset(pConnection);
        clear();
    }

}
