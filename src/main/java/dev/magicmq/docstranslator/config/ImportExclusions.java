package dev.magicmq.docstranslator.config;


import java.util.List;

public class ImportExclusions {

    private List<String> packages;
    private List<String> classes;

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }
}
