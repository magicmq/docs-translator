package dev.magicmq.docstranslator.config;


import dev.magicmq.docstranslator.config.formats.Formats;
import dev.magicmq.docstranslator.config.packaging.Packaging;

public class Settings {

    private General general;
    private SourceJars sourceJars;
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

    public SourceJars getSourceJars() {
        return sourceJars;
    }

    public void setSourceJars(SourceJars sourceJars) {
        this.sourceJars = sourceJars;
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
