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
using System;
using System.Reflection;

namespace Ufc.MpOS.Proxy
{
	/// <summary>
    /// Interface that a user defined proxy handler needs to implement.  This interface 
    /// defines one method that gets invoked by the generated proxy.  
	/// </summary>
	public interface InvocationHandler
	{
		Object Invoke(Object proxy, MethodInfo method, object[] methodAttributes, Object[] methodParams);
	}
}