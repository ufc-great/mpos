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
package br.ufc.mdcc.mpos.net.rpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import br.ufc.mdcc.mpos.net.TcpServerPool;
import br.ufc.mdcc.mpos.net.rpc.deploy.model.RpcService;
import br.ufc.mdcc.mpos.net.rpc.model.Code;
import br.ufc.mdcc.mpos.net.rpc.model.RpcProfile;
import br.ufc.mdcc.mpos.net.rpc.util.ClassLoaderException;
import br.ufc.mdcc.mpos.net.rpc.util.DebugBufferedInputStream;
import br.ufc.mdcc.mpos.net.rpc.util.DynamicClassLoaderEvent;
import br.ufc.mdcc.mpos.net.rpc.util.JarClassLoader;
import br.ufc.mdcc.mpos.net.rpc.util.RpcInputStream;
import br.ufc.mdcc.mpos.net.rpc.util.RpcSerializable;

/**
 * This server implementation was used for rpc (remote procedure call),
 * under TCP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class RpcTcpServer extends TcpServerPool{
    private JarClassLoader jarLoader = null;
    private final int BUFFER_SIZE = 4096;// 4kb

    public RpcTcpServer(String cloudletIp, RpcService service) {
        super(cloudletIp, service, RpcTcpServer.class);
        startMessage = "Service: " + service.getName() + "_" + service.getVersionApp() + "v, was started on port: " + service.getPort();
        
        //only to get TcpServerPool class loader when start this thread 
        setDynamicClassLoaderEvent(new DynamicClassLoaderEvent() {       
            @Override
            public void setCurrentClassLoader(ClassLoader current) throws ClassLoaderException {
                jarLoader = new JarClassLoader(current, getService().getDependencies());
            }
        });
    }

    /**
     * Advice: All variables should be local to ensure thread-safe operations.
     */
    @Override
    public void clientRequest(Socket connection) throws IOException {
        CallRemotable call = null;
        Object objReturn = null;

        CloseableStream closeable = new CloseableStream();
        OutputStream output = connection.getOutputStream();
        InputStream input = connection.getInputStream();

        try {
            call = receive(input, closeable);
            if (call.debug == null) {
                objReturn = invokeMethod(call);
            } else {
                long initCpuTime = System.currentTimeMillis();
                objReturn = invokeMethod(call);
                call.debug.setExecutionCpuTime(System.currentTimeMillis() - initCpuTime);
            }

            send(output, closeable, objReturn, call);
        } catch (Exception e) {
            if (call != null) {
                logger.info("Method call: " + call.methodName + ", from object: " + call.objLocal.getClass().getName() + " failed!", e);
            } else {
                logger.info("CallRemotable have some params which didn't loading correctly. ", e);
            }
            sentError(output, closeable, e);
        } finally {
            waitClientClose(input);

            objReturn = null;
            call = null;

            close(closeable.dis);
            close(closeable.ois);
            close(closeable.dos);
            close(closeable.oos);

            closeable = null;
        }
    }

    private Object invokeMethod(CallRemotable call) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        Object serverObject = call.objLocal;

        for (Method method : serverObject.getClass().getMethods()) {
            if (method.getName().equals(call.methodName)) {

                Class<?>[] params = method.getParameterTypes();
                if (params.length == call.methodParams.length) {
                    try {
                        return method.invoke(serverObject, call.methodParams);
                    } catch (IllegalArgumentException e) {
                        // try call another overloaded method
                    }
                }
            }
        }
        return null;//not found...
    }

    @SuppressWarnings("resource")
    private CallRemotable receive(InputStream is, CloseableStream closeable) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        CallRemotable call = new CallRemotable();
        byte dataFlag[] = new byte[1];

        is.read(dataFlag);
        if (dataFlag[0] == Code.DATASTREAM) {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(is, BUFFER_SIZE));

            String clsName = dis.readUTF();
            Object objLocal = jarLoader.newInstance(clsName);

            if (objLocal != null) {
                call.objLocal = objLocal;
                call.manualSerialization = true;
                call.methodName = dis.readUTF();
                call.methodParams = ((RpcSerializable) objLocal).readMethodParams(dis, call.methodName);
            } else {
                throw new ClassNotFoundException("Update your dependence in side client, not found this class:" + clsName);
            }
            closeable.dis = dis;
        } else if (dataFlag[0] == Code.OBJECTSTREAM) {
            ObjectInputStream ois = new RpcInputStream(jarLoader, new BufferedInputStream(is, BUFFER_SIZE));

            String clsName = ois.readUTF();
            Object objLocal = jarLoader.newInstance(clsName);

            if (objLocal != null) {
                call.objLocal = objLocal;
                call.methodName = ois.readUTF();
                call.methodParams = (Object[]) ois.readObject();
            } else {
                throw new ClassNotFoundException("Update your dependence in side client, not found this class:" + clsName);
            }
            closeable.ois = ois;
        } else if (dataFlag[0] == Code.DATASTREAMDEBUG) {
            call.debug = new RpcProfile();

            DebugBufferedInputStream bufInput = new DebugBufferedInputStream(is, BUFFER_SIZE);
            DataInputStream dis = new DataInputStream(bufInput);

            String clsName = dis.readUTF();
            Object objLocal = jarLoader.newInstance(clsName);

            if (objLocal != null) {
                call.objLocal = objLocal;
                call.methodName = dis.readUTF();

                call.manualSerialization = true;
                long totalDownloadTime = System.currentTimeMillis();
                call.methodParams = ((RpcSerializable) objLocal).readMethodParams(dis, call.methodName);
                call.debug.setDonwloadTime(System.currentTimeMillis() - totalDownloadTime);
                call.debug.setDownloadSize(bufInput.getTotalReadData());
            } else {
                throw new ClassNotFoundException("Update your dependence in side client, not found this class:" + clsName);
            }
            closeable.dis = dis;
        } else if (dataFlag[0] == Code.OBJECTSTREAMDEBUG) {
            call.debug = new RpcProfile();

            DebugBufferedInputStream bufInput = new DebugBufferedInputStream(is, BUFFER_SIZE);
            ObjectInputStream ois = new RpcInputStream(jarLoader, bufInput);

            String clsName = ois.readUTF();
            Object objLocal = jarLoader.newInstance(clsName);

            if (objLocal != null) {
                call.objLocal = objLocal;
                call.methodName = ois.readUTF();
                // avoid the initial lag and calcule param delay
                long totalDownloadTime = System.currentTimeMillis();
                call.methodParams = (Object[]) ois.readObject();
                call.debug.setDonwloadTime(System.currentTimeMillis() - totalDownloadTime);
                call.debug.setDownloadSize(bufInput.getTotalReadData());
            } else {
                throw new ClassNotFoundException("Update your dependence in side client, not found this class:" + clsName);
            }
            closeable.ois = ois;
        }
        return call;
    }

    private void sentError(OutputStream os, CloseableStream closeable, Exception e) throws IOException {
        byte dataFlag[] = new byte[1];

        dataFlag[0] = Code.DATASTREAM;
        os.write(dataFlag);

        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os, BUFFER_SIZE));
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        dos.writeInt(Code.METHOD_THROW_ERROR);
        dos.writeUTF(sw.toString());
        dos.flush();

        closeable.dos = dos;
    }

    private void send(OutputStream os, CloseableStream closeable, Object objReturn, CallRemotable call) throws IOException {
        byte dataFlag[] = new byte[1];

        if (call.manualSerialization) {
            if (call.debug == null) {
                dataFlag[0] = Code.DATASTREAM;
                os.write(dataFlag);

                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os, BUFFER_SIZE));
                dos.writeInt(Code.OK);
                ((RpcSerializable) call.objLocal).writeMethodReturn(dos, call.methodName, objReturn);

                dos.flush();
                closeable.dos = dos;
            } else {
                dataFlag[0] = Code.DATASTREAMDEBUG;
                os.write(dataFlag);

                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os, BUFFER_SIZE));
                dos.writeInt(Code.OK);
                dos.writeInt(call.debug.getDownloadSize());//download on server perspective
                dos.writeLong(call.debug.getDonwloadTime());
                dos.writeLong(call.debug.getExecutionCpuTime());
                ((RpcSerializable) call.objLocal).writeMethodReturn(dos, call.methodName, objReturn);

                dos.flush();
                closeable.dos = dos;
            }
        } else {
            if (call.debug == null) {
                dataFlag[0] = Code.OBJECTSTREAM;
                os.write(dataFlag);

                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(os, BUFFER_SIZE));
                oos.writeInt(Code.OK);
                oos.writeObject(objReturn);

                oos.flush();
                closeable.oos = oos;
            } else {
                dataFlag[0] = Code.OBJECTSTREAMDEBUG;
                os.write(dataFlag);

                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(os, BUFFER_SIZE));
                oos.writeInt(Code.OK);
                oos.writeInt(call.debug.getDownloadSize());
                oos.writeLong(call.debug.getDonwloadTime());
                oos.writeLong(call.debug.getExecutionCpuTime());
                oos.writeObject(objReturn);

                oos.flush();
                closeable.oos = oos;
            }
        }
    }

    private void waitClientClose(InputStream input) {
        try {
            input.read(new byte[4]);
        } catch (IOException e) {
            logger.info("Some error during close socket!", e);
        }
    }

    @Override
    public RpcService getService() {
        return (RpcService) super.getService();
    }

    // wrapper class, for close stream correctly
    private final class CloseableStream {
        DataInputStream dis;
        DataOutputStream dos;
        ObjectInputStream ois;
        ObjectOutputStream oos;
    }

    private final class CallRemotable {
        boolean manualSerialization = false;
        String methodName;
        Object methodParams[];
        Object objLocal;
        RpcProfile debug;
    }
}