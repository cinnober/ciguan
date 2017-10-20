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
package com.cinnober.ciguan.client;

/**
 *
 * Client device information
 * 
 */
public interface MvcDeviceInfoIf extends MvcDeviceTypeIf {
    
    /**
     * @return the screen orientation at start-up
     */
    MvcScreenOrientationIf getInitialScreenOrientation();
    
    /**
     * @return the ratio between physical and independent (dips) screen pixels
     */
    double getDevicePixelRatio();
    
    /**
     * @return the screen width in pixels
     */
    int getScreenWidth();
    
    /**
     * @return the screen height in pixels
     */
    int getScreenHeight();
    
    /**
     * @return the window's navigator.appCodeName.
     */
    String getAppCodeName();

    /**
     * @return the window's navigator.appName.
     */
    String getAppName();

    /**
     * @return the window's navigator.appVersion.
     */
    String getAppVersion();

    /**
     * @return the window's navigator.platform.
     */
    String getPlatform();
    
    /**
     * @return the window's navigator.userAgent
     */
    String getUserAgent();
    
    /**
     * Singleton instance of me
     */
    public static class Singleton {

        static MvcDeviceInfoIf cInstance;

        public static MvcDeviceInfoIf get() {
            return cInstance;
        }

        public static void set(MvcDeviceInfoIf pType) {
            cInstance = pType;
        }
    }
    
}
