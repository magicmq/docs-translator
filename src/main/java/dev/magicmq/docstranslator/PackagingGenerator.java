package dev.magicmq.docstranslator;


import dev.magicmq.docstranslator.config.Settings;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackagingGenerator {

    private static final Pattern classifierPattern = Pattern.compile("( *)%classifiers%");

    private final Settings settings;
    private final Path outputDir;

    public PackagingGenerator(Settings settings, Path outputDir) {
        this.settings = settings;
        this.outputDir = outputDir;
    }

    public void generate() throws IOException {
        generateSetupPy();
        generatePyProject();
        generateManifest();
        saveLicense();
    }

    private void generateSetupPy() throws IOException {
        String setup = settings.getFormats().getPackaging().getSetup();

        List<String> pyModules = settings.getPackaging().getSetup().getPyModules()
                .stream()
                .map(string -> "'" + string + "'")
                .toList();

        Matcher matcher = classifierPattern.matcher(setup);
        String spaces = matcher.find() ? matcher.group(1) : "";
        List<String> classifiers = settings.getPackaging().getSetup().getClassifiers()
                .stream()
                .map(string -> "'" + string + "'")
                .toList();

        setup = setup
                .replace("%name%", settings.getPackaging().getSetup().getName())
                .replace("%version%", settings.getPackaging().getSetup().getVersion())
                .replace("%author%", settings.getPackaging().getSetup().getAuthor())
                .replace("%author_email%", settings.getPackaging().getSetup().getAuthorEmail())
                .replace("%description%", settings.getPackaging().getSetup().getDescription())
                .replace("%url%", settings.getPackaging().getSetup().getUrl())
                .replace("%py_modules%", String.join(", ", pyModules))
                .replace("%python_requires%", settings.getPackaging().getSetup().getPythonRequires())
                .replace("%classifiers%", String.join(",\n" + spaces, classifiers));

        Path setupPyPath = outputDir.resolve("setup.py");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(setupPyPath.toFile()))) {
            writer.write(setup);
        }
    }

    private void generatePyProject() throws IOException {
        List<String> requires = settings.getPackaging().getPyProject().getRequires()
                .stream()
                .map(string -> "\"" + string + "\"")
                .toList();

        String pyProject = settings.getFormats().getPackaging().getPyProject();
        pyProject = pyProject
                .replace("%requires%", String.join(", ", requires))
                .replace("%build_backend%", settings.getPackaging().getPyProject().getBuildBackend());

        Path pyProjectPath = outputDir.resolve("pyproject.toml");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pyProjectPath.toFile()))) {
            writer.write(pyProject);
        }
    }

    private void generateManifest() throws IOException {
        String manifest = String.join("\n", settings.getPackaging().getManifest());

        Path manifestPath = outputDir.resolve("MANIFEST.in");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(manifestPath.toFile()))) {
            writer.write(manifest);
        }
    }

    private void saveLicense() throws IOException {
        Path savePath = outputDir.resolve("LICENSE");

        try (InputStream inputStream = new BufferedInputStream(new URL(settings.getPackaging().getLicense()).openStream());
             FileOutputStream out = new FileOutputStream(savePath.toFile())) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}