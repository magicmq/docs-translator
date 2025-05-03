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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackagingGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PackagingGenerator.class);
    private static final Pattern classifierPattern = Pattern.compile("( *)%classifiers%");

    private final Path outputDir;

    public PackagingGenerator(Path outputDir) {
        this.outputDir = outputDir;
    }

    public void generate() throws IOException {
        generateSetupPy();
        generatePyProject();
        generateManifest();
        saveLicense();
    }

    private void generateSetupPy() throws IOException {
        String setup = SettingsProvider.get().getSettings().getFormats().getPackaging().getSetup();

        List<String> pyModules;
        if (SettingsProvider.get().getSettings().getPackaging().getSetup().getPyModules() != null) {
            pyModules = SettingsProvider.get().getSettings().getPackaging().getSetup().getPyModules();
        } else {
            pyModules = Collections.emptyList();
        }

        List<String> pyModuleNames = new ArrayList<>();
        for (String pyModule : pyModules) {
            logger.debug("Downloading python module at URL {}", pyModule);
            try {
                Utils.downloadResource(pyModule, outputDir);

                String fileName = pyModule.substring(pyModule.lastIndexOf("/") + 1);
                String moduleName = fileName.substring(0, fileName.length() - 3);
                pyModuleNames.add("'" + moduleName + "'");
            } catch (IOException e) {
                logger.error("Error when downloading python module at URL {}", pyModule, e);
            }
        }

        Matcher matcher = classifierPattern.matcher(setup);
        String spaces = matcher.find() ? matcher.group(1) : "";
        List<String> classifiers = SettingsProvider.get().getSettings().getPackaging().getSetup().getClassifiers()
                .stream()
                .map(string -> "'" + string + "'")
                .toList();

        setup = setup
                .replace("%name%", SettingsProvider.get().getSettings().getPackaging().getSetup().getName())
                .replace("%version%", SettingsProvider.get().getSettings().getPackaging().getSetup().getVersion())
                .replace("%author%", SettingsProvider.get().getSettings().getPackaging().getSetup().getAuthor())
                .replace("%author_email%", SettingsProvider.get().getSettings().getPackaging().getSetup().getAuthorEmail())
                .replace("%description%", SettingsProvider.get().getSettings().getPackaging().getSetup().getDescription())
                .replace("%url%", SettingsProvider.get().getSettings().getPackaging().getSetup().getUrl())
                .replace("%py_modules%", String.join(", ", pyModuleNames))
                .replace("%python_requires%", SettingsProvider.get().getSettings().getPackaging().getSetup().getPythonRequires())
                .replace("%classifiers%", String.join(",\n" + spaces, classifiers));

        Path setupPyPath = outputDir.resolve("setup.py");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(setupPyPath.toFile()))) {
            writer.write(setup);
        }
    }

    private void generatePyProject() throws IOException {
        List<String> requires = SettingsProvider.get().getSettings().getPackaging().getPyProject().getRequires()
                .stream()
                .map(string -> "\"" + string + "\"")
                .toList();

        String pyProject = SettingsProvider.get().getSettings().getFormats().getPackaging().getPyProject();
        pyProject = pyProject
                .replace("%requires%", String.join(", ", requires))
                .replace("%build_backend%", SettingsProvider.get().getSettings().getPackaging().getPyProject().getBuildBackend());

        Path pyProjectPath = outputDir.resolve("pyproject.toml");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pyProjectPath.toFile()))) {
            writer.write(pyProject);
        }
    }

    private void generateManifest() throws IOException {
        String manifest = String.join("\n", SettingsProvider.get().getSettings().getPackaging().getManifest());

        Path manifestPath = outputDir.resolve("MANIFEST.in");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(manifestPath.toFile()))) {
            writer.write(manifest);
        }
    }

    private void saveLicense() throws IOException {
        Path savePath = outputDir.resolve("LICENSE");

        try (InputStream inputStream = new BufferedInputStream(new URL(SettingsProvider.get().getSettings().getPackaging().getLicense()).openStream());
             FileOutputStream out = new FileOutputStream(savePath.toFile())) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
