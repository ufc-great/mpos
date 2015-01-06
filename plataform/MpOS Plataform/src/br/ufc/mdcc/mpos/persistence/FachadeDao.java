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
package br.ufc.mdcc.mpos.persistence;

/**
 * Fachade for Data Access Objects.
 * This implementation is a singleton thread-safe.
 * 
 * @author Philipp B. Costa
 */
public final class FachadeDao {
	private static class SingletonHolder {
		public static final FachadeDao instance = new FachadeDao();
	}

	private NetProfileDao netProfileDao;
	private RpcServiceDao rpcServiceDao;

	private FachadeDao() {
		netProfileDao = new NetProfileDao();
		rpcServiceDao = new RpcServiceDao();
	}

	public static FachadeDao getInstance() {
		return SingletonHolder.instance;
	}

	public NetProfileDao getNetProfileDao() {
		return netProfileDao;
	}

	public RpcServiceDao getRpcServiceDao() {
		return rpcServiceDao;
	}
}