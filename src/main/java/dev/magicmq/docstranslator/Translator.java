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

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Translator {

    private static final Pattern ARTIFACT_PATTERN = Pattern.compile("^([a-zA-Z0-9\\-]+?)-(\\d+\\.\\d+(?:\\.\\d+)?(?:-[a-zA-Z0-9-.]+)?)-sources(?:\\.jar)?$");

    private final List<Path> jarFilePaths;
    private final Path outputFolder;
    private final InitPyRegistry registry;
    private final JdkTranslator jdkTranslator;

    public Translator(List<Path> jarFilePaths, Path javaSourcesPath, Path outputFolder) {
        this.jarFilePaths = jarFilePaths;
        this.outputFolder = outputFolder;
        this.registry = new InitPyRegistry();
        this.jdkTranslator = new JdkTranslator(javaSourcesPath, this.outputFolder, registry);
    }

    public void translate() throws IOException {
        for (Path jarFilePath : jarFilePaths) {
            Matcher matcher = ARTIFACT_PATTERN.matcher(jarFilePath.getFileName().toString());
            matcher.find();
            String artifactId = matcher.group(1);
            String artifactVersion = matcher.group(2);

            try (FileSystem jarFileSystem = FileSystems.newFileSystem(jarFilePath)) {
                Path root = jarFileSystem.getPath("");

                try (Stream<Path> walk = Files.walk(root)) {
                    translateSources(walk, artifactId, artifactVersion);
                }
            }
        }

        DocsTranslator.get().getLogger().log(Level.INFO, "Translating JDK sources...");
        jdkTranslator.translateSources();

        DocsTranslator.get().getLogger().log(Level.INFO, "Saving __init__.py files...");
        registry.saveInitPys(outputFolder);
    }

    private void translateSources(Stream<Path> walker, String artifactId, String artifactVersion) {
        walker.forEach(path -> {
            Path physicalPath = Path.of(path.toString());
            if (Files.isDirectory(path)) {
                if (!path.toString().contains("META-INF") && !path.toString().isEmpty())
                    generateInitPy(physicalPath);
            } else {
                if (path.getFileName().toString().endsWith(".java")) {
                    try {
                        String sourceFileName = path.getFileName().toString();
                        String className = sourceFileName.substring(0, sourceFileName.lastIndexOf("."));

                        if (!className.equals("package-info") && !className.equals("module-info")) {
                            DocsTranslator.get().getLogger().log(Level.FINE, "Processing source file '" + sourceFileName + "'");

                            registry.getInitPyAt(physicalPath.getParent()).addImport(className);

                            String translated = translateSource(path, artifactId, artifactVersion, className);
                            Path outputFilePath = outputFolder.resolve(physicalPath.getParent()).resolve(className + ".py");
                            saveTranslatedFile(outputFilePath, translated);

                            DocsTranslator.get().getLogger().log(Level.FINE, "Finished processing source file '" + sourceFileName + "'");
                        }
                    } catch (IOException e) {
                        DocsTranslator.get().getLogger().log(Level.SEVERE, "Error when processing source file '" + path.getFileName().toString() + "'", e);
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String translateSource(Path sourceFilePath, String artifactId, String artifactVersion, String className) throws IOException {
        String fileContent = Files.readString(sourceFilePath);

        CompilationUnit cu = new JavaParser().parse(fileContent).getResult().orElseThrow();

        String packageName = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .orElse("");

        Module module = new Module(
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
