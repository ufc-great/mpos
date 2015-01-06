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
package br.ufc.mdcc.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author Philipp
 */
public final class ImageUtils {
	private ImageUtils() {
	}

	/**
	 * Encode raw (color matrix) to data buffer (jpeg). Work with any runtime
	 * Android or Java SE.
	 * 
	 * @param imageRaw
	 * @return
	 * @throws IOException
	 */
	public static byte[] encodeRawToJpeg(int[][] imageRaw) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream(2 * 1024 * 1024); // 2mb
		byte data[] = null;
		try {
			Bitmap bitmap = encodeMatrixToBitmap(imageRaw);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

			output.flush();
			data = output.toByteArray();

			// clean
			output.close();
			output = null;
			bitmap = null;
			System.gc();
		} catch (NoClassDefFoundError e) {
			BufferedImage bufferedImage = encodeMatrixToBufferedImage(imageRaw);

			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(1.0f);

			writer.setOutput(new MemoryCacheImageOutputStream(output));
			writer.write(bufferedImage);

			output.flush();
			data = output.toByteArray();

			// clean
			output.close();
			output = null;
			writer = null;
			bufferedImage = null;
			System.gc();
		}
		return data;
	}

	/**
	 * Decode databuffer (jpeg) to raw (color matrix). Work with any runtime
	 * Android or Java SE.
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static int[][] decodeJpegToRaw(byte[] data) throws IOException {
		int imageRaw[][] = null;
		try {
			imageRaw = decodeBitmapToMatrix(BitmapFactory.decodeByteArray(data, 0, data.length));
		} catch (NoClassDefFoundError e) {
			imageRaw = decodeBufferedImageToMatrix(ImageIO.read(new ByteArrayInputStream(data)));
		}
		return imageRaw;
	}

	private static int[][] decodeBitmapToMatrix(Bitmap image) throws IOException {
		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();
		int imageMatrix[][] = new int[imgWidth][imgHeight];

		for (int x = 0; x < imgWidth; x++) {
			for (int y = 0; y < imgHeight; y++) {
				imageMatrix[x][y] = image.getPixel(x, y);
			}
		}

		image.recycle();
		image = null;
		System.gc();
		return imageMatrix;
	}

	private static int[][] decodeBufferedImageToMatrix(BufferedImage image) throws IOException {
		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();
		int imageMatrix[][] = new int[imgWidth][imgHeight];

		for (int x = 0; x < imgWidth; x++) {
			for (int y = 0; y < imgHeight; y++) {
				imageMatrix[x][y] = image.getRGB(x, y);
			}
		}

		image = null;
		System.gc();
		return imageMatrix;
	}

	private static BufferedImage encodeMatrixToBufferedImage(int imageMatrix[][]) {
		BufferedImage bufferedImage = new BufferedImage(imageMatrix.length, imageMatrix[0].length, BufferedImage.TYPE_INT_RGB);

		int imgWidth = imageMatrix.length;
		int imgHeight = imageMatrix[0].length;
		for (int x = 0; x < imgWidth; x++) {
			for (int y = 0; y < imgHeight; y++) {
				bufferedImage.setRGB(x, y, imageMatrix[x][y]);
			}
		}
		return bufferedImage;
	}

	private static Bitmap encodeMatrixToBitmap(int imageMatrix[][]) {
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap image = Bitmap.createBitmap(imageMatrix.length, imageMatrix[0].length, conf);

		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();
		for (int x = 0; x < imgWidth; x++) {
			for (int y = 0; y < imgHeight; y++) {
				image.setPixel(x, y, imageMatrix[x][y]);
			}
		}
		return image;
	}

	public static byte[] streamToByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream(2 * 1024 * 1024); // 2mb
		byte buffer[] = new byte[32 * 1024];

		int read = 0;
		while ((read = is.read(buffer)) != -1) {
			output.write(buffer, 0, read);
		}

		output.flush();
		return output.toByteArray();
	}

	public static int[][] rawImageClone(int matrix[][]) {
		int clone[][] = new int[matrix.length][0];

		int width = matrix.length;
		for (int i = 0; i < width; i++) {
			clone[i] = Arrays.copyOf(matrix[i], matrix[i].length);
		}

		return clone;
	}

	public static int getRed(int color) {
		return (color >> 16) & 0xFF;
	}

	public static int getGreen(int color) {
		return (color >> 8) & 0xFF;
	}

	public static int getBlue(int color) {
		return color & 0xFF;
	}

	public static int setColor(int red, int green, int blue) {
		return (0xFF << 24) | (red << 16) | (green << 8) | blue;
	}
}