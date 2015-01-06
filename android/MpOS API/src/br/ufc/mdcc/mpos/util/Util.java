/*******************************************************************************
 * Copyright (C) 2014 Philipp B. Costa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.ufc.mdcc.mpos.util;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

/**
 * @author Philipp B. Costa
 */
public final class Util {
	private static Pattern patternIpAddress;
	private static final String IP_ADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	static {
		patternIpAddress = Pattern.compile(IP_ADDRESS_PATTERN);
	}

	private Util() {

	}

	/**
	 * Validate ip address with regular expression
	 * 
	 * @param ip
	 * @return true valid ip address, false invalid ip address
	 */
	public static boolean validateIpAddress(final String ip) {
		return patternIpAddress.matcher(ip).matches();
	}

	/**
	 * Esse metodo reconhece um padrão dentro de um array de bytes
	 * 
	 * @param source array que gostaria de buscar o padrao
	 * @param target array alvo que gostaria detectar o padrao neste array
	 * @return verdade se achou ou falso se não achou
	 */
	public static boolean containsArrays(byte source[], byte target[]) {
		return indexOfArrays(source, target, 0, target.length) > -1;
	}

	private static int indexOfArrays(byte target[], byte source[], int sourceOffset, int sourceCount) {
		int targetOffset = 0, targetCount = target.length;
		int fromIndex = 0;

		if (fromIndex >= targetCount) {
			return (sourceCount == 0 ? targetCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (sourceCount == 0) {
			return fromIndex;
		}

		byte first = source[sourceOffset];
		int max = targetOffset + (targetCount - sourceCount);

		for (int i = targetOffset + fromIndex; i <= max; i++) {
			/* Look for first character. */
			if (target[i] != first) {
				while (++i <= max && target[i] != first)
					;
			}
			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + sourceCount - 1;
				for (int k = sourceOffset + 1; j < end && target[j] == source[k]; j++, k++)
					;

				if (j == end) {
					/* Found whole string. */
					return i - targetOffset;
				}
			}
		}
		return -1;
	}
	
	public static byte[] convertIntToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
    
    public static int convertByteArrayToInt(byte[] value) {
        return ByteBuffer.wrap(value).getInt();
    }
}