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

/**
 * Without offloading support!
 * 
 * @author Philipp
 */
public interface Filter {
    public int[][] greyScaleImage(int source[][]);

    public byte[] mapTone(byte source[], byte map[]);

    public int[][] mapTone(int source[][], int map[][]);

    public byte[] filterApply(byte source[], double filter[][], double factor, double offset);

    public int[][] filterApply(int source[][], double filter[][], double factor, double offset);

    public byte[] cartoonizerImage(byte source[]);

    public int[][] cartoonizerImage(int source[][]);
}