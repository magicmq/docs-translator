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


import dev.magicmq.docstranslator.SettingsProvider;
import dev.magicmq.docstranslator.module.init.InitPyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class JdkTranslator extends AbstractTranslator {

    private static final Logger logger = LoggerFactory.getLogger(JdkTranslator.class);

    private final Path javaSourcesPath;
    private final List<Path> neededSourcesPaths;

    public JdkTranslator(Path javaSourcesPath, Path outputFolder, InitPyRegistry registry) {
        super(outputFolder, registry);
        this.javaSourcesPath = javaSourcesPath;
        neededSourcesPaths = new ArrayList<>();
    }

    @Override
    public void translate() {
        for (Path sourceFilePath : neededSourcesPaths) {
            Path parentPath = sourceFilePath.getParent();
            generateInitPy(parentPath);

            try {
                String sourceFileName = sourceFilePath.getFileName().toString();
                String className = sourceFileName.substring(0, sourceFileName.lastIndexOf("."));

                Path absoluteSourcePath = javaSourcesPath.resolve(sourceFilePath);

                logger.debug("Processing JDK source file '{}'", sourceFilePath);

                registry.getInitPyAt(parentPath).addImport(className);

                String translated = translateSource(
                        absoluteSourcePath,
                        SettingsProvider.get().getSettings().getJdkSources().getGroup(),
                        SettingsProvider.get().getSettings().getJdkSources().getName(),
                        SettingsProvider.get().getSettings().getJdkSources().getVersion(),
                        className,
                        null
                );
                Path outputFilePath = outputFolder.resolve(sourceFilePath.getParent()).resolve(className + ".py");
                saveTranslatedFile(outputFilePath, translated);
            } catch (NoSuchElementException e) {
                logger.error("JavaParser was unable to parse JDK source file '{}'", sourceFilePath, e);
            } catch (NoSuchFileException e) {
                logger.error("Could not process JDK source file '{}' because the file does not exist", sourceFilePath);
            } catch (IOException e) {
                logger.error("Error when processing JDK source file '{}'", sourceFilePath, e);
            }
        }
    }

    public void addSourceFile(String packageName, String className) {
        Path sourceFilePath = Path.of(packageName.replace(".", "/")).resolve(Path.of(className + ".java"));
        if (!neededSourcesPaths.contains(sourceFilePath))
            neededSourcesPaths.add(sourceFilePath);
    }
}
