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
using ParticleCollision.Core.Model;
using System.Collections.Generic;
using Ufc.MpOS.Net.Rpc.Util;
using Ufc.MpOS.Offload;

namespace ParticleCollision.Core
{
	public interface BallUpdater
	{
		[Remotable(Offload.STATIC)]
		List<Ball> UpdateStatic(List<Ball> balls, int width, int height);

		[Remotable]
		List<Ball> UpdateDynamic(List<Ball> balls, int width, int height);

		List<Ball> UpdateOffline(List<Ball> balls, int width, int height);
	}

	public interface BallUpdaterCustom : RpcSerializable
	{
		[Remotable(Offload.STATIC)]
		List<Ball> UpdateStatic(List<Ball> balls);

		[Remotable]
		List<Ball> UpdateDynamic(List<Ball> balls);

		List<Ball> UpdateOffline(List<Ball> balls);

		void CanvasDimensions(int width, int height);
	}
}