/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package dev.magicmq.docstranslator.config.packaging;


import java.util.List;

public class Packaging {

    private Setup setup;
    private PyProject pyProject;
    private List<String> manifest;
    private String license;

    public Setup getSetup() {
        return setup;
    }

    public void setSetup(Setup setup) {
        this.setup = setup;
    }

    public PyProject getPyProject() {
        return pyProject;
    }

    public void setPyProject(PyProject pyProject) {
        this.pyProject = pyProject;
    }

    public List<String> getManifest() {
        return manifest;
    }

    public void setManifest(List<String> manifest) {
        this.manifest = manifest;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
