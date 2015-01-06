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

namespace Ufc.MpOS.Proxy
{
    public interface IProxy
    {
		/*
		 * Proxy defines methods for creating dynamic proxy classes and instances. 
		 * 
		 * A proxy class implements a declared set of interfaces and delegates method 
		 * invocations to an InvocationHandler.
		 */
		object NewProxyInstance(object instanceObject, Type objInterface, InvocationHandler handler);
    }
}