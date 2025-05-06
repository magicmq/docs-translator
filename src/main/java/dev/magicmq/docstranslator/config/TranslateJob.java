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

package dev.magicmq.docstranslator.config;


import java.util.List;

public class TranslateJob {

    private String pyPIName;
    private String pyPIVersion;
    private List<String> artifacts;
    private List<String> pyModules;

    public String getPyPIName() {
        return pyPIName;
    }

    public void setPyPIName(String pyPIName) {
        this.pyPIName = pyPIName;
    }

    public String getPyPIVersion() {
        return pyPIVersion;
    }

    public void setPyPIVersion(String pyPIVersion) {
        this.pyPIVersion = pyPIVersion;
    }

    public List<String> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<String> artifacts) {
        this.artifacts = artifacts;
    }

    public List<String> getPyModules() {
        return pyModules;
    }

    public void setPyModules(List<String> pyModules) {
        this.pyModules = pyModules;
    }
}
