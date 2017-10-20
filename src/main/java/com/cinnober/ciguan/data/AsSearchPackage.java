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
package com.cinnober.ciguan.data;

import com.cinnober.ciguan.annotation.CwfDisplayName;
import com.cinnober.ciguan.annotation.CwfIdField;

/**
 * Search package information
 */
public class AsSearchPackage {

    @CwfIdField
    @CwfDisplayName
    public String packageName;
    
    public String namespace;
    
    public AsSearchPackage(String pPackageName, String pNamespace) {
        packageName = pPackageName;
        namespace = pNamespace;
    }
    
}
