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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufc.mdcc.mpos.net.rpc.deploy.model.DependencePath;

/**
 * Class loader for jar files.
 * 
 * @author Philipp B. Costa
 */
public final class JarClassLoader {
	private ClassLoader classLoader;
	private Map<String, Class<?>> cache = new HashMap<String, Class<?>>();

	public JarClassLoader(ClassLoader thread, List<DependencePath> dependences) throws ClassLoaderException {
		URL urlJars[] = new URL[dependences.size()];
		int count = 0;
		for (DependencePath app : dependences) {
			try {
				urlJars[count++] = new File(app.getPath()).toURI().toURL();
			} catch (Exception e) {
				throw new ClassLoaderException("Error for loading jarPaths");
			}
		}
		classLoader = URLClassLoader.newInstance(urlJars, thread);
	}

	public synchronized Object newInstance(String clsName) throws InstantiationException, IllegalAccessException {
		Class<?> cls = getClassFromClassLoader(clsName);
		if (cls != null) {
			return cls.newInstance();
		}
		return null;
	}

	public synchronized Class<?> resolveClass(String clsName) {
		return getClassFromClassLoader(clsName);
	}

	private Class<?> getClassFromClassLoader(String clsName) {
		Class<?> cls = cache.get(clsName);
		if (cls != null) {
			return cls;
		}

		try {
			cls = Class.forName(clsName, true, classLoader);
			cache.put(clsName, cls);
			return cls;
		} catch (ClassNotFoundException e) {
		}

		return null;
	}
}