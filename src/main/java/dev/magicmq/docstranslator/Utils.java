package dev.magicmq.docstranslator;


import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public final class Utils {

    private Utils() {}

    public static void downloadResource(String url, Path outputDir) throws IOException {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        Path savePath = outputDir.resolve(fileName);

        try (InputStream inputStream = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream out = new FileOutputStream(savePath.toFile())) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
