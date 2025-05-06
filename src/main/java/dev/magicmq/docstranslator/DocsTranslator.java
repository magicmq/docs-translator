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


import dev.magicmq.docstranslator.config.TranslateJob;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DocsTranslator {

    private static final Logger logger = LoggerFactory.getLogger(DocsTranslator.class);

    private final Path workingDir;

    private Path mavenDir;
    private Path javaSourcesDir;

    public DocsTranslator(Path workingDir) {
        this.workingDir = workingDir;
    }

    public void start() {
        MDC.put("job", "master");

        try {
            logger.info("Initializing settings.yml...");

            try {
                SettingsProvider.get().initSettings(workingDir);
            } catch (IOException e) {
                logger.error("Error while initializing settings.yml", e);
                return;
            }

            logger.info("Initializing maven and JDK sources directories...");

            try {
                initDirectories();
            } catch (IOException e) {
                logger.error("Error when initializing maven and/or JDK sources directories", e);
                return;
            }

            logger.info("Initializing translate jobs...");

            List<dev.magicmq.docstranslator.TranslateJob> jobs = new ArrayList<>();
            for (TranslateJob item : SettingsProvider.get().getSettings().getTranslateJobs()) {
                jobs.add(new dev.magicmq.docstranslator.TranslateJob(
                        workingDir,
                        mavenDir,
                        javaSourcesDir,
                        item.getPyPIName(),
                        item.getPyPIVersion(),
                        item.getArtifacts(),
                        item.getPyModules()
                ));
            }

            logger.info("Starting translate jobs...");

            ExecutorService executor = Executors.newFixedThreadPool(SettingsProvider.get().getSettings().getBatching().getThreads());
            jobs.forEach(executor::submit);

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Translate jobs were interrupted before they could finish");
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            logger.info("Finished all translate jobs!");
        } finally {
            MDC.clear();
        }
    }

    private void initDirectories() throws IOException {
        mavenDir = workingDir.resolve(SettingsProvider.get().getSettings().getMaven().getPath()).toAbsolutePath();
        javaSourcesDir = workingDir.resolve(SettingsProvider.get().getSettings().getJdkSources().getPath()).toAbsolutePath();

        if (SettingsProvider.get().getSettings().getMaven().isDeleteOnStart() && Files.exists(mavenDir)) {
            logger.info("Deleting local repository folder...");
            FileUtils.deleteDirectory(mavenDir.toFile());
        }
        Files.createDirectories(mavenDir);
    }

    public static void main(String[] args) {
        Path workingDir;
        if (args.length > 0) {
            workingDir = Path.of(args[0]);
        } else {
            workingDir = Path.of(".");
        }

        DocsTranslator translator = new DocsTranslator(workingDir);
        translator.start();
    }
}
