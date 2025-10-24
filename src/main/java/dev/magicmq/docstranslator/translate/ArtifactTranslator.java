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
import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class ArtifactTranslator extends Translator {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactTranslator.class);

    private final JdkTranslator jdkTranslator;

    public ArtifactTranslator(Path javaSourcesPath, Path outputFolder) {
        super(outputFolder, new InitPyRegistry());
        this.jdkTranslator = new JdkTranslator(javaSourcesPath, this.outputFolder, this.registry);
    }

    public void translate(List<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            logger.info("Translating '{}'", artifact);

            Path jarFilePath = artifact.getFile().toPath();

            String groupId = artifact.getGroupId();
            String artifactId = artifact.getArtifactId();
            String artifactVersion = artifact.getVersion();

            try (FileSystem jarFileSystem = FileSystems.newFileSystem(jarFilePath)) {
                Path root = jarFileSystem.getPath("");

                try (Stream<Path> walk = Files.walk(root)) {
                    translateSources(walk, groupId, artifactId, artifactVersion);
                } catch (IOException e) {
                    logger.error("Error when iterating over files in JAR file '{}'", jarFilePath, e);
                }
            } catch (IOException e) {
                logger.error("Error when opening JAR file '{}'", jarFilePath, e);
            }
        }
    }

    public void doPostTranslateTasks() {
        if (SettingsProvider.get().getSettings().getJdkSources().isTranslate()) {
            logger.info("Translating JDK sources...");
            jdkTranslator.translate();
        } else
            logger.warn("Skipping translation of JDK sources, per the settings.yml");

        logger.info("Saving __init__.py files...");
        registry.saveInitPys(outputFolder);
    }

    private void translateSources(Stream<Path> walker, String groupId, String artifactId, String artifactVersion) {
        walker
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".java"))
                .forEach(path -> {
                    Path physicalPath = Path.of(path.toString());
                    try {
                        String sourceFileName = path.getFileName().toString();
                        String className = sourceFileName.substring(0, sourceFileName.lastIndexOf("."));

                        if (!className.equals("package-info") && !className.equals("module-info")) {
                            Path parentPath = physicalPath.getParent();
                            generateInitPy(parentPath);

                            logger.debug("Processing source file '{}'", path);

                            registry.getInitPyAt(parentPath).addImport(className);

                            String translated = translateSource(path, groupId, artifactId, artifactVersion, className, jdkTranslator);
                            Path outputFilePath = outputFolder.resolve(physicalPath.getParent()).resolve(className + ".py");
                            saveTranslatedFile(outputFilePath, translated);
                        }
                    } catch (NoSuchElementException e) {
                        logger.error("JavaParser was unable to parse source file '{}'", path, e);
                    }  catch (Exception e) {
                        logger.error("Error when processing source file '{}'", path, e);
                    }
        });
    }
}
