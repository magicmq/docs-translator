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

package dev.magicmq.docstranslator;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import dev.magicmq.docstranslator.module.Module;
import dev.magicmq.docstranslator.module.init.InitPyRegistry;
import org.eclipse.aether.artifact.Artifact;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Translator {

    private final List<Artifact> artifacts;
    private final Path outputFolder;
    private final InitPyRegistry registry;
    private final JdkTranslator jdkTranslator;

    public Translator(List<Artifact> artifacts, Path javaSourcesPath, Path outputFolder) {
        this.artifacts = artifacts;
        this.outputFolder = outputFolder;
        this.registry = new InitPyRegistry();
        this.jdkTranslator = new JdkTranslator(javaSourcesPath, this.outputFolder, registry);
    }

    public void translate() throws IOException {
        for (Artifact artifact : artifacts) {
            Path jarFilePath = artifact.getFile().toPath();

            String groupId = artifact.getGroupId();
            String artifactId = artifact.getArtifactId();
            String artifactVersion = artifact.getVersion();

            try (FileSystem jarFileSystem = FileSystems.newFileSystem(jarFilePath)) {
                Path root = jarFileSystem.getPath("");

                try (Stream<Path> walk = Files.walk(root)) {
                    translateSources(walk, groupId, artifactId, artifactVersion);
                }
            }
        }

        if (DocsTranslator.get().getSettings().getJdkSources().isTranslate()) {
            DocsTranslator.get().getLogger().log(Level.INFO, "Translating JDK sources...");
            jdkTranslator.translateSources();
        }

        DocsTranslator.get().getLogger().log(Level.INFO, "Saving __init__.py files...");
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

                            DocsTranslator.get().getLogger().log(Level.FINE, "Processing source file '" + sourceFileName + "'");

                            registry.getInitPyAt(parentPath).addImport(className);

                            String translated = translateSource(path, groupId, artifactId, artifactVersion, className);
                            Path outputFilePath = outputFolder.resolve(physicalPath.getParent()).resolve(className + ".py");
                            saveTranslatedFile(outputFilePath, translated);

                            DocsTranslator.get().getLogger().log(Level.FINE, "Finished processing source file '" + sourceFileName + "'");
                        }
                    } catch (IOException e) {
                        DocsTranslator.get().getLogger().log(Level.SEVERE, "Error when processing source file '" + path.getFileName().toString() + "'", e);
                        e.printStackTrace();
                    }
        });
    }

    private String translateSource(Path sourceFilePath, String groupId, String artifactId, String artifactVersion, String className) throws IOException {
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

    private void saveTranslatedFile(Path outputFilePath, String text) throws IOException {
        Files.createDirectories(outputFilePath.getParent());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath.toFile()))) {
            writer.write(text);
        } catch (IOException e) {
            DocsTranslator.get().getLogger().log(Level.SEVERE, "Error when saving file '" + outputFilePath.getFileName().toString() + "' to output folder", e);
            e.printStackTrace();
        }
    }

    private void generateInitPy(Path folder) {
        if (!registry.doesInitPyExistAt(folder)) {
            registry.newInitPy(folder);
        }
    }

}
