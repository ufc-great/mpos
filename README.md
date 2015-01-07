[MpOS Framework](http://mpos.great.ufc.br)
====================================================================

Here you will find the source code and tutorials of MpOS Framework (Android and Windows Phone)

## Developers

In this tutorial you'll see how to run BenchImage app with MpOS

### Technologies used:

1. Clone the MpOS project from github
2. Windows 8.1 (x64) Professional (for Linux and Mac OS X only Android side works)
3. Android side:
  * Eclipse Platform 4.2.1v with ADT 23.0.x
  * Oracle Java SDK 8.x (for any OS)
  * Android SDK installed (API 19 or above)
4. Windows Phone side:
  * Visual Studio Professional 2013 (with Update 4) or Community 2013 (not tested)
  * Visual Studio SDK 2013 (with Update 4)
  * Windows Phone emulators (need Win 8.1 x64 Pro with client Hyper-V installed)


#### Import Android components on Eclipse ADT

Open Eclipse ADT and follow this steps (jump to next section if you know how to import in eclipse)

```
Import -> Git -> Projects from Git
```
```
Click in “Local” and “Add” a repository
```
```
Selected the path where Project was cloned '<cloned_path>/android' and click in "finish"
```
```
Selected the repository cloned and click “next”
```
```
Choose “Import existing projects” and click “Next”
```
```
Selected the “BenchImage2” and “MpOS API” to import and click in “Finish”
```

You’ll see the projects “BenchImage2” and “MpOS API” on eclipse workspace. In BenchImage2 has an MainActivity to start the mobile app. 

PS: For test the BenchImage2 app is recommended to run in device using the Android 4.0.x or above, because some services like Discovery service using the multicast and maybe the emulator not support this feature. 

PS2: Be sure to check if your WiFi network has support multicast.


### Import Windows Phone components to Visual Studio 2013

In cloned directory has a folder called "windows_phone" and click on “MpOS.sln” for open project on VS 2013.

PS: For test the BenchImage2 app is recommended to run in device using the Windows Phone 8.x or above. 

PS2: Be sure to check if your WiFi network has support multicast.


### Execute the MpOS Plataform on Prompt

1. You need to navigated to cloned directory -> folder "plataform" -> folder "executables"

2. Execute this command

```
java -jar mpos_plataform.jar
```

PS: To kill the application on Prompt is using the “Ctrl+C”.

PS2: You should to check the firewall permissions or disable it. 


### Execute the BenchImage app

Execute BenchImage on Android using the Eclipse ADT and on Windows Phone using the VS 2013. On MpOS Plataform CLI you’ll see the multicast and deploy service working. When you load the app, selected on: 

```
“Filtro” -> Original to Cartoonized;
```
```
“Processamento” -> Local to Cloudlet; 
```

Press “Inicia” to starting the offloading application and as resulting you’ll see the figure with cartoonizer filter.

PS: On system monitor maybe you’ll see the high network activity and fast app executing when compared with “Local” executing on “Processamento”.



Contributing
-------
In general, we follow the fork-and-pull git workflow:

1. Fork the repository on GitHub
2. Commit changes to a branch in your fork
3. Pull request "upstream" with your changes
4. Merge changes in to "upstream" repository

:warning: Be sure to merge the latest from "upstream" before making a pull request.



License and Author
-------
Copyright (C) 2014 Philipp B. Costa

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
```
       http://www.apache.org/licenses/LICENSE-2.0
```
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
