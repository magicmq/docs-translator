package dev.magicmq.docstranslator.config.packaging;


import java.util.List;

public class PyProject {

    private List<String> requires;
    private String buildBackend;

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(List<String> requires) {
        this.requires = requires;
    }

    public String getBuildBackend() {
        return buildBackend;
    }

    public void setBuildBackend(String buildBackend) {
        this.buildBackend = buildBackend;
    }
}
