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


import dev.magicmq.docstranslator.config.Repository;
import dev.magicmq.docstranslator.translate.Translator;
import org.apache.commons.io.FileUtils;
import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DocsTranslator {

    private static final Logger logger = LoggerFactory.getLogger(DocsTranslator.class);

    private final Path workingDir;

    private Path mavenPath;
    private Path javaSourcesPath;
    private Path outputFolderPath;

    public DocsTranslator(Path workingDir) {
        this.workingDir = workingDir;
    }

    public void start() throws IOException {
        logger.info("Initializing settings.yml...");

        SettingsProvider.get().initSettings(workingDir);

        logger.info("Initializing working directories...");

        initDirectories();

        logger.info("Fetching artifacts to translate...");

        List<Artifact> artifacts = fetchArtifacts();

        logger.info("Translating...");

        Translator translator = new Translator(artifacts, javaSourcesPath, outputFolderPath);
        translator.translate();

        logger.info("Generating Python package files...");

        PackagingGenerator packagingGenerator = new PackagingGenerator(outputFolderPath);
        packagingGenerator.generate();

        logger.info("Finished!");
    }

    private void initDirectories() throws IOException {
        mavenPath = workingDir.resolve(SettingsProvider.get().getSettings().getMaven().getPath()).toAbsolutePath();
        javaSourcesPath = workingDir.resolve(SettingsProvider.get().getSettings().getJdkSources().getPath()).toAbsolutePath();
        outputFolderPath = workingDir.resolve(SettingsProvider.get().getSettings().getOutput().getPath()).toAbsolutePath();

        if (SettingsProvider.get().getSettings().getMaven().isDeleteOnStart() && Files.exists(mavenPath)) {
            logger.info("Deleting local repository folder...");
            FileUtils.deleteDirectory(mavenPath.toFile());
        }
        Files.createDirectories(mavenPath);

        if (SettingsProvider.get().getSettings().getOutput().isDeleteOnStart() && Files.exists(outputFolderPath)) {
            logger.info("Deleting output folder...");
            FileUtils.deleteDirectory(outputFolderPath.toFile());
        }
        Files.createDirectories(outputFolderPath);
    }

    private List<Artifact> fetchArtifacts() {
        MavenResolver resolver = new MavenResolver(
                mavenPath.toFile(),
                SettingsProvider.get().getSettings().getMaven().isUseCentral(),
                SettingsProvider.get().getSettings().getMaven().getDependencyScope());
        for (Repository repository : SettingsProvider.get().getSettings().getMaven().getRepositories()) {
            resolver.addRemoteRepository(repository.getId(), repository.getUrl());
        }

        return resolver.fetch(SettingsProvider.get().getSettings().getMaven().getArtifacts(), SettingsProvider.get().getSettings().getMaven().getExcludeArtifacts());
    }

    public static void main(String[] args) {
        Path workingDir;
        if (args.length > 0) {
            workingDir = Path.of(args[0]);
        } else {
            workingDir = Path.of(".");
        }

        DocsTranslator translator = new DocsTranslator(workingDir);
        try {
            translator.start();
        } catch (IOException e) {
            logger.error("Error when translating", e);
        }
    }
}
