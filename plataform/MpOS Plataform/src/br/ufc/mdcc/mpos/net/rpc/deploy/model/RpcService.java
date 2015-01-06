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

import java.util.List;

import br.ufc.mdcc.mpos.net.util.Service;

/**
 * @author Philipp B. Costa
 */
public final class RpcService extends Service implements Comparable<RpcService> {
	private int id;
	private String versionApp;
	private List<DependencePath> dependencies;

	public RpcService() {
		this(null, -1, null, null);
	}

	public RpcService(String name, int port, String versionApp, List<DependencePath> dependences) {
		this(0, name, port, versionApp, dependences);
	}

	public RpcService(int id, String name, int port, String versionApp, List<DependencePath> dependences) {
		super(name, port);
		this.id = id;
		this.versionApp = versionApp;
		this.dependencies = dependences;
	}

	public final int getId() {
		return id;
	}

	public final void setId(int id) {
		this.id = id;
	}

	public final String getVersionApp() {
		return versionApp;
	}

	public final void setVersionApp(String versionApp) {
		this.versionApp = versionApp;
	}

	public final List<DependencePath> getDependencies() {
		return dependencies;
	}

	public final void setDependencies(List<DependencePath> dependencies) {
		this.dependencies = dependencies;
	}

	//process message receive from network
	public void processDeployMessage(String message) {
		String process[] = message.toLowerCase().split(":");
		setName(process[1]);
		setVersionApp(process[2]);
	}

	public int compareTo(RpcService o) {
		int strCmp = o.getName().compareTo(getName());
		if (strCmp == 0) {
			return o.versionApp.compareTo(versionApp);
		} else {
			return strCmp;
		}
	}
}