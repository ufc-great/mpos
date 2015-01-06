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
package br.ufc.mdcc.mpos.net.rpc.model;

/**
 * Flags used in RPC protocol.
 * 
 * @author Philipp B. Costa
 */
public final class Code {
	public static final byte OK = 0x01;

	public static final byte CLASS_NOT_FOUND = 0x02;

	public static final byte METHOD_THROW_ERROR = 0x03;

	public static final byte SERVER_ERROR = 0x04;

	public static final byte DATASTREAM = 0x15;

	public static final byte DATASTREAMDEBUG = 0x16;

	public static final byte OBJECTSTREAM = 0x25;

	public static final byte OBJECTSTREAMDEBUG = 0x26;
}
