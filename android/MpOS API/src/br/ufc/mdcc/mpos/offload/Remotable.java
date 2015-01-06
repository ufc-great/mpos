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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for supporting offloading process. User can selected priority endpoint and debug network (upload time [milliseconds], upload size
 * [bytes], download time and download size)
 * 
 * @author Philipp B. Costa
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Remotable {
	Offload value() default Offload.DYNAMIC;

	boolean cloudletPrority() default true;

	boolean status() default false;// see on log stats

	public enum Offload {
		STATIC, DYNAMIC;

		public String toString() {
			if (ordinal() == STATIC.ordinal()) {
				return "Offload Static";// always try invoke remotable method
			} else {
				return "Offload Dynamic";// depends from decision making
			}
		};
	}
}