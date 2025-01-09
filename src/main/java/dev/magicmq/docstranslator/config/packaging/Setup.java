package dev.magicmq.docstranslator.config.packaging;


import java.util.List;

public class Setup {

    private String name;
    private String version;
    private String author;
    private String authorEmail;
    private String description;
    private String url;
    private List<String> pyModules;
    private String pythonRequires;
    private List<String> classifiers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getPyModules() {
        return pyModules;
    }

    public void setPyModules(List<String> pyModules) {
        this.pyModules = pyModules;
    }

    public String getPythonRequires() {
        return pythonRequires;
    }

    public void setPythonRequires(String pythonRequires) {
        this.pythonRequires = pythonRequires;
    }

    public List<String> getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(List<String> classifiers) {
        this.classifiers = classifiers;
    }
}
