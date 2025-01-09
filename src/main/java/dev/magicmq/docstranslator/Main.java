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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class Main {

    public static void main(String[] args) {
        DocsTranslator translator = DocsTranslator.get();

        File file = new File("settings.yml");
        if (!file.exists()) {
            saveSettings();
        }

        try {
            translator.start();
        } catch (IOException e) {
            translator.getLogger().log(Level.SEVERE, "Error when translating", e);
            e.printStackTrace();
        }
    }

    private static void saveSettings() {
        InputStream in = getResource("settings.yml");

        if (in != null) {
            try {
                OutputStream out = new FileOutputStream("settings.yml");
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static InputStream getResource(String fileName) {
        try {
            URL url = Main.class.getClassLoader().getResource(fileName);

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
