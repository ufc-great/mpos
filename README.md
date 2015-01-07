# 1. Demo how to the BenchImage work with MpOS framework.

Technologies used:

1. Clone the MpOS project from github
2. Windows 8.1 (x64) Professional (for Linux and Mac OS X only Android side works)
3. Android side:
  1. Eclipse Platform 4.2.1v with ADT 23.0.x
  2. Oracle Java SDK 8.x (for any OS)
  3. Android SDK installed (API 19 or above)
4. Windows Phone side:
  1. Visual Studio Professional 2013 (with Update 4) or Community 2013 (not tested)
  2. Visual Studio SDK 2013 (with Update 4)
  3. Windows Phone emulators (need Win 8.1 x64 Pro with client Hyper-V installed)


## 1.1. Import the Android components on Eclipse ADT

1. Import -> Git -> Projects from Git

 <image>

2. Click in “Local” and “Add” a repository

 <image>

3. Selected the path where Project was cloned and clicked in Finish

 <image>

4. Selected the repository cloned and click “next”

 <image>

5. Choose “Import existing projects” and click “Next”

 <image>

6. Selected the “BenchImage2” and “MpOS API” to import and click in “Finish”

 <image>

You’ll see the projects “BenchImage2” and “MpOS API” on eclipse workspace. In BenchImage2 has an MainActivity to start the mobile app. In “assets” folder has many resources among them a “dep” folder with benchimage.jar which was generated from this application and dependencies. This jar's application can be generated in BenchImage2 -> Export -> Jar File. Finally, the MpOS API is a Library Project where you attach your Project to use the MpOS components, this project has been attached on the BenchImage2.

PS: For test the BenchImage2 app is recommended to run in device using the Android 4.0.x or above, because some services like Discovery service using the multicast and maybe the emulator not support this feature. Execute the BenchImage2 app after the start the MpOS Platform.


## 1.2. Import the Windows Phone components to Visual Studio 2013

1. On cloned Project was a folder called “Windows phone” and click on “MpOS.sln” for open project on VS 2013.

PS: For test the BenchImage2 app is recommended to run in device using the Windows Phone 8.x or above. Be sure to check if your WiFi network has support multicast. Execute the BenchImage2 app after the start the MpOS Platform.


## 1.3. Execute the MpOS Plataform on Prompt

1. You need to navigated to cloned Project -> plataform -> executables

2. Execute this command: java -jar mpos_plataform.jar

PS: Didn’t forget to permit or disable the firewall application. To kill the application on Prompt is using the “Ctrl+C”.


## 1.4. Execute the BenchImage app

1. Execute BenchImage on Android using the Eclipse ADT and on Windows Phone using the VS 2013. On MpOS Plataform CLI you’ll see the multicast and deploy service working. When you load the app, selected on: 

“Filtro” -> Original to Cartoonized;

“Processamento” -> Local to Cloudlet; 

2. Press “Inicia” to starting the offloading application and as resulting you’ll see the figure with cartoonizer filter.

PS: On system monitor maybe you’ll see the high network activity and fast app executing when compared with “Local” executing on “Processamento”.
