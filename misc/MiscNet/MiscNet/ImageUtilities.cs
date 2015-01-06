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
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;

namespace Ufc.MiscNet
{
    public class ImageUtilities
    {
		public int[][] DecodeJpegToRaw(byte[] jpegImage)
		{
			MemoryStream memoryStream = new MemoryStream(jpegImage);
			Bitmap bitmap = new Bitmap(memoryStream);

			int width = bitmap.Size.Width;
			int height = bitmap.Size.Height;

			var rawImage = new int[width][];

			for (int i = 0; i < width; i++)
			{
				rawImage[i] = new int[height];
				for (int j = 0; j < height; j++)
				{
					rawImage[i][j] = bitmap.GetPixel(i, j).ToArgb();
				}
			}

			bitmap = null;
			memoryStream.Dispose();
			memoryStream = null;

			return rawImage;
		}

		public byte[] EncodeRawToJpeg(int[][] rawImage)
		{
			Bitmap bitmap = new Bitmap(rawImage.Length, rawImage[0].Length);

			int width = bitmap.Size.Width;
			int height = bitmap.Size.Height;

			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					bitmap.SetPixel(i, j, Color.FromArgb(rawImage[i][j]));
				}
			}

			using (MemoryStream memoryStream = new MemoryStream())
			{
				EncoderParameters myEncoderParameters = new EncoderParameters(1);
				myEncoderParameters.Param[0] = new EncoderParameter(Encoder.Quality, 100L);//jpeg with 100% quality!
				bitmap.Save(memoryStream, GetEncoder(ImageFormat.Jpeg), myEncoderParameters);
				return memoryStream.ToArray();
			}
		}

		private ImageCodecInfo GetEncoder(ImageFormat format)
		{
			var codecs = ImageCodecInfo.GetImageDecoders();
			foreach (ImageCodecInfo codec in codecs)
			{
				if (codec.FormatID == format.Guid)
				{
					return codec;
				}
			}
			return null;
		}
    }
}