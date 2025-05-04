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

package dev.magicmq.docstranslator.translate;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import dev.magicmq.docstranslator.module.Module;
import dev.magicmq.docstranslator.module.init.InitPyRegistry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

public abstract class AbstractTranslator {

    protected final Path outputFolder;
    protected final InitPyRegistry registry;

    public AbstractTranslator(Path outputFolder, InitPyRegistry registry) {
        this.outputFolder = outputFolder;
        this.registry = registry;
    }

    public abstract void translate();

    protected String translateSource(Path sourceFilePath, String groupId, String artifactId, String artifactVersion, String className, JdkTranslator jdkTranslator) throws IOException, NoSuchElementException {
        String fileContent = Files.readString(sourceFilePath);

        CompilationUnit cu = new JavaParser().parse(fileContent).getResult().orElseThrow();

        String packageName = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .orElse("");

        Module module = new Module(
                groupId,
                artifactId,
                artifactVersion,
                packageName,
                className,
                jdkTranslator
        );
        module.init(cu);

        return module.translate();
    }

    protected void saveTranslatedFile(Path outputFilePath, String text) throws IOException {
        Files.createDirectories(outputFilePath.getParent());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath.toFile()))) {
            writer.write(text);
        }
    }

    protected void generateInitPy(Path folder) {
        if (!registry.doesInitPyExistAt(folder)) {
            registry.newInitPy(folder);
        }
    }
}
