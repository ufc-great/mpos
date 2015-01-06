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
package br.ufc.mdcc.mpos.net.rpc.deploy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.util.Log;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.core.ClientTcp;
import br.ufc.mdcc.mpos.net.core.FactoryClient;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;

/**
 * This implementation made deploy dependences in remote machine,
 * under TCP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class DeployService extends Thread {
    private final String clsName = DeployService.class.getName();

    private Context context;
    private ServerContent server;

    private String appVersion;
    private String appName;

    private String dependenceDir = "dep";
    private final int BUFFER_SIZE = 8192;

    public DeployService(Context context, ServerContent server) {
        this.context = context;
        this.server = server;

        appName = MposFramework.getInstance().getDeviceController().getAppName();
        appVersion = MposFramework.getInstance().getDeviceController().getAppVersion();
    }

    @Override
    public void run() {
        Log.i(clsName, "Started Deploy App " + appName + "/" + appVersion + " on Remote Server (" + server.getIp() + ":" + server.getDeployAppPort() + ")");

        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;

        ClientTcp client = (ClientTcp) FactoryClient.getInstance(Protocol.TCP_STREAM);
        try {
            client.connect(server.getIp(), server.getDeployAppPort());

            outputStream = new DataOutputStream(new BufferedOutputStream(client.getOutputStream(), BUFFER_SIZE));
            inputStream = new DataInputStream(new BufferedInputStream(client.getInputStream(), BUFFER_SIZE));

            String deployRequest = "mpos_deploy_app:" + appName + ":" + appVersion;
            send(outputStream, deployRequest);
            if (!receiveFeedbackMessage(inputStream)) {
                throw new DeployException("Problems in deploy request");
            }

            String fileDep[] = context.getAssets().list(dependenceDir);
            if (fileDep.length == 0) {
                throw new IOException("Diretory in assets \"dep\" need to be create and put application jar inside with dependences!!");
            }

            String mposDependence = "mpos_dependence_size:" + fileDep.length;
            send(outputStream, mposDependence);
            if (!receiveFeedbackMessage(inputStream)) {
                throw new DeployException("Problems in sent dependence size");
            }

            sentFiles(inputStream, outputStream, fileDep);

            server.setRpcServicePort(receiveServicePort(inputStream));

            Log.i(clsName, "Service: " + appName + ", deployed on: " + server.getIp() + ":" + server.getRpcServicePort());
        } catch (IOException e) {
            Log.e(clsName, e.getMessage(), e);
        } catch (MissedEventException e) {
            Log.e(clsName, "Didn't TCP Manual?", e);
        } catch (InterruptedException e) {
            Log.e(clsName, "Thread Interrupted?", e);
        } finally {
            Log.i(clsName, "Finished Deploy App " + appName + "/" + appVersion + " on Remote Server.");
            if (client != null) {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    client.close();
                } catch (IOException e) {
                    Log.e(clsName, e.getMessage(), e);
                }
            }
        }
    }

    private void sentFiles(DataInput input, DataOutputStream output, String dependenceFiles[]) throws IOException, InterruptedException {
        for (String name : dependenceFiles) {
            InputStream fileResource = context.getAssets().open(dependenceDir + "/" + name);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fileToArray(baos, fileResource);

            byte[] data = baos.toByteArray();
            output.writeUTF(name);
            output.writeInt(data.length);
            output.write(data, 0, data.length);
            output.flush();

            baos.close();
            baos = null;

            if (!receiveFeedbackMessage(input)) {
                throw new DeployException("Problems in sent file to server");
            }
        }
    }

    private void send(DataOutputStream output, String data) throws IOException, InterruptedException {
        output.writeUTF(data);
        output.flush();
    }

    private int receiveServicePort(DataInput is) throws IOException {
        String message = is.readUTF();
        return Integer.parseInt(message.split(":")[1]);
    }

    private boolean receiveFeedbackMessage(DataInput is) throws IOException {
        String feedback = is.readUTF();
        return feedback.equals("ok");
    }

    private void fileToArray(OutputStream os, InputStream fileResource) throws IOException, InterruptedException {
        byte fileBuffer[] = new byte[1024 * 16];
        int read = 0;
        while ((read = fileResource.read(fileBuffer)) > 0) {
            os.write(fileBuffer, 0, read);
        }
        os.flush();
    }
}