package dev.magicmq.docstranslator.config;


public class Output {

    private String path;
    private boolean deleteOnStart;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDeleteOnStart() {
        return deleteOnStart;
    }

    public void setDeleteOnStart(boolean deleteOnStart) {
        this.deleteOnStart = deleteOnStart;
    }
}
