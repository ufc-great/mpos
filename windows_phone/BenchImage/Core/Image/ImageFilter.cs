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
using BenchImage.Core.Util;
using System;
using System.Diagnostics;

namespace BenchImage.Core.Image
{
	public sealed class ImageFilter : CloudletFilter, InternetFilter
	{
		public byte[] MapTone(byte[] source, byte[] map)
		{
			//prefer block the ui thread, because that object: "System.Windows.Media.Imaging.BitmapImage"
			int[][] image = MapTone(PhotoUtilities.DecodeJpegToRaw(ref source), PhotoUtilities.DecodeJpegToRaw(ref map));
			return PhotoUtilities.EncodeRawToJpeg(ref image);
		}

		public int[][] MapTone(int[][] source, int[][] map)
		{
			int imgWidth = source.Length;
			int imgHeight = source[0].Length;
			int filterHeight = map[0].Length;

			for (int x = 0; x < imgWidth; x++)
			{
				for (int y = 0; y < imgHeight; y++)
				{
					int color = source[x][y];
					int channelRed = PhotoUtilities.Red(color);
					int channelGreen = PhotoUtilities.Green(color);
					int channelBlue = PhotoUtilities.Blue(color);

					if (filterHeight == 1)
					{
						channelRed = map[channelRed][0];
						channelGreen = map[channelGreen][0];
						channelBlue = map[channelBlue][0];
					}
					else
					{
						channelRed = map[channelRed][0];
						channelGreen = map[channelGreen][1];
						channelBlue = map[channelBlue][2];
					}
					source[x][y] = PhotoUtilities.Color(PhotoUtilities.Red(channelRed), PhotoUtilities.Green(channelGreen), PhotoUtilities.Blue(channelBlue));
				}
			}
			return source;
		}

		public byte[] FilterApply(byte[] source, double[][] filter, double factor, double offset)
		{
			int[][] image = FilterApply(PhotoUtilities.DecodeJpegToRaw(ref source), filter, factor, offset);
			return PhotoUtilities.EncodeRawToJpeg(ref image);
		}

		public int[][] FilterApply(int[][] source, double[][] filter, double factor, double offset)
		{
			int imgWidth = source.Length;
			int imgHeight = source[0].Length;

			int filterHeight = filter.Length;
			int filterWidth = filter[0].Length;

			for (int x = 0; x < imgWidth; x++)
			{
				for (int y = 0; y < imgHeight; y++)
				{
					int red = 0;
					int green = 0;
					int blue = 0;

					for (int filterX = 0; filterX < filterWidth; filterX++)
					{
						for (int filterY = 0; filterY < filterHeight; filterY++)
						{
							int imageX = (x - (filterWidth / 2) + filterX + imgWidth) % imgWidth;
							int imageY = (y - (filterHeight / 2) + filterY + imgHeight) % imgHeight;

							int color = source[imageX][imageY];

							double maskValue = filter[filterX][filterY];

							red += (int)(PhotoUtilities.Red(color) * maskValue);
							green += (int)(PhotoUtilities.Green(color) * maskValue);
							blue += (int)(PhotoUtilities.Blue(color) * maskValue);
						}
					}

					red = Math.Min(Math.Max((int)(factor * red + offset), 0), 255);
					green = Math.Min(Math.Max((int)(factor * green + offset), 0), 255);
					blue = Math.Min(Math.Max((int)(factor * blue + offset), 0), 255);

					source[x][y] = PhotoUtilities.Color(red, green, blue);
				}
			}

			return source;
		}

		public byte[] CartoonizerImage(byte[] source)
		{
			int[][] image = CartoonizerImage(PhotoUtilities.DecodeJpegToRaw(ref source));
			return PhotoUtilities.EncodeRawToJpeg(ref image);
		}

		public int[][] CartoonizerImage(int[][] source)
		{
			source = GreyScaleImage(source);

			int[][] imageInvert = InvertColors(PhotoUtilities.RawImageClone(source));

			double[][] maskFilter = new double[3][];
			maskFilter[0] = new double[3]{ 1, 2, 1 };
			maskFilter[1] = new double[3] { 2, 4, 2 };
			maskFilter[2] = new double[3] { 1, 2, 1 };

			imageInvert = FilterApply(imageInvert, maskFilter, 1.0 / 16.020, 0.0);

			int[][] result = ColorDodgeBlendOptimized(imageInvert, source);

			source = null;
			imageInvert = null;
			GC.Collect();

			return result;
		}

		private int[][] GreyScaleImage(int[][] source)
		{
			double greyScaleRed = 0.299;
			double greyScaleGreen = 0.587;
			double greyScaleBlue = 0.114;

			int red, green, blue;
			int pixel;

			int imgWidth = source.Length;
			int imgHeight = source[0].Length;

			for (int x = 0; x < imgWidth; x++)
			{
				for (int y = 0; y < imgHeight; y++)
				{
					pixel = source[x][y];

					red = PhotoUtilities.Red(pixel);
					green = PhotoUtilities.Green(pixel);
					blue = PhotoUtilities.Blue(pixel);

					red = green = blue = (int)(greyScaleRed * red + greyScaleGreen * green + greyScaleBlue * blue);

					source[x][y] = PhotoUtilities.Color(red, green, blue);
				}
			}

			return source;
		}

		private int[][] InvertColors(int[][] source)
		{
			int pixelColor;
			int red, green, blue;

			int imgWidth = source.Length;
			int imgHeight = source[0].Length;
			for (int y = 0; y < imgHeight; y++)
			{
				for (int x = 0; x < imgWidth; x++)
				{
					pixelColor = source[x][y];

					red = 255 - PhotoUtilities.Red(pixelColor);
					green = 255 - PhotoUtilities.Green(pixelColor);
					blue = 255 - PhotoUtilities.Blue(pixelColor);

					source[x][y] = PhotoUtilities.Color(red, green, blue);
				}
			}
			return source;
		}

		private int[][] ColorDodgeBlendOptimized(int[][] source, int[][] layer)
		{
			int imgWidth = source.Length;
			int imgHeight = source[0].Length;

			for (int i = 0; i < imgHeight; i++)
			{
				for (int j = 0; j < imgWidth; j++)
				{
					int filterInt = layer[j][i];
					int srcInt = source[j][i];

					int redValueFinal = Colordodge(PhotoUtilities.Red(filterInt), PhotoUtilities.Red(srcInt));
					int greenValueFinal = Colordodge(PhotoUtilities.Green(filterInt), PhotoUtilities.Green(srcInt));
					int blueValueFinal = Colordodge(PhotoUtilities.Blue(filterInt), PhotoUtilities.Blue(srcInt));

					source[j][i] = PhotoUtilities.Color(redValueFinal, greenValueFinal, blueValueFinal);
				}
			}

			return source;
		}

		private int Colordodge(int in1, int in2)
		{
			float image = (float)in2;
			float mask = (float)in1;
			return ((int)((image == 255) ? image : Math.Min(255, (((long)mask << 8) / (255 - image)))));
		}
	}
}