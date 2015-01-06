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
using System.ComponentModel;

namespace Ufc.MpOS.Util.TaskOp
{
	public delegate void ProgressEventHandler(int percentage);
	public delegate void MessageProgressEventHandler(string message);
	public delegate void RunCompletedEventHandler<Result>(Result obj);
	
	public abstract class Job
	{
		protected BackgroundWorker worker;
		public event ProgressEventHandler ProgressChanged;
		public event MessageProgressEventHandler MessageProgressChanged;

		public Job()
		{
			worker = new BackgroundWorker();
			worker.WorkerReportsProgress = true;
			worker.WorkerSupportsCancellation = true;
			worker.DoWork += new DoWorkEventHandler(DoWork);
		}

		public void Execute()
		{
			if (worker.IsBusy != true)
			{
				worker.ProgressChanged += new ProgressChangedEventHandler(ProgressWorkerChanged);
				worker.RunWorkerCompleted += new RunWorkerCompletedEventHandler(RunWorkerCompleted);
				worker.RunWorkerAsync();
			}
		}

		public void Cancel()
		{
			worker.CancelAsync();
		}

		public void PublishProgress(int progress)
		{
			worker.ReportProgress(progress);
		}

		public void PublishProgress(int progress, string message)
		{
			worker.ReportProgress(progress, message);
		}

		private void ProgressWorkerChanged(object sender, ProgressChangedEventArgs e)
		{
			if (ProgressChanged != null)
			{
				if (e.UserState == null)
				{
					ProgressChanged(e.ProgressPercentage);
				}
				else
				{
					MessageProgressChanged((string)e.UserState);
				}
			}
		}

		protected abstract void RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e);

		protected abstract void DoWork(object sender, DoWorkEventArgs e);
	}

	public abstract class AsyncTask<Result> : Job
	{
		public event RunCompletedEventHandler<Result> RunCompleted;

		protected abstract Result DoInBackground();

		protected sealed override void DoWork(object sender, DoWorkEventArgs e)
		{
			e.Result = DoInBackground();
		}

		protected override sealed void RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
		{
			if (!e.Cancelled && e.Error == null)
			{
				RunCompleted((Result)e.Result);
			}
		}
	}
}