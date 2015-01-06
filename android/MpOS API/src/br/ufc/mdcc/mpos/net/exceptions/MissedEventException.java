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
package br.ufc.mdcc.mpos.net.exceptions;

/**
 * @author Philipp B. Costa
 */
public final class MissedEventException extends Exception {
	private static final long serialVersionUID = -1580074066152767986L;

	public MissedEventException(String msg) {
		super(msg);
	}

	public MissedEventException() {
		this("Need define a ReceiveDataEvent for receive data from socket");
	}
}