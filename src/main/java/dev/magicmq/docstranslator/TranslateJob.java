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


import dev.magicmq.docstranslator.translate.ArtifactTranslator;
import org.apache.commons.io.FileUtils;
import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TranslateJob implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TranslateJob.class);

    private final Path workingDir;
    private final Path javaSourcesDir;
    private final String pyPIName;
    private final String pyPIVersion;
    private final List<String> artifacts;
    private final List<String> pyModules;
    private MavenResolver maven;

    private Path outputFolderPath;

    public TranslateJob(Path workingDir, String pyPIName, String pyPIVersion, List<String> artifacts, List<String> pyModules, MavenResolver maven) {
        this.workingDir = workingDir;
        this.javaSourcesDir = workingDir.resolve(SettingsProvider.get().getSettings().getJdkSources().getPath()).toAbsolutePath();
        this.pyPIName = pyPIName;
        this.pyPIVersion = pyPIVersion;
        this.artifacts = artifacts;
        this.pyModules = pyModules;
        this.maven = maven;
    }

    @Override
    public void run() {
        MDC.put("job", this.pyPIVersion);

        try {
            logger.info("Initializing output folder...");

            try {
                initOutputFolder();
            } catch (IOException e) {
                logger.error("Failed to initialize output folder", e);
                return;
            }

            logger.info("Fetching and translating artifacts...");

            ArtifactTranslator artifactTranslator = new ArtifactTranslator(javaSourcesDir, outputFolderPath);

            for (String artifactString : artifacts) {
                try {
                    List<Artifact> artifacts = maven.fetch(this.pyPIVersion, artifactString).get();
                    artifactTranslator.translate(artifacts);
                } catch (InterruptedException | ExecutionException e) {
                        logger.error("Error when fetching artifact '{}' and/or its dependencies from Maven,", artifactString, e);
                        return;
                    }
            }

            logger.info("Performing post-translate tasks...");

            artifactTranslator.doPostTranslateTasks();

            logger.info("Generating Python package files...");

            try {
                PackagingGenerator packagingGenerator = new PackagingGenerator(outputFolderPath, pyPIName, pyPIVersion, pyModules);
                packagingGenerator.generate();
            } catch (IOException e) {
                logger.error("Failed to generate Python package files", e);
                return;
            }

            logger.info("Finished translate job '{}' version '{}'!", pyPIName, pyPIVersion);

        } finally {
            MDC.clear();
        }
    }

    private void initOutputFolder() throws IOException {
        outputFolderPath = workingDir
                .resolve(SettingsProvider.get().getSettings().getOutput().getPath())
                .resolve(pyPIName + "/")
                .resolve(pyPIVersion + "/")
                .toAbsolutePath();

        if (SettingsProvider.get().getSettings().getOutput().isDeleteOnStart() && Files.exists(outputFolderPath)) {
            logger.info("Deleting output folder...");
            FileUtils.deleteDirectory(outputFolderPath.toFile());
        }
        Files.createDirectories(outputFolderPath);
    }
}
