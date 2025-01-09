package dev.magicmq.docstranslator.module;


import dev.magicmq.docstranslator.DocsTranslator;

public record Import(String packageName, String className) implements Comparable<Import> {

    @Override
    public int compareTo(Import o) {
        return (this.packageName + "." + this.className).compareTo(o.packageName + "." + o.className);
    }

    @Override
    public String toString() {
        return DocsTranslator.get().getSettings().getFormats().getModule().getImportDeclaration()
                .replace("%package%", this.packageName)
                .replace("%module%", this.className);
    }
}
