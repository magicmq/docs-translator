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


import dev.magicmq.docstranslator.DocsTranslator;
import dev.magicmq.docstranslator.base.Translatable;
import dev.magicmq.docstranslator.module.Import;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InitPy implements Translatable {

    private final Path path;
    private final List<Import> imports;

    public InitPy(Path path) {
        this.path = path;
        this.imports = new ArrayList<>();
    }

    public Path getPath() {
        return path;
    }

    public void addImport(String moduleName) {
        imports.add(new Import("." + moduleName, moduleName));
    }

    @Override
    public String translate() {
        return imports.stream().map(Import::toString).collect(Collectors.joining("\n"));
    }

    public void saveToFolder(Path outputFolderPath) {
        Path fullPath = outputFolderPath.resolve(path);
        try {
            Files.createDirectories(fullPath);

            Path outputFilePath = fullPath.resolve(Path.of("__init__.py"));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath.toFile()))) {
                writer.write(translate());
            }

            DocsTranslator.get().getLogger().log(Level.FINE, "Saved __init__.py for folder '" + fullPath + "'");
        } catch (IOException e) {
            DocsTranslator.get().getLogger().log(Level.SEVERE, "Error when saving __init__.py to folder '" + fullPath + "'", e);
        }
    }

}
