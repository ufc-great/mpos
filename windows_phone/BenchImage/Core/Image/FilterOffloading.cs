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
using Ufc.MpOS.Offload;

/*
 * This interfaces method support offloading technic by data type attribute.
 * 
 * @author Philipp B. Costa
 */
namespace BenchImage.Core.Image
{
	public interface CloudletFilter : Filter
	{
		[Remotable(Status = true)]
		new byte[] MapTone(byte[] source, byte[] map);

		[Remotable(Status = true)]
		new byte[] FilterApply(byte[] source, double[][] filter, double factor, double offset);

		[Remotable(Offload.STATIC, Status = true)]
		new byte[] CartoonizerImage(byte[] source);
	}

	public interface InternetFilter : Filter
	{
		[Remotable(Status = true, CloudletPriority = false)]
		new byte[] MapTone(byte[] source, byte[] map);

		[Remotable(Status = true, CloudletPriority = false)]
		new byte[] FilterApply(byte[] source, double[][] filter, double factor, double offset);

		[Remotable(Offload.STATIC, Status = true, CloudletPriority = false)]
		new byte[] CartoonizerImage(byte[] source);
	}
}