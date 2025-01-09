package dev.magicmq.docstranslator.config;


import java.util.List;

public class SourceJars {

    private String path;
    private boolean deleteOnStart;
    private boolean download;
    private List<String> urls;

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

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
