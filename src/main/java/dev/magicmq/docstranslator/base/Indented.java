package dev.magicmq.docstranslator.base;


public abstract class Indented {

    protected final int indent;

    public Indented(int indent) {
        this.indent = indent;
    }

    public int getIndent() {
        return indent;
    }

}
