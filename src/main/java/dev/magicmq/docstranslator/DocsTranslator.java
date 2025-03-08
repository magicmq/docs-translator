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


import dev.magicmq.docstranslator.config.Settings;
import dev.magicmq.docstranslator.utils.logging.CustomFormatter;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Stream;

public class DocsTranslator {

    private static DocsTranslator instance;

    private Logger logger;
    private Settings settings;

    private Path jarsFolderPath;
    private Path javaSourcesPath;
    private Path outputFolderPath;

    private DocsTranslator() {
        initSettings();
        initLogger();
    }

    public void start() throws IOException {
        logger.log(Level.INFO, "Initializing working directories...");

        initDirectories();

        if (settings.getSourceJars().isDownload()) {
            logger.log(Level.INFO, "Downloading JARs...");

            downloadJars();
        }

        List<Path> jarFilePaths = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(jarsFolderPath)) {
            paths.forEach(path -> {
                if (Files.isRegularFile(path))
                    jarFilePaths.add(path);
            });
        }

        logger.log(Level.INFO, "Translating sources...");

        Translator translator = new Translator(jarFilePaths, javaSourcesPath, outputFolderPath);
        try {
            translator.translate();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error when translating", e);
            e.printStackTrace();
        }

        logger.log(Level.INFO, "Generating Python package files...");

        PackagingGenerator packagingGenerator = new PackagingGenerator(settings, outputFolderPath);
        packagingGenerator.generate();

        logger.log(Level.INFO, "Finished.");
    }

    public Logger getLogger() {
        return logger;
    }

    public Settings getSettings() {
        return settings;
    }

    private void initSettings() {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = new FileInputStream("settings.yml")) {
            this.settings = yaml.loadAs(inputStream, Settings.class);
        } catch (IOException e) {
            try (InputStream inputStream = DocsTranslator.class.getClassLoader().getResourceAsStream("settings.yml")) {
                this.settings = yaml.loadAs(inputStream, Settings.class);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void initLogger() {
        this.logger = Logger.getLogger(DocsTranslator.class.getSimpleName());

        this.logger.setLevel(Level.parse(settings.getGeneral().getLoggingLevel()));

        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new CustomFormatter());
        logger.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler("output.log", true);
            fileHandler.setFormatter(new CustomFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error when initializing FileHandler for logger", e);
            e.printStackTrace();
        }
    }

    private void initDirectories() throws IOException {
        jarsFolderPath = Path.of(settings.getSourceJars().getPath()).toAbsolutePath();
        javaSourcesPath = Path.of(settings.getJdkSources().getPath()).toAbsolutePath();
        outputFolderPath = Path.of(settings.getOutput().getPath()).toAbsolutePath();

        if (settings.getSourceJars().isDeleteOnStart())
            FileUtils.deleteDirectory(jarsFolderPath.toFile());
        Files.createDirectories(jarsFolderPath);

        if (settings.getOutput().isDeleteOnStart())
            FileUtils.deleteDirectory(outputFolderPath.toFile());
        Files.createDirectories(outputFolderPath);
    }

    private void downloadJars() throws IOException {
        for (String url : settings.getSourceJars().getUrls()) {
            Utils.downloadResource(url, jarsFolderPath);
        }
    }

    public static DocsTranslator get() {
        if (instance == null)
            instance = new DocsTranslator();
        return instance;
    }
}
