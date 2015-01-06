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
package br.ufc.mdcc.mpos.net.rpc.deploy.model;

/**
 * Dependence path has the .jar location on hard disk. 
 * 
 * @author Philipp B. Costa
 */
public final class DependencePath {
	private int id;
	private int idService;
	private String path;

	public DependencePath(String path) {
		this(0, 0, path);
	}

	public DependencePath(int idService, String path) {
		this(0, idService, path);
	}

	public DependencePath(int id, int idService, String path) {
		this.id = id;
		this.idService = idService;
		this.path = path;
	}

	public final int getId() {
		return id;
	}

	public final void setId(int id) {
		this.id = id;
	}

	public final int getIdService() {
		return idService;
	}

	public final String getPath() {
		return path;
	}
}