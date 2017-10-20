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
 * Defines authorization functionality.
 */
public interface AsAuthorizationHandlerIf {

	/**
	 * Check if a user has access to the given service.
	 *
	 * @param pUserId The user id.
	 * @param pServiceRef The service ref.
	 * @return {@code true}, if successful.
	 */
	boolean checkAccess(String pUserId, Object pServiceRef);

	/**
	 * Adds access for the specified user to the provided service.
	 *
	 * @param pUserId The user id.
	 * @param pServiceRef The service ref.
	 */
	void addAccess(String pUserId, Object pServiceRef);

	/**
	 * Get the initialized status
	 * @return true if the authorization handler has been initialized with authorization data, otherwise false
	 */
	boolean isInitialized();

	/**
	 * Set the initialization status to true. To be called only from code which populates access data.
	 */
	void setInitialized();

	/**
	 *
	 * Singleton instance of the authorization handler.
	 */
	public static class Singleton {

		static AsAuthorizationHandlerIf cInstance;

		/**
		 * Retrieve the instance.
		 *
		 * @return the authorization handler.
		 */
		public static AsAuthorizationHandlerIf get() {
			return cInstance;
		}

		/**
		 * Creates the instance.
		 */
		public static void create() {
			cInstance = AsBeanFactoryIf.Singleton.get().create(AsAuthorizationHandlerIf.class);
		}
	}


}
