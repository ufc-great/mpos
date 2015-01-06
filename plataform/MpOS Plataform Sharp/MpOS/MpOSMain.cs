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
using Ufc.MpOS.Controller;
using Ufc.MpOS.Util;

/**
 * Notice:
 * 
 * -This project need for some reflections cases to adding the dependences "Microsoft.Phone" and "System.Windows"
 * 
 *		Right click on References -> Add reference -> Browse -> 
 *		Go to “C:\Program Files (x86)\Microsoft SDKs\Windows Phone\v8.0\Tools\MDILXAPCompile\Framework” -> 
 *		Find the "System.Windows.dll" and "Microsoft.Phone.dll" file -> Click on Add -> Click OK.
 */
namespace Ufc.MpOS
{
	sealed class MpOSMain
	{
		public static string PATH;
		
		static void Main(string[] args)
		{
			Logger.Configure("mpos_server_wp.log", true);

			EnvironmentFramework.MPOS_PATH = args[0];

			Properties properties = new Properties();
			properties.Load(EnvironmentFramework.MPOS_PATH + "config.properties");

			ServiceController.Instance.Start(properties);
		}
	}
}