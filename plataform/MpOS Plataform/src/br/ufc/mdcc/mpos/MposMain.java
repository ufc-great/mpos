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
package br.ufc.mdcc.mpos;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import br.ufc.mdcc.mpos.controller.ServiceController;

/**
 * Main class for starting app.
 * 
 * call on console: java -jar mpos_plataform.jar
 * 
 * @author Philipp B. Costa
 */
public final class MposMain {
    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        PropertyConfigurator.configure(MposMain.class.getResourceAsStream("/br/ufc/mdcc/mpos/util/log4j.properties"));
    }

    private MposMain() {

    }

    public static void main(String... args) {
        Logger logger = Logger.getLogger(MposMain.class);
        try {
            ServiceController.getInstance().start();
            System.out.println("MpOS Platform for Android was started!");

            new MposMain().startDotNetSubsystem();
        } catch (Exception e) {
            logger.error("Some error in start MpOS", e);
            System.exit(0);
        }
    }

    //TODO: You need to kill manually the MpOS Platform on c_sharp
    private void startDotNetSubsystem() throws IOException {
        new Thread() {
            @Override
            public void run() {
                try {
                    String basePath = new File("").getAbsolutePath();

                    Process proc = null;
                    String[] cmd = {basePath + File.separator + "c_sharp" + File.separator + "MpOS Plataform.exe", basePath + File.separator};
                    proc = Runtime.getRuntime().exec(cmd);

                    InputStreamReader inputStreamReader = new InputStreamReader(proc.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    System.out.println("MpOS Platform for Windows Phone was started!");
                    
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}