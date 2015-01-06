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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/**
 * The programmer write a custom serialization for sent params methods to remote
 * server and receive the return method from remote. This interface avoid the
 * MPoS uses the java built-in serialization method (heavy reflection) like
 * Parceable in Android framework. That interface must be used for speedup rpc
 * call. Useful in real-time aplications.
 * 
 * @author Philipp Costa
 */
public interface RpcSerializable {

	/**
	 * Write how the serializable method params
	 * 
	 * @param out
	 * @param methodName
	 * @param params
	 * @throws IOException
	 */
	public void writeMethodParams(DataOutput out, String methodName, Object params[]) throws IOException;

	/**
	 * Read how the deserializable method params
	 * 
	 * @param in
	 * @param methodName
	 * @return
	 * @throws IOException
	 */
	public Object[] readMethodParams(DataInput in, String methodName) throws IOException;

	/**
	 * Write how the serializable return param from method
	 * 
	 * @param out
	 * @param methodName
	 * @param returnParam
	 * @throws IOException
	 */
	public void writeMethodReturn(DataOutput out, String methodName, Object returnParam) throws IOException;

	/**
	 * Read how the deserializable the return param from method
	 * 
	 * @param in
	 * @param methodName
	 * @return
	 * @throws IOException
	 */
	public Object readMethodReturn(DataInput in, String methodName) throws IOException;

}