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
using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;

namespace BenchImage.Core.Util
{
	public sealed class PhotoUtilities
	{
		public static byte[] ImageJpg { get; set; }
		public static byte[] FilterJpg { get; set; }

		private PhotoUtilities() { }

		//- Notice
		//You can't define the path "System.Threading.Tasks.*" and etc. on "using" scheme
		//for respect the portable issues!
		public static async System.Threading.Tasks.Task SavePhotoTask(byte[] image, string name)
		{
			//Debug.WriteLine("[BenchImage_DEBUG]: Salvando imagem filtrada!");

			Windows.Storage.StorageFolder local = Windows.Storage.ApplicationData.Current.LocalFolder;

			Windows.Storage.StorageFolder dataFolder = await local.CreateFolderAsync("BenchImageOutput", Windows.Storage.CreationCollisionOption.OpenIfExists);
			Windows.Storage.StorageFile file = await dataFolder.CreateFileAsync(name, Windows.Storage.CreationCollisionOption.ReplaceExisting);

			using (var stream = await file.OpenStreamForWriteAsync())
			{
				stream.Write(image, 0, image.Length);
			}

			//Debug.WriteLine("[BenchImage_DEBUG]: Salvou imagem filtrada!");
		}

		public static async System.Threading.Tasks.Task LoadPhotoTask(string size, string name)
		{
			Windows.Storage.StorageFolder installationFolder = Windows.ApplicationModel.Package.Current.InstalledLocation;
			Windows.Storage.StorageFolder assetsFolder = await installationFolder.GetFolderAsync("Assets");
			Windows.Storage.StorageFolder imagesFolder = await assetsFolder.GetFolderAsync("Images");
			Windows.Storage.StorageFolder sizeFolder = null;
			if (size.Equals("0.3MP"))
			{
				sizeFolder = await imagesFolder.GetFolderAsync("0_3mp");
			}
			else
			{
				sizeFolder = await imagesFolder.GetFolderAsync(size.ToLower());
			}

			IReadOnlyList<Windows.Storage.StorageFile> files = await sizeFolder.GetFilesAsync();
			foreach (var file in files)
			{
				if (name.Equals(file.Name))
				{
					using (Windows.Storage.Streams.IRandomAccessStream fileStream = await file.OpenReadAsync())
					{
						ImageJpg = new byte[fileStream.Size];
						using (var reader = new Windows.Storage.Streams.DataReader(await file.OpenReadAsync()))
						{
							await reader.LoadAsync((uint)fileStream.Size);
							reader.ReadBytes(ImageJpg);
						}
					}
					break;
				}
			}
		}

		public static async System.Threading.Tasks.Task LoadFilterTask(string name)
		{
			var installationFolder = Windows.ApplicationModel.Package.Current.InstalledLocation;
			var assetsFolder = await installationFolder.GetFolderAsync("Assets");
			var filtersFolder = await assetsFolder.GetFolderAsync("Filters");

			string filename = null;
			if (name.Equals("Red Ton"))
			{
				filename = "map1.png";
			}

			var file = await filtersFolder.GetFileAsync(filename);
			using (var fileStream = await file.OpenReadAsync())
			{
				FilterJpg = new byte[fileStream.Size];
				using (var reader = new Windows.Storage.Streams.DataReader(await file.OpenReadAsync()))
				{
					await reader.LoadAsync((uint)fileStream.Size);
					reader.ReadBytes(FilterJpg);
				}
			}
		}

		public static int[][] DecodeJpegToRaw(ref byte[] image)
		{
			int[][] rawImage = null;
			try
			{
				using (MemoryStream stream = new MemoryStream(image))
				{
					var bitmapImage = new System.Windows.Media.Imaging.BitmapImage();
					bitmapImage.SetSource(stream);

					var bitmap = new System.Windows.Media.Imaging.WriteableBitmap(bitmapImage);
					int width = bitmap.PixelWidth;
					int height = bitmap.PixelHeight;

					rawImage = new int[width][];
					for (int col = 0; col < width; col++)
					{
						rawImage[col] = new int[height];
						for (int row = 0; row < height; row++)
						{
							rawImage[col][row] = bitmap.Pixels[(row * width) + col];
						}
					}

					bitmap = null;
					bitmapImage = null;
					GC.Collect();
				}				
			}
			catch (Exception)
			{
				//- Notice!
				//On Server or Desktop plataform, call the reference function by using reflection
				//Equals made in Android plataform, because this libaries aren't portable between WP and .NET 4.0!
				Type bitmapType = Type.GetType("Ufc.MiscNet.ImageUtilities, MiscNet, Version=1.0.0.0, Culture=neutral");
				object instance = Activator.CreateInstance(bitmapType);
				Type[] typeParam = new Type[1] { typeof(byte[]) };

				MethodInfo method = bitmapType.GetMethod("DecodeJpegToRaw", typeParam);
				rawImage = (int[][])method.Invoke(instance, new object[1] { image });
			}
			finally
			{
				image = null;
				GC.Collect();
			}
			return rawImage;
		}

		public static byte[] EncodeRawToJpeg(ref int[][] rawImage)
		{
			try
			{
				var bitmap = new System.Windows.Media.Imaging.WriteableBitmap(rawImage.Length, rawImage[0].Length);
				int width = rawImage.Length;
				int height = rawImage[0].Length;

				for (int col = 0; col < width; col++)
				{
					for (int row = 0; row < height; row++)
					{
						bitmap.Pixels[(row * width) + col] = rawImage[col][row];
					}
				}

				using (MemoryStream memoryStream = new MemoryStream())
				{
					System.Windows.Media.Imaging.Extensions.SaveJpeg(bitmap, memoryStream, width, height, 0, 100);
					memoryStream.Seek(0, SeekOrigin.Begin);

					bitmap = null;
					rawImage = null;
					GC.Collect();
					return memoryStream.ToArray();
				}
			}
			catch (Exception)
			{
				//- Notice!
				//On Server or Desktop plataform, call the reference function by using reflection
				//Equals made in Android plataform, because this libaries aren't portable between WP and .NET 4.0!
				Type bitmapType = Type.GetType("Ufc.MiscNet.ImageUtilities, MiscNet, Version=1.0.0.0, Culture=neutral");
				object instance = Activator.CreateInstance(bitmapType);
				Type[] typeParam = new Type[1] { typeof(int[][]) };

				MethodInfo method = bitmapType.GetMethod("EncodeRawToJpeg", typeParam);
				return (byte[]) method.Invoke(instance, new object[1] { rawImage });
			}
		}

		public static int[][] RawImageClone(int[][] image)
		{
			int width = image.Length;
			int[][] clone = new int[width][];

			int heightSize = image[0].Length;
			for (int col = 0; col < width; col++)
			{
				clone[col] = new int[heightSize];
				Array.Copy(image[col], clone[col], heightSize);
			}
			return clone;
		}

		public static string PhotoNameToFileName(string name)
		{
			switch (name)
			{
				case "FAB Show":
					return "img1.jpg";
				case "Cidade":
					return "img4.jpg";
				case "SkyLine":
					return "img5.jpg";

				default: return null;
			}
		}

		public static int Red(int color)
		{
			return (color >> 16) & 0xFF;
		}

		public static int Green(int color)
		{
			return (color >> 8) & 0xFF;
		}

		public static int Blue(int color)
		{
			return color & 0xFF;
		}

		public static int Color(int red, int green, int blue)
		{
			return (0xFF << 24) | (red << 16) | (green << 8) | blue;
		}
	}
}