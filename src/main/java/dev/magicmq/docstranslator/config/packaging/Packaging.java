package dev.magicmq.docstranslator.config.packaging;


import java.util.List;

public class Packaging {

    private Setup setup;
    private PyProject pyProject;
    private List<String> manifest;
    private String license;

    public Setup getSetup() {
        return setup;
    }

    public void setSetup(Setup setup) {
        this.setup = setup;
    }

    public PyProject getPyProject() {
        return pyProject;
    }

    public void setPyProject(PyProject pyProject) {
        this.pyProject = pyProject;
    }

    public List<String> getManifest() {
        return manifest;
    }

    public void setManifest(List<String> manifest) {
        this.manifest = manifest;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
