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
package br.ufc.mdcc.collision.controller;

import java.util.List;

import br.ufc.mdcc.mpos.net.rpc.util.RpcSerializable;
import br.ufc.mdcc.mpos.offload.Remotable;
import br.ufc.mdcc.mpos.offload.Remotable.Offload;

/**
 * @author Philipp
 * @param <T>
 */

public interface BallCustomUpdater<T> extends RpcSerializable {
    @Remotable(value = Offload.STATIC)
    public List<T> updateStatic(List<T> balls);

    @Remotable
    public List<T> updateDynamic(List<T> balls);

    public List<T> updateOffline(List<T> balls);

    public void setWindowDimensions(int width, int height);
}