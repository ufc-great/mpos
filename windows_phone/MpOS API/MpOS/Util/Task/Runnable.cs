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
using System.Diagnostics;
using System.Threading;

namespace Ufc.MpOS.Util.TaskOp
{
	public abstract class Runnable
	{
		private Thread thread;
		protected volatile bool running = false;

		protected abstract void Run();

		public void Start()
		{
			if (thread == null)
			{
				running = true;
				thread = new Thread(new ThreadStart(Run));

				thread.Start();
			}
			else
			{
				throw new ThreadStateException("The thread has already been started.");
			}
		}

		public void Stop()
		{
			running = false;
		}

		public void Join()
		{
			thread.Join();
		}

		public void Join(int timeout)
		{
			thread.Join(timeout);
		}

		public void Interrupt()
		{
			thread.Abort();
		}

		public bool IsAlive()
		{
			return thread.IsAlive;
		}

		public void RunSynchronous()
		{
			Start();
			Join();
		}
	}
}