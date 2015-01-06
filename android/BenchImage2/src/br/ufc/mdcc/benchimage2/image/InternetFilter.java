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
package br.ufc.mdcc.benchimage2.image;

import br.ufc.mdcc.mpos.offload.Remotable;
import br.ufc.mdcc.mpos.offload.Remotable.Offload;

/**
 * With offloading support and server internet preference!
 * 
 * @author Philipp
 */
public interface InternetFilter extends Filter {
	@Remotable(cloudletPrority = false)
	public byte[] mapTone(byte source[], byte map[]);

	@Remotable(cloudletPrority = false)
	public byte[] filterApply(byte source[], double filter[][], double factor, double offset);

	@Remotable(value = Offload.STATIC, cloudletPrority = false, status = true)
	public byte[] cartoonizerImage(byte source[]);
}