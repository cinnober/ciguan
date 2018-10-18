/*
 * 
 *
 * 
 * 
 *
 * This software is the confidential and proprietary information of
 * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Cinnober.
 *
 * Cinnober makes no representations or warranties about the suitability
 * of the software, either expressed or implied, including, but not limited
 * to, the implied warranties of merchantibility, fitness for a particular
 * purpose, or non-infringement. Cinnober shall not be liable for any
 * damages suffered by licensee as a result of using, modifying, or
 * distributing this software or its derivatives.
 */
package com.cinnober.ciguan.transport.plugin;

import java.util.UUID;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.AsSessionDataIf;
import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.CwfMessageIf;
import com.cinnober.ciguan.client.impl.MvcEventEnum;
import com.cinnober.ciguan.client.impl.MvcModelNames;
import com.cinnober.ciguan.data.AsClientSession;
import com.cinnober.ciguan.data.CwfDataFactory;
import com.cinnober.ciguan.data.CwfMessage;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.request.AsRequestServiceIf;

/**
 *
 * Plugin handling back-end request/response
 *
 */
public class AsRequestPlugin extends AsTransportPlugin {

    @Override
    public void onMessage(AsConnectionIf pConnection, CwfMessageIf pMessage) {

        if (pMessage.getName().equals(MvcModelNames.ServerRequest.name())) {
            // Check the request token validity
            if (checkRequestToken(pConnection, pMessage)) {

                // Pre-processing hook
                preProcessRequest(pConnection, pMessage);

                // This is a request to the back-end, call the request service
                AsRequestServiceIf tService = pConnection.getRequestService();
                CwfMessageIf tResponse = tService.send(pConnection, pMessage);
                if (tResponse != null) {
                    pConnection.getTransportService().addClientMessage(tResponse);

                    // Post-processing hook
                    postProcessRequest(pConnection, pMessage, tResponse);
                }

                // Generate a new request token and send it to the client
                AsSessionDataIf tSessionData = pConnection.getSessionData();
                tSessionData.setRequestToken(UUID.randomUUID().toString());
                CwfDataIf tEvent = CwfDataFactory.create(MvcEventEnum.UpdateRequestTokenEvent);
                tEvent.setProperty(ATTR_REQUEST_TOKEN, tSessionData.getRequestToken());
                pConnection.getTransportService().addClientMessage(new CwfMessage(tEvent, -1));
            }
        }

    }

    /**
     * Format a log entry when the XSRF check fails
     * @param pConnection the application server connection
     * @param pMessage the client message foir which the check failed
     * @return a formatted log entry string
     */
    protected String formatTokenMismatchLog(AsConnectionIf pConnection, CwfMessageIf pMessage) {
        StringBuilder tLog = new StringBuilder();
        tLog.append("Client request token does not match the session, possible CSRF attack.\n\n");

        tLog.append("Client request:  " + pMessage.getData()).append("\n");
        tLog.append("Client token:    " + pMessage.getData().getProperty(ATTR_REQUEST_TOKEN)).append("\n");
        tLog.append("Server token:    " + pConnection.getSessionData().getRequestToken()).append("\n");
        String tSessionId = pConnection.getSessionId();
        AsClientSession tSession = As.getGlobalDataSources().getDataSource(AsClientSession.class).get(tSessionId);
        tLog.append("SessionID:       " + tSessionId + ", created " + tSession.createdAt).append("\n");
        tLog.append("Browser details: " + tSession.browserDetails).append("\n");
        tLog.append("Logged in:       " + pConnection.getSessionData().isLoggedIn()).append("\n");
        tLog.append("User ID:         " +
            "session=" + tSession.userId + ", connection=" + pConnection.getSessionData().getUser()).append("\n");
        tLog.append("Acting user ID:  " + pConnection.getSessionData().getLoggedInUser()).append("\n");
        tLog.append("Act on behalf:   " + pConnection.getSessionData().isActOnBehalf()).append("\n\n");

        tLog.append("CALL STACK:").append("\n");
        StackTraceElement[] tStackTrace = Thread.currentThread().getStackTrace();
        // Ignore the two first entries (getStackTrace() itself and this method)
        for (int i = 2; i < tStackTrace.length; i++) {
            tLog.append("  ").append(tStackTrace[i]).append("\n");
        }
        return tLog.toString();
    }

    protected void preProcessRequest(AsConnectionIf pConnection, CwfMessageIf pMessage) {
        // No action, override when needed
    }

    protected void postProcessRequest(AsConnectionIf pConnection, CwfMessageIf pMessage, CwfMessageIf pResponse) {
        // No action, override when needed
    }

    protected boolean checkRequestToken(AsConnectionIf pConnection, CwfMessageIf pMessage) {
        String tToken = pConnection.getSessionData().getRequestToken();
        if (tToken == null || !tToken.equals(pMessage.getData().getProperty(ATTR_REQUEST_TOKEN))) {
            // Possible security breach, log and invalidate the session
            AsLoggerIf.Singleton.get().log(formatTokenMismatchLog(pConnection, pMessage));
            handleInvalidRequestToken(pConnection);
            return false;
        }
        return true;
    }

    /**
     * Handle an invalid request token.
     *
     * @param pConnection the application server connection
     */
    protected void handleInvalidRequestToken(AsConnectionIf pConnection) {
        // No action, override and implement back-end specific needs
    }

}
