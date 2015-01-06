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
package br.ufc.mdcc.mpos.net.rpc.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Get the total read data in <BufferedInputStream>
 * 
 * @author Philipp B. Costa
 */
public final class DebugBufferedInputStream extends BufferedInputStream {
	private int totalReadData = 0;

	public DebugBufferedInputStream(InputStream in) {
		super(in);
	}

	public DebugBufferedInputStream(InputStream in, int size) {
		super(in, size);
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		int read = super.read(buffer);
		totalReadData += read;
		return read;
	}

	@Override
	public synchronized int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		int read = super.read(buffer, byteOffset, byteCount);
		totalReadData += read;
		return read;
	}

	@Override
	public synchronized int read() throws IOException {
		totalReadData++;
		return super.read();
	}

	public int getTotalReadData() {
		return totalReadData;
	}
}