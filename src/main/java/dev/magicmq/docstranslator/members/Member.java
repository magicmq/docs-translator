package dev.magicmq.docstranslator.members;


import dev.magicmq.docstranslator.doc.DocString;
import dev.magicmq.docstranslator.base.Indented;
import dev.magicmq.docstranslator.base.Translatable;

public abstract class Member extends Indented implements Translatable {

    protected DocString docString;

    public Member(int indent) {
        super(indent);
    }

    public DocString newDocString() {
        this.docString = new DocString(indent);
        return docString;
    }

    public DocString newDocString(int indent) {
        this.docString = new DocString(indent);
        return docString;
    }
}
