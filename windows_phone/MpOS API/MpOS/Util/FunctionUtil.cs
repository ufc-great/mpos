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
namespace Ufc.MpOS.Util
{
	class FunctionUtil
	{
		public static bool ContainsArrays(byte[] source, byte[] target) {
			return IndexOfArrays(source, target, 0, target.Length) > -1;
		}

		private static int IndexOfArrays(byte[] target, byte[] source, int sourceOffset, int sourceCount) {
			int targetOffset = 0, targetCount = target.Length;
			int fromIndex = 0;

			if (fromIndex >= targetCount)
			{
				return (sourceCount == 0 ? targetCount : -1);
			}
			if (fromIndex < 0)
			{
				fromIndex = 0;
			}
			if (sourceCount == 0)
			{
				return fromIndex;
			}

			byte first = source[sourceOffset];
			int max = targetOffset + (targetCount - sourceCount);

			for (int i = targetOffset + fromIndex; i <= max; i++)
			{
				/* Look for first character. */
				if (target[i] != first)
				{
					while (++i <= max && target[i] != first)
						;
				}
				/* Found first character, now look at the rest of v2 */
				if (i <= max)
				{
					int j = i + 1;
					int end = j + sourceCount - 1;
					for (int k = sourceOffset + 1; j < end && target[j] == source[k]; j++, k++)
						;

					if (j == end)
					{
						/* Found whole string. */
						return i - targetOffset;
					}
				}
			}
			return -1;
		}
	}
}