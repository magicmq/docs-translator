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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DocsTranslator {

    private static final Logger logger = LoggerFactory.getLogger(DocsTranslator.class);

    private final Path workingDir;

    public DocsTranslator(Path workingDir) {
        this.workingDir = workingDir;
    }

    public void start() {
        MDC.put("job", "main");

        try {
            logger.info("Initializing settings.yml...");

            try {
                SettingsProvider.get().initSettings(workingDir);
            } catch (IOException e) {
                logger.error("Error while initializing settings.yml", e);
                return;
            }

            logger.info("Initializing Apache Maven Resolver...");

            MavenResolver maven;
            try {
                maven = new MavenResolver(workingDir);
            } catch (IOException e) {
                logger.error("Error when initializing Maven Resolver", e);
                return;
            }

            logger.info("Initializing and starting translate jobs...");

            ExecutorService executor = Executors.newFixedThreadPool(SettingsProvider.get().getSettings().getBatching().getThreads());

            for (TranslateJob item : SettingsProvider.get().getSettings().getTranslateJobs()) {
                logger.info("Starting translate job '{}' version '{}'", item.getPyPIName(), item.getPyPIVersion());
                executor.submit(new dev.magicmq.docstranslator.TranslateJob(
                        workingDir,
                        item.getPyPIName(),
                        item.getPyPIVersion(),
                        item.getArtifacts(),
                        item.getPyModules(),
                        maven
                ));
            }

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Translate jobs were interrupted before they could finish");
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            logger.info("All translate jobs have finished!");
        } finally {
            MDC.clear();
        }
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
