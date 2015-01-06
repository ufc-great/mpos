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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import br.ufc.mdcc.mpos.config.MposConfig;
import br.ufc.mdcc.mpos.config.Inject;
import br.ufc.mdcc.mpos.net.endpoint.EndpointController;
import br.ufc.mdcc.mpos.net.exceptions.NetworkException;
import br.ufc.mdcc.mpos.net.profile.ProfileController;
import br.ufc.mdcc.mpos.offload.ProxyHandler;
import br.ufc.mdcc.mpos.offload.Remotable;
import br.ufc.mdcc.mpos.util.device.DeviceController;

/**
 * This class start all service from MpOS framework
 * 
 * @author Philipp B. Costa
 */
public final class MposFramework {
    private final String clsName = MposFramework.class.getName();
    private final static MposFramework instance = new MposFramework();

    private EndpointController endpointController;
    private ProfileController profileController;
    private DeviceController deviceController;

    //start only once...
    private boolean start = false;

    private MposFramework() {}

    public static MposFramework getInstance() {
        return instance;
    }

    public void start(Activity activity) {
        try {
            
            //only network services
            if (!start) {
                start = true;
                configureControllers(activity);
            }
            
            injectObjects(activity);
            Log.i(clsName, ">>> Finish MpOS Framework loading!");
        } catch (InstantiationException e) {
            Log.e(clsName, "Instantiation Error!", e);
        } catch (IllegalAccessException e) {
            Log.e(clsName, "You class is public visibility?", e);
        } catch (ClassNotFoundException e) {
            Log.e(clsName, "Class Not Found Error!", e);
        } catch (NetworkException e) {
            Log.e(clsName, "Network Error: ", e);
        } catch (NameNotFoundException e) {
            Log.e(clsName, "Class Not Found Error!", e);
        } catch (RuntimeException e) {
            Log.e(clsName, "Any runtime exception", e);
        }
    }

    public void stop() {
        deviceController.removeLocationListener();

        deviceController.destroy();
        profileController.destroy();
        endpointController.destroy();

        start = false;
        Log.d(clsName, "Finish MpOS API Framework!");
    }

    private void injectObjects(Activity activity) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class<?> cls = activity.getClass();

        Field fields[] = cls.getDeclaredFields();
        for (Field f : fields) {
            Inject anno = f.getAnnotation(Inject.class);
            if (anno != null) {
                f.setAccessible(true);

                if (f.getType().isInterface()) {
                    Method methods[] = f.getType().getMethods();
                    boolean remoteSupport = false;
                    for (Method method : methods) {
                        if (method.getAnnotation(Remotable.class) != null) {
                            remoteSupport = true;
                            break;
                        }
                    }

                    if (remoteSupport) {
                        f.set(activity, ProxyHandler.newInstance(anno.value(), f.getType()));
                    } else {
                        throw new InstantiationException("The injection required a interface with remotable annotation!");
                    }
                } else {
                    throw new InstantiationException("The injection annotation required a object interface, not a concrete class or primitive type!");
                }
            }
        }
    }

    private void configureControllers(Activity activity) throws NetworkException, NameNotFoundException {
        Class<?> cls = activity.getClass();
        Context context = activity.getApplicationContext();

        deviceController = new DeviceController(activity);

        Annotation[] annotations = cls.getAnnotations();
        for (Annotation anno : annotations) {
            if (anno instanceof MposConfig) {
                MposConfig app = cls.getAnnotation(MposConfig.class);

                if (app.deviceDetails()) {
                    deviceController.collectDeviceConfig();
                }
                if (app.locationCollect()) {
                    deviceController.collectLocation();
                }

                String endpointSecondary = app.endpointSecondary();
                if (endpointSecondary.equals("")) {
                    endpointSecondary = null;
                }

                if (endpointSecondary == null && !app.discoveryCloudlet()) {
                    throw new NetworkException("You must define an internet server IP or allow the service discovery!");
                }

                profileController = new ProfileController(context, app.profile());
                endpointController = new EndpointController(context, endpointSecondary, app.decisionMakerActive(), app.discoveryCloudlet());

                break;
            }
        }
    }

    public DeviceController getDeviceController() {
        return deviceController;
    }

    public EndpointController getEndpointController() {
        return endpointController;
    }

    public ProfileController getProfileController() {
        return profileController;
    }
}