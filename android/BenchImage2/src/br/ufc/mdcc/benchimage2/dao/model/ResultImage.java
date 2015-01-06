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
package br.ufc.mdcc.benchimage2.dao.model;

import java.util.Date;

import android.graphics.Bitmap;
import br.ufc.mdcc.mpos.net.rpc.model.RpcProfile;

/**
 * @author Philipp
 */
public final class ResultImage {
    private int id;
    private Date date;

    private long totalTime;

    private Bitmap bitmap;
    private AppConfiguration config;
    private RpcProfile rpcProfile;

    public ResultImage(AppConfiguration config) {
        this();
        this.config = config;
        this.rpcProfile = new RpcProfile();
    }

    public ResultImage() {
        date = new Date();
    }

    public final int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public final Date getDate() {
        return date;
    }

    public final void setDate(Date date) {
        this.date = date;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public final AppConfiguration getConfig() {
        return config;
    }

    public final void setConfig(AppConfiguration config) {
        this.config = config;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setRpcProfile(RpcProfile rpcProfile) {
        this.rpcProfile = rpcProfile;
    }

    public RpcProfile getRpcProfile() {
        return rpcProfile;
    }

    @Override
    public String toString() {
        return "ResultImage [id=" + id + ", date=" + date + ", totalTime=" + totalTime + ", config=" + config + ", debug=" + rpcProfile + "]";
    }
}