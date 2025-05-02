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
package dev.magicmq.docstranslator.config;


import dev.magicmq.docstranslator.config.formats.Formats;
import dev.magicmq.docstranslator.config.packaging.Packaging;

public class Settings {

    private General general;
    private Maven maven;
    private JdkSources jdkSources;
    private Output output;
    private ImportExclusions importExclusions;
    private Formats formats;
    private Packaging packaging;

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public Maven getMaven() {
        return maven;
    }

    public void setMaven(Maven maven) {
        this.maven = maven;
    }

    public JdkSources getJdkSources() {
        return jdkSources;
    }

    public void setJdkSources(JdkSources jdkSources) {
        this.jdkSources = jdkSources;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public ImportExclusions getImportExclusions() {
        return importExclusions;
    }

    public void setImportExclusions(ImportExclusions importExclusions) {
        this.importExclusions = importExclusions;
    }

    public Formats getFormats() {
        return formats;
    }

    public void setFormats(Formats formats) {
        this.formats = formats;
    }

    public Packaging getPackaging() {
        return packaging;
    }

    public void setPackaging(Packaging packaging) {
        this.packaging = packaging;
    }
}
