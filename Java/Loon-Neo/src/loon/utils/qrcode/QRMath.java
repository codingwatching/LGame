/**
 * Copyright 2008 - 2019 The Loon Game Engine Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.5
 */
package loon.utils.qrcode;

import loon.LSysException;

public class QRMath {

	private final static class MathTable {

		final static int[] EXP_TABLE = new int[256];
		final static int[] LOG_TABLE = new int[256];
		static {
			for (int i = 0; i < 8; i++) {
				EXP_TABLE[i] = 1 << i;
			}
			for (int i = 8; i < 256; i++) {
				EXP_TABLE[i] = EXP_TABLE[i - 4] ^ EXP_TABLE[i - 5] ^ EXP_TABLE[i - 6] ^ EXP_TABLE[i - 8];
			}
			for (int i = 0; i < 255; i++) {
				LOG_TABLE[EXP_TABLE[i]] = i;
			}
		}
	}

	public static int glog(int n) {
		if (n < 1) {
			throw new LSysException("log(" + n + ")");
		}
		return MathTable.LOG_TABLE[n];
	}

	public static int gexp(int n) {
		while (n < 0) {
			n += 255;
		}
		while (n >= 256) {
			n -= 255;
		}
		return MathTable.EXP_TABLE[n];
	}
}
