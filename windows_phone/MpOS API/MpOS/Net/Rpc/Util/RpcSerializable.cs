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
using System.IO;

namespace Ufc.MpOS.Net.Rpc.Util
{
	/**
	 * The programmer write a custom serialization for sent params methods to remote server and receive the return method from remote. This interface
	 * avoid the MPoS uses the java built-in serialization method (heavy reflection) like Parceable in Android framework. That interface must be used for
	 * speedup rpc call. Useful in real-time aplications.
     * 
	 * @author Philipp
	 */
	public interface RpcSerializable
	{
		/**
		 * Write how the serializable method params
		 */
		void WriteMethodParams(BinaryWriter writer, string methodName, object[] methodParams);

		/**
		 * Read how the deserializable method params
		 */
		object[] ReadMethodParams(BinaryReader reader, string methodName);

		/**
		 * Write how the serializable return param from method
		 */
		void WriteMethodReturn(BinaryWriter writer, string methodName, object returnParam);

		/**
		 * Read how the deserializable the return param from method
		 */
		object ReadMethodReturn(BinaryReader reader, string methodName);
	}
}