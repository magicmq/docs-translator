package dev.magicmq.docstranslator;


import dev.magicmq.docstranslator.config.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsProvider {

    private static final Logger logger = LoggerFactory.getLogger(SettingsProvider.class);
    private static final String SETTINGS_FILE_NAME = "settings.yml";

    private static SettingsProvider instance;

    private Settings settings;

    private SettingsProvider() {}

    public Settings getSettings() {
        return settings;
    }

    public void initSettings(Path workingDir) throws IOException {
        Path settingsPath = workingDir.resolve(SETTINGS_FILE_NAME);
        if (Files.notExists(settingsPath))
            saveDefaultSettings(settingsPath);

        try (InputStream input = Files.newInputStream(settingsPath)) {
            Yaml yaml = new Yaml();
            Settings settings = yaml.loadAs(input, Settings.class);
            if (settings == null)
                throw new IOException("Loaded settings.yml is null");
            this.settings = settings;
        }
    }

    private void saveDefaultSettings(Path target) throws IOException {
        URL resource = getClass().getClassLoader().getResource(SETTINGS_FILE_NAME);
        if (resource == null)
            throw new FileNotFoundException("Default settings.yml not found in JAR");

        try (InputStream input = resource.openStream();
            OutputStream output = Files.newOutputStream(target)) {
            input.transferTo(output);
        }

        logger.info("Default settings.yml saved to {}", target);
    }

    public static SettingsProvider get() {
        if (instance == null)
            instance = new SettingsProvider();
        return instance;
    }
}
