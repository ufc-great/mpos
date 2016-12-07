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

namespace ParticleCollision.Core.Model
{
	/*
	 * - Notice:
	 * For 500 Balls using double type the payload for each call is 38569 bytes, now using float type
	 * the payload is 20569 bytes.
	 */ 
	public sealed class Ball
	{	
		public int DamageColorTime { get; set; }
		public bool CollisionColor { get; set; } //true = red (Collision), false = yellow
		public float BallRadius { get; set; }

		// Ball's center (x,y)
		public float BallX { get; set; }
		public float BallY { get; set; }

		// Ball's speed (x,y)
		public float BallSpeedX { get; set; }
		public float BallSpeedY { get; set; }

		public float Left { get; set; }
		public float Top { get; set; }
		public float Right { get; set; }
		public float Bottom { get; set; }
		
		public Ball()
		{
			DamageColorTime = 0;
		}

		private Ball(bool collisionColor, float ballRadius)
			: this()
		{
			this.CollisionColor = collisionColor;
			this.BallRadius = ballRadius;
		}

		public Ball(Random random, bool fixLocation) : this(3.5f, random, fixLocation, 240, 320) { }

		public Ball(float ballRadius, Random random, bool fixLocation, int width, int height)
			: this(true, ballRadius)
		{
			if (fixLocation)
			{
				BallX = 100.0f;
				BallY = 100.0f;
			}
			else
			{
				BallX = 15.0f + Convert.ToSingle(random.NextDouble()) * width;
				BallY = 10.0f + Convert.ToSingle(random.NextDouble()) * height;
			}

			// 35% reverse start
			if (random.NextDouble() > 0.35)
			{
				BallSpeedX = 3.0f + Convert.ToSingle(random.NextDouble()) * 15.0f;
				BallSpeedY = 3.0f + Convert.ToSingle(random.NextDouble()) * 15.0f;
			}
			else
			{
				BallSpeedX = -(3.0f + Convert.ToSingle(random.NextDouble()) * 15.0f);
				BallSpeedY = -(3.0f + Convert.ToSingle(random.NextDouble()) * 15.0f);
			}

			GenerateBounds();
		}

		public Ball(float ballRadius, float ballX, float ballY, float ballSpeedX, float ballSpeedY)
			: this(true, ballRadius)
		{
			BallX = ballX;
			BallY = ballY;
			BallSpeedX = ballSpeedX;
			BallSpeedY = ballSpeedY;

			GenerateBounds();
		}

		public void NextPosition()
		{
			BallY += BallSpeedY;
			BallX += BallSpeedX;
		}

		public void GenerateBounds()
		{
			Left = BallX - BallRadius;
			Right = BallX + BallRadius;
			Top = BallY - BallRadius;
			Bottom = BallY + BallRadius;
		}

		public void Damage()
		{
			if (CollisionColor)
			{
				CollisionColor = false;
				DamageColorTime = 45;// next 45fps
			}
		}

		public void RecoveryTime()
		{
			if (DamageColorTime > 0)
			{
				DamageColorTime--;
			}
			else
			{
				CollisionColor = true;
			}
		}
	}
}