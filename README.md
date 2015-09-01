[MpOS Framework] (http://mpos.great.ufc.br)
====================================================================

This guide will provide you all the steps to download, import and run the BenchImage application, which can be used to demonstrate MpOS functionalities for both Android and Windows Phone platforms.

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
Import -> Android -> Existing Android Code Into Workspace
```
```
Browser the path where Project was cloned '<cloned_path>/android' and click on "OK" button
```
```
Selected the “BenchImage2” and “MpOS API” to import and click on “Finish” button
```
```
In each project “BenchImage2” and “MpOS API” go in Properties -> Android -> Selected 
"Target Name: Android 4.x" and click on "OK" button
```

You’ll see the projects “BenchImage2” and “MpOS API” on eclipse workspace. In BenchImage2 has an MainActivity to start the mobile app. 

Note:  In order to test the ‘BenchImage’ application, it is recommended to use a device with Android 4.0.X or above, because of the multicast-based discovery system. Emulator is not recommend either. 


#### Import Windows Phone components to Visual Studio 2013

* Browse the path where the project is located.

* Enter in the “windows_phone” folder.

* Click on “MpOS.sln” in order to open the project on VS 2013.

Note: For test the BenchImage2 app is recommended to run in device using the Windows Phone 8.x or above. 


#### Execute the MpOS Plataform

* Browse the path where the project is located:

```
cloned directory -> folder "plataform" -> folder "executables"
```
```
configure the machine IP in config.properties "cloudlet.interface.ip"
```

* Open Prompt (or console) and navigate again to “executables” folder 

* Execute this command:

```
java -jar mpos_plataform.jar
```

Note: To kill the application on Prompt is using the “Ctrl+C”.

Note2: You should check the firewall permissions to allow mpos_plataform.jar to run properly. And also if the wireless access point supports multicast protocol. 


#### Execute the BenchImage app

Execute BenchImage on Android using the Eclipse ADT and on Windows Phone using the VS 2013. When you load the mobile app, selected on: 

```
“Filtro” -> Original to Cartoonized;
```
```
“Processamento” -> Local to Cloudlet; 
```

Press “Inicia” to starting the offloading application and as resulting you’ll see the figure with cartoonizer filter.

Note: On system monitor maybe you’ll see the high network activity and fast app executing when compared with “Local” executing on “Processamento”.

Note: On MpOS Plataform CLI, you’ll be able to see the multicast and deployment services working. And you’ll also able to see a high network activity when the offloading system is working. Depending on the mobile device, execution environment and network conditions, the filter should be executed faster outside of the mobile device (Cloudlet or Internet).



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



How to Cite
-------
If you are using the MpOS Framework for your research, please do not forget to cite. Thanks!

Philipp B. Costa, Paulo A. L. Rego, Lincoln S. Rocha, Fernando A. M. Trinta, and José N. de Souza. 2015. MpOS: a multiplatform offloading system. In Proceedings of the 30th Annual ACM Symposium on Applied Computing (SAC '15). ACM, New York, NY, USA, 577-584.

The Bibtex format is:

@inproceedings{Costa:2015:MMO:2695664.2695945,
 author = {Costa, Philipp B. and Rego, Paulo A. L. and Rocha, Lincoln S. and Trinta, Fernando A. M. and de Souza, Jos{\'e} N.},
 title = {MpOS: A Multiplatform Offloading System},
 booktitle = {Proceedings of the 30th Annual ACM Symposium on Applied Computing},
 series = {SAC '15},
 year = {2015},
 isbn = {978-1-4503-3196-8},
 location = {Salamanca, Spain},
 pages = {577--584},
 numpages = {8},
 url = {http://doi.acm.org/10.1145/2695664.2695945},
 doi = {10.1145/2695664.2695945},
 acmid = {2695945},
 publisher = {ACM},
 address = {New York, NY, USA},
 keywords = {mobile cloud computing, multiple platforms, offloading},
} 
