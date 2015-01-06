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
package br.ufc.mdcc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import br.ufc.mdcc.util.ImageUtils;

public class Main {
	public static void main(String... args) throws IOException {
		Main instance = new Main();

		FileInputStream fis = new FileInputStream("./data/f22.jpg");
		int imgMatrix[][] = ImageUtils.decodeJpegToRaw(ImageUtils.streamToByteArray(fis));
		fis.close();

		imgMatrix = instance.greyScaleImage(imgMatrix);

		byte dataOutput[] = ImageUtils.encodeRawToJpeg(imgMatrix);
		FileOutputStream fos = new FileOutputStream("./data/f22_grey.jpg");
		fos.write(dataOutput);
		fos.flush();
		fos.close();
	}

	public int[][] greyScaleImage(int source[][]) {
		final double greyScaleRed = 0.299;
		final double greyScaleGreen = 0.587;
		final double greyScaleBlue = 0.114;

		int red, green, blue;
		int pixel;

		int imgWidth = source.length;
		int imgHeight = source[0].length;

		for (int x = 0; x < imgWidth; x++) {
			for (int y = 0; y < imgHeight; y++) {
				pixel = source[x][y];

				red = ImageUtils.getRed(pixel);
				green = ImageUtils.getGreen(pixel);
				blue = ImageUtils.getBlue(pixel);

				red = green = blue = (int) (greyScaleRed * red + greyScaleGreen * green + greyScaleBlue * blue);

				source[x][y] = ImageUtils.setColor(red, green, blue);
			}
		}

		return source;
	}
}