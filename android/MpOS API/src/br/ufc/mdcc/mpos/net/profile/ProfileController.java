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
package br.ufc.mdcc.mpos.net.profile;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import br.ufc.mdcc.mpos.config.ProfileNetwork;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.exceptions.NetworkException;
import br.ufc.mdcc.mpos.net.profile.model.Network;
import br.ufc.mdcc.mpos.persistence.ProfileNetworkDao;
import br.ufc.mdcc.mpos.util.TaskResult;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;

/**
 * This class control the profile services
 * 
 * @author Philipp B. Costa
 */
public final class ProfileController {
    private TaskResult<Network> taskResultEvent;
    
    private ProfileNetworkDao profileDao;
    private ProfileNetworkTask taskNetwork;
    private ProfileNetwork profileNetwork;

    public ProfileController(Context context, ProfileNetwork profile) {
        this.profileNetwork = profile;
        profileDao = new ProfileNetworkDao(context);

        Log.i(ProfileController.class.getName(), "MpOS Profile Started!");
    }

    public void setTaskResultEvent(TaskResult<Network> taskResultEvent) {
        this.taskResultEvent = taskResultEvent;
    }

    public void networkAnalysis(ServerContent server) throws MissedEventException, NetworkException {
        networkAnalysis(server, profileNetwork);
    }

    public void networkAnalysis(ServerContent server, ProfileNetwork profileNetwork) throws MissedEventException, NetworkException {
        if (server == null) {
            throw new NetworkException("The remote service isn't ready for profile network");
        }

        if (profileNetwork == ProfileNetwork.LIGHT) {
            taskNetwork = new ProfileNetworkLight(persistNetworkResults(taskResultEvent), server);
        } else if (profileNetwork == ProfileNetwork.DEFAULT) {
            taskNetwork = new ProfileNetworkDefault(persistNetworkResults(taskResultEvent), server);
        } else if (profileNetwork == ProfileNetwork.FULL) {
            taskNetwork = new ProfileNetworkFull(persistNetworkResults(taskResultEvent), server);
        }
        taskNetwork.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private TaskResult<Network> persistNetworkResults(final TaskResult<Network> interceptedResults) {
        // interception pattern
        return new TaskResultAdapter<Network>() {
            @Override
            public void completedTask(final Network network) {
                if (network != null) {
                    // local persistence
                    profileDao.add(network);
                }
                interceptedResults.completedTask(network);
            }

            @Override
            public void taskOnGoing(int completed) {
                interceptedResults.taskOnGoing(completed);
            }
        };
    }

    public void destroy() {
        if (taskNetwork != null) {
            taskNetwork.halt();
            taskNetwork = null;
        }
        profileNetwork = null;
    }
}