package dev.magicmq.docstranslator.members;


import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.DocsTranslator;
import dev.magicmq.docstranslator.utils.StringUtils;
import dev.magicmq.docstranslator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class Field extends Member {

    private final FieldDeclaration fieldDeclaration;

    public Field(int indent, FieldDeclaration fieldDeclaration) {
        super(indent);
        this.fieldDeclaration = fieldDeclaration;
    }

    @Override
    public String translate() {
        List<String> variables = new ArrayList<>();
        for (VariableDeclarator declarator : fieldDeclaration.getVariables()) {
            String fieldName = declarator.getNameAsString();
            Expression initializer = declarator.getInitializer().orElse(null);
            String fieldValue = initializer != null ? TypeUtils.convertValue(initializer.toString()) : "None";
            String replaced = DocsTranslator.get().getSettings().getFormats().getField().getInitializer()
                    .replace("%name%", fieldName)
                    .replace("%value%", fieldValue);

            variables.add(StringUtils.indent(replaced, indent));
        }
        if (fieldDeclaration.getComment().isPresent() && fieldDeclaration.getComment().get() instanceof JavadocComment) {
            Javadoc javadoc = ((JavadocComment) fieldDeclaration.getComment().get()).parse();
            newDocString().parse(javadoc);
        }

        StringBuilder builder = new StringBuilder();

        builder.append(String.join("\n", variables));

        if (docString != null) {
            builder.append("\n");
            builder.append(docString.serialize());
        }

        return builder.toString();
    }

}
