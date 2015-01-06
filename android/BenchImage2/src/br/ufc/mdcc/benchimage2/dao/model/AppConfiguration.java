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
package br.ufc.mdcc.benchimage2.dao.model;

/**
 * Configuration UI Control
 * 
 * @author Philipp
 */
public final class AppConfiguration {
    private String local;
    private String image;
    private String filter;
    private String size;
    private String outputDirectory;

    public AppConfiguration() {
        this(null, null, null, null);
    }

    public AppConfiguration(String local, String image, String filter, String size) {
        this.local = local;
        this.image = image;
        this.filter = filter;
        this.size = size;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public String toString() {
        return "AppConfiguration [local=" + local + ", image=" + image + ", filter=" + filter + ", size=" + size + ", outputDirectory=" + outputDirectory + "]";
    }
}