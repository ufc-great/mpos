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
using ParticleCollision.Core.Model;
using ParticleCollision.Core.Util;
using System.Collections.Generic;
using System.IO;

namespace ParticleCollision.Core
{
	public sealed class BallController : BallUpdater
	{
		private CollisionBall collision;

		public BallController()
		{
			collision = new CollisionBall();
		}

		public List<Ball> UpdateOffline(List<Ball> balls, int width, int height)
		{
			collision.CanvasDimensions(width, height);
			return updateState(balls);
		}


		public List<Ball> UpdateStatic(List<Ball> balls, int width, int height)
		{
			collision.CanvasDimensions(width, height);
			return updateState(balls);
		}


		public List<Ball> UpdateDynamic(List<Ball> balls, int width, int height)
		{
			collision.CanvasDimensions(width, height);
			return updateState(balls);
		}

		private List<Ball> updateState(List<Ball> balls)
		{
			foreach (Ball current in balls)
			{
				current.NextPosition();
				collision.CollisionWindow(current);
				collision.DetectCollisionBall(current, balls);
			}
			return balls;
		}
	}

	public sealed class BallControllerCustom : BallUpdaterCustom
	{
		private CollisionBall collision;

		public BallControllerCustom()
		{
			collision = new CollisionBall();
		}

		public List<Ball> UpdateOffline(List<Ball> balls)
		{
			return UpdateState(balls);
		}

		public List<Ball> UpdateStatic(List<Ball> balls)
		{
			return UpdateState(balls);
		}

		public List<Ball> UpdateDynamic(List<Ball> balls)
		{
			return UpdateState(balls);
		}

		private List<Ball> UpdateState(List<Ball> balls)
		{
			foreach (Ball current in balls)
			{
				current.NextPosition();
				collision.CollisionWindow(current);
				collision.DetectCollisionBall(current, balls);
			}
			return balls;
		}

		public void CanvasDimensions(int width, int height)
		{
			collision.CanvasDimensions(width, height);
		}

		//writing custom serialization object!
		public void WriteMethodParams(BinaryWriter writer, string methodName, object[] methodParams)
		{
			if (methodName.Equals("UpdateStatic"))
			{
				List<Ball> balls = (List<Ball>) methodParams[0];
				writer.Write(balls.Count);

				foreach (Ball ball in balls)
				{
					ballToDataOutput(writer, ball);
				}

				writer.Write(collision.CanvasWidth);
				writer.Write(collision.CanvasHeight);
			}
			else if (methodName.Equals("UpdateDynamic"))
			{
				List<Ball> balls = (List<Ball>) methodParams[0];
				writer.Write(balls.Count);

				foreach (Ball ball in balls)
				{
					ballToDataOutput(writer, ball);
				}

				writer.Write(collision.CanvasWidth);
				writer.Write(collision.CanvasHeight);
			}
		}

		public object[] ReadMethodParams(BinaryReader reader, string methodName)
		{
			if (methodName.Equals("UpdateStatic"))
			{
				object[] parameters = new object[1];

				int size = reader.ReadInt32();
				List<Ball> balls = new List<Ball>(size);
				for (int i = 0; i < size; i++)
				{
					balls.Add(dataInputToBall(reader));
				}

				parameters[0] = balls;
				collision.CanvasDimensions(reader.ReadInt32(), reader.ReadInt32());

				return parameters;
			}
			else if (methodName.Equals("UpdateDynamic"))
			{
				object[] parameters = new object[1];

				int size = reader.ReadInt32();
				List<Ball> balls = new List<Ball>(size);
				for (int i = 0; i < size; i++)
				{
					balls.Add(dataInputToBall(reader));
				}

				parameters[0] = balls;
				collision.CanvasDimensions(reader.ReadInt32(), reader.ReadInt32());

				return parameters;
			}
			return null;
		}

		public void WriteMethodReturn(BinaryWriter writer, string methodName, object returnParam)
		{
			if (methodName.Equals("UpdateStatic"))
			{
				List<Ball> balls = (List<Ball>)returnParam;
				writer.Write(balls.Count);
				foreach (Ball ball in balls)
				{
					ballToDataOutput(writer, ball);
				}
			}
			else if (methodName.Equals("UpdateDynamic"))
			{
				List<Ball> balls = (List<Ball>)returnParam;
				writer.Write(balls.Count);
				foreach (Ball ball in balls)
				{
					ballToDataOutput(writer, ball);
				}
			}
		}

		public object ReadMethodReturn(BinaryReader reader, string methodName)
		{
			if (methodName.Equals("UpdateStatic"))
			{
				int size = reader.ReadInt32();
				List<Ball> balls = new List<Ball>(size);
				for (int i = 0; i < size; i++)
				{
					balls.Add(dataInputToBall(reader));
				}
				return balls;
			}
			else if (methodName.Equals("UpdateDynamic"))
			{
				int size = reader.ReadInt32();
				List<Ball> balls = new List<Ball>(size);
				for (int i = 0; i < size; i++)
				{
					balls.Add(dataInputToBall(reader));
				}
				return balls;
			}
			return null;
		}

		private void ballToDataOutput(BinaryWriter output, Ball ball)
		{
			output.Write(ball.DamageColorTime);
			output.Write(ball.CollisionColor);
			output.Write(ball.BallRadius);
			output.Write(ball.BallX);
			output.Write(ball.BallY);
			output.Write(ball.BallSpeedX);
			output.Write(ball.BallSpeedY);
			output.Write(ball.Left);
			output.Write(ball.Top);
			output.Write(ball.Right);
			output.Write(ball.Bottom);
		}

		private Ball dataInputToBall(BinaryReader input)
		{
			Ball ball = new Ball();

			ball.DamageColorTime = input.ReadInt32();
			ball.CollisionColor = input.ReadBoolean();
			ball.BallRadius = input.ReadSingle();
			ball.BallX = input.ReadSingle();
			ball.BallY = input.ReadSingle();
			ball.BallSpeedX = input.ReadSingle();
			ball.BallSpeedY = input.ReadSingle();
			ball.Left = input.ReadSingle();
			ball.Top = input.ReadSingle();
			ball.Right = input.ReadSingle();
			ball.Bottom = input.ReadSingle();

			return ball;
		}
	}
}