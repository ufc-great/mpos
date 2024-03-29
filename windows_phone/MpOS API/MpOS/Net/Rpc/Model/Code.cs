﻿/*******************************************************************************
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
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;

namespace Ufc.MpOS.Net.Rpc.Model
{
    public class Code
    {
		private Code()
		{

		}
		
		public static readonly byte OK = 0x01;

        public static readonly byte CLASS_NOT_FOUND = 0x02;

        public static readonly byte METHOD_THROW_ERROR = 0x03;

        public static readonly byte SERVER_ERROR = 0x04;

        public static readonly byte CUSTOMSTREAM = 0x15;

        public static readonly byte CUSTOMSTREAMDEBUG = 0x16;

        public static readonly byte BSONSTREAM = 0x25;

        public static readonly byte BSONSTREAMDEBUG = 0x26;
    }
}