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
package com.cinnober.ciguan.service.impl;

import com.cinnober.ciguan.AsConnectionIf;
import com.cinnober.ciguan.service.AsServiceIf;

/**
 *
 * Base class for application server service implementations
 * 
 * Service implementations should typically add one or more service methods similar to the default method
 * which takes Object, but use late binding by specifying the exact request type(s).
 * 
 */
public abstract class AsService implements AsServiceIf {

    @Override
    public final Object service(AsConnectionIf pConnection, Object pRequest) {
        // We should never get here
        throw new UnsupportedOperationException(
            "Service class " + getClass().getName() +
            " is missing its implementation for parameter type " + pRequest.getClass().getName());
    }

}
