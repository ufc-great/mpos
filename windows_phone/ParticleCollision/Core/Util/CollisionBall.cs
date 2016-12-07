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
using System.Collections.Generic;

namespace ParticleCollision.Core.Util
{
	public sealed class CollisionBall
	{
		public int CanvasWidth { get; private set; } //android name 'xWindowMax'
		public int CanvasHeight { get; private set; } //android name 'yWindowMax'

		public void CollisionWindow(Ball ball)
		{
			float ballRadius = ball.BallRadius;

			// left or right window borders
			if (ball.BallX + ballRadius > CanvasWidth)
			{
				ball.BallSpeedX = -ball.BallSpeedX;
				ball.BallX = CanvasWidth - ballRadius;
			}
			else if (ball.BallX - ballRadius < 0)
			{
				ball.BallSpeedX = -ball.BallSpeedX;
				ball.BallX = ballRadius;
			}

			// top and bottom window borders
			if (ball.BallY + ballRadius > CanvasHeight)
			{
				ball.BallSpeedY = -ball.BallSpeedY;
				ball.BallY = CanvasHeight - ballRadius;
			}
			else if (ball.BallY - ballRadius < 0)
			{
				ball.BallSpeedY = -ball.BallSpeedY;
				ball.BallY = ballRadius;
			}
		}

		public void DetectCollisionBall(Ball current, List<Ball> balls)
		{
			foreach (Ball other in balls)
			{
				if (current != other && Intersects(current, other))
				{
					ChangeDirection(current, other);
					current.Damage();
					other.Damage();
				}
			}
			current.RecoveryTime();
		}

		private void ChangeDirection(Ball current, Ball other)
		{
			//red color
			if (current.CollisionColor || other.CollisionColor)
			{
				if ((current.BallSpeedX > 0 && other.BallSpeedX < 0) || (current.BallSpeedX < 0 && other.BallSpeedX > 0))
				{
					current.BallSpeedX = -current.BallSpeedX * 0.95f;
					other.BallSpeedX = -other.BallSpeedX * 0.95f;
				}
				else
				{
					if (current.BallSpeedX > 0 && other.BallSpeedX > 0 && current.BallSpeedX > other.BallSpeedX)
					{
						// gain 5%
						other.BallSpeedX = current.BallSpeedX * 1.05f;
						// loss 3%
						current.BallSpeedX = other.BallSpeedX * 0.97f;
					}
					else if (current.BallSpeedX < 0 && other.BallSpeedX < 0 && current.BallSpeedX < other.BallSpeedX)
					{
						current.BallSpeedX = other.BallSpeedX * 1.05f;
						other.BallSpeedX = current.BallSpeedX * 0.97f;
					}
				}

				if ((current.BallSpeedY > 0 && other.BallSpeedY < 0) || (current.BallSpeedY < 0 && other.BallSpeedY > 0))
				{
					// loss 5%
					current.BallSpeedY = -current.BallSpeedY * 0.95f;
					other.BallSpeedY = -other.BallSpeedY * 0.95f;
				}
				else
				{
					if (current.BallSpeedY > 0 && other.BallSpeedY > 0 && current.BallSpeedY > other.BallSpeedY)
					{
						other.BallSpeedY = current.BallSpeedY * 1.05f;
						current.BallSpeedY = other.BallSpeedY * 0.97f;
					}
					else if (current.BallSpeedY < 0 && other.BallSpeedY < 0 && current.BallSpeedY < other.BallSpeedY)
					{
						current.BallSpeedY = other.BallSpeedY * 1.05f;
						other.BallSpeedY = current.BallSpeedY * 0.97f;
					}
				}
			}
		}

		private bool Intersects(Ball current, Ball other)
		{
			return current.Left < other.Right && other.Left < current.Right && current.Top < other.Bottom && other.Top < current.Bottom;
		}

		public void CanvasDimensions(int canvasWidth, int canvasHeight)
		{
			CanvasWidth = canvasWidth;
			CanvasHeight = canvasHeight;
		}
	}
}