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
package br.ufc.mdcc.mpos.net.rpc.model;

/**
 * Class used for information in debugs stream
 * 
 * @author Philipp B. Costa
 */
public final class RpcProfile {
	private int id;

	private long executionCpuTime;

	private long uploadTime;
	private long donwloadTime;
	private int uploadSize;
	private int downloadSize;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getExecutionCpuTime() {
		return executionCpuTime;
	}

	public void setExecutionCpuTime(long executionCpuTime) {
		this.executionCpuTime = executionCpuTime;
	}

	public void setExecutionCpuTimeLocal(long executionCpuTime) {
		this.executionCpuTime = executionCpuTime;
		donwloadTime = 0L;
		downloadSize = 0;
		uploadTime = 0L;
		uploadSize = 0;
	}

	public long getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(long uploadTime) {
		this.uploadTime = uploadTime;
	}

	public long getDonwloadTime() {
		return donwloadTime;
	}

	public void setDonwloadTime(long donwloadTime) {
		this.donwloadTime = donwloadTime;
	}

	public int getDownloadSize() {
		return downloadSize;
	}

	public void setDownloadSize(int downloadSize) {
		this.downloadSize = downloadSize;
	}

	public int getUploadSize() {
		return uploadSize;
	}

	public void setUploadSize(int uploadSize) {
		this.uploadSize = uploadSize;
	}

	@Override
	public String toString() {
		return "DebugRpc [id=" + id + ", executionCpuTime=" + executionCpuTime + ", uploadTime=" + uploadTime + ", donwloadTime=" + donwloadTime + ", uploadSize=" + uploadSize + ", downloadSize=" + downloadSize + "]";
	}
}