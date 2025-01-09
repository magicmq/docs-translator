package dev.magicmq.docstranslator.members;

import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.DocsTranslator;
import dev.magicmq.docstranslator.module.Module;
import dev.magicmq.docstranslator.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Annotation extends Member {

    private final Module parent;

    private String name;
    private final List<AnnotationMember> members;

    public Annotation(int indent, Module parent) {
        super(indent);
        this.parent = parent;
        this.members = new ArrayList<>();
    }

    public void init(AnnotationDeclaration declaration) {
        this.name = declaration.getNameAsString();

        if (declaration.getComment().isPresent() && declaration.getComment().get() instanceof JavadocComment) {
            Javadoc javadoc = ((JavadocComment) declaration.getComment().get()).parse();
            newDocString(indent + 4).parse(javadoc);
        }

        for (BodyDeclaration<?> bodyDeclaration : declaration.getMembers()) {
            if (bodyDeclaration instanceof AnnotationMemberDeclaration method) {
                AnnotationMember member = new AnnotationMember(indent + 4, method);
                this.members.add(member);
            }
        }
    }

    @Override
    public String translate() {
        StringBuilder builder = new StringBuilder();

        String replaced = DocsTranslator.get().getSettings().getFormats().getClass_().getDeclaration().replace("%name%", name);
        String declaration = StringUtils.indent(replaced, indent);
        builder.append(declaration);

        if (docString != null) {
            builder.append("\n");
            builder.append(docString.serialize());
        }

        builder.append("\n\n");

        if (!members.isEmpty()) {
            builder.append(members.stream().map(AnnotationMember::translate).collect(Collectors.joining("\n\n\n")));
        }

        return builder.toString();
    }
}