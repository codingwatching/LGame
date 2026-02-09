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
package loon.utils;

import loon.LSystem;

public class UNLong {

	private long unLong;

	public UNLong(int unsigned) {
		this.read(unsigned);
	}

	public UNLong(long tobecomeunsigned) {
		this.setValue(tobecomeunsigned);
	}

	public UNLong(ArrayByte bb, int offset) {
		this.read(bb, offset);
	}

	public UNLong(byte[] bytes, int offset) {
		this.read(bytes, offset);
	}

	public void read(int i) {
		byte[] bytes = new byte[8];
		for (int j = 0; j < 8; j++) {
			bytes[7 - j] = (byte) (i >>> (j * 8));
		}
		read(bytes, 0);
	}

	public void read(ArrayByte bb, int offset) {
		int initialPos = bb.position();
		bb.setPosition(offset);
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[i] = bb.get();
		}
		read(bytes, 0);
		bb.setPosition(initialPos);
	}

	public void read(byte[] bytes, int offset) {
		unLong = 0L;
		for (int i = 0; i < 8; i++) {
			unLong = (unLong << 8) | (bytes[offset + i] & 0xFFL);
		}
	}

	public byte[] write() {
		byte[] buf = new byte[8];
		buf[0] = (byte) (unLong >>> 56);
		buf[1] = (byte) (unLong >>> 48);
		buf[2] = (byte) (unLong >>> 40);
		buf[3] = (byte) (unLong >>> 32);
		buf[4] = (byte) (unLong >>> 24);
		buf[5] = (byte) (unLong >>> 16);
		buf[6] = (byte) (unLong >>> 8);
		buf[7] = (byte) (unLong >>> 0);
		return buf;
	}

	public boolean write(ArrayByte bb, int offset) {
		if (bb.limit() - 8 < offset) {
			return false;
		}
		bb.setPosition(offset);

		byte[] buf = new byte[8];
		buf[0] = (byte) (unLong >>> 56);
		buf[1] = (byte) (unLong >>> 48);
		buf[2] = (byte) (unLong >>> 40);
		buf[3] = (byte) (unLong >>> 32);
		buf[4] = (byte) (unLong >>> 24);
		buf[5] = (byte) (unLong >>> 16);
		buf[6] = (byte) (unLong >>> 8);
		buf[7] = (byte) (unLong >>> 0);

		bb.write(buf);
		return true;
	}

	public boolean write(byte[] bytes, int offset) {
		if (bytes.length - 8 < offset) {
			return false;
		}

		bytes[offset] = (byte) (unLong >>> 56);
		bytes[offset + 1] = (byte) (unLong >>> 48);
		bytes[offset + 2] = (byte) (unLong >>> 40);
		bytes[offset + 3] = (byte) (unLong >>> 32);
		bytes[offset + 4] = (byte) (unLong >>> 24);
		bytes[offset + 5] = (byte) (unLong >>> 16);
		bytes[offset + 6] = (byte) (unLong >>> 8);
		bytes[offset + 7] = (byte) (unLong >>> 0);
		return true;
	}

	public long getValue() {
		return unLong;
	}

	public void setValue(long value) {
		unLong = value;
	}

	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode = LSystem.unite(hashCode, unLong);
		return hashCode;
	}
}
