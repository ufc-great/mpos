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
using System.IO;

namespace Ufc.MpOS.Util
{
	public sealed class Logger
	{
		private Type typeCls;
		private static TextWriter writer = null;
		private static bool ConsoleOutput = false;
		private static Level levelLog;

		private Logger(Type typeCls)
		{
			this.typeCls = typeCls;
		}

		public static void Configure(string path)
		{
			Configure(path, false);
		}

		public static void Configure(string path, bool consoleOutput)
		{
			Configure(path, consoleOutput, Level.ALL);
		}

		public static void Configure(string path, bool consoleOutput, Level level)
		{
			levelLog = level;
			if (writer == null)
			{
				writer = File.AppendText(path);
				Logger.ConsoleOutput = consoleOutput;
			}
		}

		public static Logger GetLogger(Type typeCls)
		{
			return new Logger(typeCls);
		}

		public void Debug(string message)
		{
			if (levelLog == Level.DEBUG || levelLog == Level.ALL)
			{
				string log = String.Format("DEBUG: {0} {1} >> {2} - {3}", DateTime.Now.ToLongDateString(),
				DateTime.Now.ToLongTimeString(), typeCls.Name, message);

				writer.WriteLine(log);
				writer.Flush();
				if (Logger.ConsoleOutput)
				{
					Console.WriteLine(log);
				}
			}
		}

		public void Info(string message)
		{
			if (levelLog == Level.INFO || levelLog == Level.ALL)
			{
				string log = String.Format("INFO: {0} {1} >> {2} - {3}", DateTime.Now.ToLongDateString(),
				DateTime.Now.ToLongTimeString(), typeCls.Name, message);

				writer.WriteLine(log);
				writer.Flush();
				if (Logger.ConsoleOutput)
				{
					Console.WriteLine(log);
				}
			}
		}

		public void Warn(string message)
		{
			if (levelLog == Level.WARN || levelLog == Level.ALL)
			{
				string log = String.Format("WARN: {0} {1} >> {2} - {3}", DateTime.Now.ToLongDateString(),
				DateTime.Now.ToLongTimeString(), typeCls.Name, message);

				writer.WriteLine(log);
				writer.Flush();
				if (Logger.ConsoleOutput)
				{
					Console.WriteLine(log);
				}
			}
		}

		public void Error(string message, Exception e)
		{
			Error(message + "\n" + e.ToString());
		}

		public void Error(string message)
		{
			if (levelLog == Level.ERROR || levelLog == Level.ALL)
			{
				string log = String.Format("ERROR: {0} {1} >> {2} - {3}", DateTime.Now.ToLongDateString(),
				DateTime.Now.ToLongTimeString(), typeCls.Name, message);

				writer.WriteLine(log);
				writer.Flush();
				if (Logger.ConsoleOutput)
				{
					Console.WriteLine(log);
				}
			}
		}

		public void Fatal(string message)
		{
			if (levelLog == Level.FATAL || levelLog == Level.ALL)
			{
				string log = String.Format("FATAL: {0} {1} >> {2} - {3}", DateTime.Now.ToLongDateString(),
				DateTime.Now.ToLongTimeString(), typeCls.Name, message);

				writer.WriteLine(log);
				writer.Flush();
				if (Logger.ConsoleOutput)
				{
					Console.WriteLine(log);
				}
			}
		}

		public enum Level
		{
			DEBUG, INFO, WARN, ERROR, FATAL, ALL
		}
	}
}