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
package com.cinnober.ciguan.client.util;



/**
 * Escapes and unescapes Strings mainly for use with CwfData serialization/deserialization
 */
public abstract class StringEscape {

	static String cTest = "#{}[],=\"";

	/**
	 * Escapes a subset of the given String matched by any one of the registered regular expressions.
	 * If the String is null then this method will return null.
	 * @param pString
	 * @return the escaped string
	 */
	public static String escape(String pString) {
		if (pString == null || pString.isEmpty()) {
			return pString;
		}
		StringBuilder tBuilder = new StringBuilder();
		for (int i = 0; i < pString.length(); i++) {
			char tChar  = pString.charAt(i);
			if (test(tChar)) {
				tBuilder.append("#").append(Integer.toString(tChar)).append(";");
			}
			else {
				tBuilder.append(tChar);
			}
		}
		return tBuilder.toString();
	}

	private static boolean test(char pC) {
		return cTest.indexOf(pC) >= 0;
	}

	/**
	 * Unescapes a the given String.
	 * If the given String is null then this method will return null.
	 * @param pString
	 * @return the unescaped string
	 */
	public static String unescape(String pString) {
		if (pString == null || pString.isEmpty()) {
			return pString;
		}
		StringBuilder tBuilder = new StringBuilder();
		for (int i = 0; i < pString.length(); i++) {
			char tChar  = pString.charAt(i);
			if (tChar == '#') {
				int tPos = pString.indexOf(';', i);
				int tValue = Integer.parseInt(pString.substring(i + 1, tPos));
				tBuilder.append((char) tValue);
				i = tPos;
			}
			else {
				tBuilder.append(tChar);
			}
		}
		return tBuilder.toString();
	}


}
