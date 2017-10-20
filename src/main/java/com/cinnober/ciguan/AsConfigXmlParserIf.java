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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.cinnober.ciguan.client.MvcModelAttributesIf;
import com.cinnober.ciguan.data.AsLocale;
import com.cinnober.ciguan.data.AsViewDefinition;

/**
 * Describes XML configuration parser functionality.
 */
public interface AsConfigXmlParserIf extends MvcModelAttributesIf {

    /** Singleton instance of this interface. */
    AsSingleton<AsConfigXmlParserIf> SINGLETON = new AsSingleton<AsConfigXmlParserIf>();

    /** Major version of xml configuration. */
    int VERSION_MAJOR = 9;

    /** Minor version of xml configuration. */
    int VERSION_MINOR = 2;

    /**
     * Get the configured get methods.
     *
     * @return the configured get methods.
     */
    List<CwfDataIf> getAsGetMethods();

    /**
     * Get a named parameter value.
     *
     * @param pParameterName The name of the parameter for which to get the value.
     * @return the parameter value, or {@code null} if the parameter does not exist.
     */
    String getAsParameter(String pParameterName);

    /**
     * Get the configured view definitions.
     *
     * @return a map containing all view definitions.
     */
    Map<String, AsViewDefinition> getViewDefinitions();

    /**
     * Get the configuration document element.
     *
     * @return the configuration document element.
     */
    Element getConfigurationDocument();

    /**
     * Get all locales.
     *
     * @return all defined locales.
     */
    Collection<AsLocale> getLocales();

    /**
     * Get all configured AS components.
     *
     * @return all configured components.
     */
    Collection<String> getConfiguredComponents();

}
