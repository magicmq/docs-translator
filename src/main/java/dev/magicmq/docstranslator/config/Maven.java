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

public class Maven {

    private String path;
    private boolean useCentral;
    private List<Repository> repositories;
    private boolean deleteOnStart;
    private List<String> excludeArtifacts;
    private String dependencyScope;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isUseCentral() {
        return useCentral;
    }

    public void setUseCentral(boolean useCentral) {
        this.useCentral = useCentral;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public boolean isDeleteOnStart() {
        return deleteOnStart;
    }

    public void setDeleteOnStart(boolean deleteOnStart) {
        this.deleteOnStart = deleteOnStart;
    }

    public List<String> getExcludeArtifacts() {
        return excludeArtifacts;
    }

    public void setExcludeArtifacts(List<String> excludeArtifacts) {
        this.excludeArtifacts = excludeArtifacts;
    }

    public String getDependencyScope() {
        return dependencyScope;
    }

    public void setDependencyScope(String dependencyScope) {
        this.dependencyScope = dependencyScope;
    }
}
