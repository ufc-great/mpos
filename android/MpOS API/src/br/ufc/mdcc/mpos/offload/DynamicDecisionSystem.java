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
package br.ufc.mdcc.mpos.offload;

import java.util.TimerTask;

import android.content.Context;
import android.util.Log;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.exceptions.NetworkException;
import br.ufc.mdcc.mpos.net.profile.model.Network;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;

/**
 * This implementation made decision about local or remote execution,
 * based in profile results (ping tcp).
 * 
 * @author Philipp B. Costa
 */
public class DynamicDecisionSystem extends TimerTask {
    private final String clsName = DynamicDecisionSystem.class.getName();

    private final Object mutex = new Object();
    private final long PING_TOLERANCE = 50;//ms

    private ServerContent server;
    // private ProfileNetworkDAO profileDao;

    private TaskResultAdapter<Network> event = new TaskResultAdapter<Network>() {
        @Override
        public void completedTask(Network network) {
            if (network != null) {
                network.generatingPingTcpStats();

                Log.i(clsName, "Decision Maker -> Ping max: " + network.getPingMaxTcp() + ", med: " + network.getPingMedTcp() + ", min: " + network.getPingMinTcp());
                MposFramework.getInstance().getEndpointController().setRemoteAdvantageExecution(network.getPingMedTcp() < PING_TOLERANCE);
            } else {
                setServer(null);
                MposFramework.getInstance().getEndpointController().setRemoteAdvantageExecution(false);
                Log.e(clsName, "Any problem in ping test!");
            }
        }
    };

    public DynamicDecisionSystem(Context context, ServerContent server) {
        setServer(server);
        MposFramework.getInstance().getProfileController().setTaskResultEvent(event);
        // profileDao = new ProfileNetworkDAO(context);
    }

    public ServerContent getServer() {
        synchronized (mutex) {
            return server.newInstance(); //imutable operation  
        }
    }

    public synchronized void setServer(ServerContent server) {
        synchronized (mutex) {
            this.server = server;
        }
    }

    @Override
    public void run() {
        try {
            if (getServer() != null) {
                MposFramework.getInstance().getProfileController().networkAnalysis(getServer());
            } else {
                Log.i(clsName, "Waiting for new endpoint...");
            }
        } catch (MissedEventException e) {
            Log.e(clsName, "Forgot the event?", e);
        } catch (NetworkException e) {
            Log.w(clsName, e);
        }
    }
}