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
package com.cinnober.ciguan;

/**
 * AS Component functionality .
 */
public interface AsComponentIf {

    /**
     * Start the component.
     *
     * @throws AsInitializationException if an initialization exception occurrs.
     */
    void startComponent() throws AsInitializationException;

    /**
     * All components started.
     *
     * @throws AsInitializationException if an initialization exception occurrs.
     */
    void allComponentsStarted() throws AsInitializationException;

    /**
     * Perform external data loading.
     */
    void synchronizeExternalData();
 
    /**
     * Reload internal component configuration.
     */
    void reloadConfiguration();
    
    /**
     * Stop this component.
     *
     * @throws AsInitializationException if an initialization exception occurrs.
     */
    void stopComponent() throws AsInitializationException;
    
    /**
     * All components have been stopped.
     *
     * @throws AsInitializationException if an initialization exception occurrs.
     */
    void allComponentsStopped() throws AsInitializationException;
    
}
