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

package dev.magicmq.docstranslator.module.init;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InitPyRegistry {

    private final List<InitPy> initPys;

    public InitPyRegistry() {
        this.initPys = new ArrayList<>();
    }

    public boolean doesInitPyExistAt(Path path) {
        return getInitPyAt(path) != null;
    }

    public InitPy getInitPyAt(Path path) {
        for (InitPy initPy : initPys) {
            if (initPy.getPath().equals(path))
                return initPy;
        }
        return null;
    }

    public void newInitPy(Path path) {
        initPys.add(new InitPy(path));
    }

    public void saveInitPys(Path outputFolderPath) {
        for (InitPy initPy : initPys)
            initPy.saveToFolder(outputFolderPath);
    }
}
