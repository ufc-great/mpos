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

/**
 * @author Philipp B. Costa
 */
public final class RpcException extends Exception {
	private static final long serialVersionUID = -3681928269723415427L;

	private int codeError = -1;

	public RpcException() {
		this("Any error in RPC communication!");
	}

	public RpcException(String message) {
		super(message);
	}

	public RpcException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public RpcException(String message, int codeError) {
		this(message);
		this.codeError = codeError;
	}

	public int getCodeError() {
		return codeError;
	}
}