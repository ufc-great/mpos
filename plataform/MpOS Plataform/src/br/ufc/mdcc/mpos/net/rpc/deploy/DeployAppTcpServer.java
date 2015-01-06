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
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import br.ufc.mdcc.mpos.controller.ServiceController;
import br.ufc.mdcc.mpos.net.TcpServer;
import br.ufc.mdcc.mpos.net.rpc.deploy.model.DependencePath;
import br.ufc.mdcc.mpos.net.rpc.deploy.model.RpcService;
import br.ufc.mdcc.mpos.net.util.Service;
import br.ufc.mdcc.mpos.persistence.FachadeDao;

/**
 * This server implementation was used for deploy the mobile app dependences,
 * under TCP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class DeployAppTcpServer extends TcpServer {
    private final int BUFFER_SIZE = 8192;// 8kb

    public DeployAppTcpServer(String cloudletIp, Service service) {
        super(cloudletIp, service, DeployAppTcpServer.class);
        startMessage = "Deploy App TCP started on port: " + service.getPort();
    }

    @Override
    public void clientRequest(Socket connection) throws IOException {
        logger.info("Starting deploy service!");

        RpcService service = new RpcService();

        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream(), BUFFER_SIZE));
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE));

        // e.g.: mpos_deploy_app:droid:Calc:1.1.19052013
        String clientMessage = null;
        if ((clientMessage = readMessage(inputStream, outputStream, "mpos_deploy_app")) == null) {
            return;// finished a conex from side client
        }
        
        service.processDeployMessage(clientMessage);

        String dir = "./app_dep/android/" + service.getName() + "_" + service.getVersionApp() + "v";
        checkDiretory(dir);

        if ((clientMessage = readMessage(inputStream, outputStream, "mpos_dependence_size")) == null) {
            return;
        }
        
        List<DependencePath> dependences = new ArrayList<DependencePath>();
        int countFiles = Integer.parseInt(clientMessage.split(":")[1]);
        for (int i = 0; i < countFiles; i++) {
            String name = inputStream.readUTF();
            int size = inputStream.readInt();
            byte[] data = new byte[size];
            inputStream.readFully(data, 0, size);

            FileOutputStream fos = new FileOutputStream(dir + "/" + name);
            fos.write(data);
            fos.flush();
            fos.close();

            dependences.add(new DependencePath(dir + "/" + name));
            sentMessage(outputStream, "ok");
        }

        service.setDependencies(dependences);
        service.setPort(generateServicePort());

        ServiceController.getInstance().startServiceRpc(service);
        
        FachadeDao.getInstance().getRpcServiceDao().add(service);
        sentMessage(outputStream, "mpos_dependence_port:" + service.getPort());

        logger.info("Service: " + service.getName() + ", deployed on port: " + service.getPort());

        close(inputStream);
        close(outputStream);
    }

    private String readMessage(DataInput input, DataOutputStream output, String header) throws IOException {
        String message = input.readUTF();
        if (!message.startsWith(header)) {
            return null;
        }
        sentMessage(output, "ok");
        return message;
    }

    private void sentMessage(DataOutputStream output, String message) throws IOException {
        output.writeUTF(message);
        output.flush();
    }

    private void checkDiretory(String path) {
        File dir = new File(path);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
    }

    private int generateServicePort() {
        int port = 0;
        Random random = new Random();
        List<Integer> registerPorts = FachadeDao.getInstance().getRpcServiceDao().getAllPorts();

        while (true) {
            port = random.nextInt(1000) + 36000;
            if (Collections.binarySearch(registerPorts, port) < 0) {
                break;
            }
        }
        return port;
    }
}