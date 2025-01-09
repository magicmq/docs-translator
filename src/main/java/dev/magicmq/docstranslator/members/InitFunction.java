package dev.magicmq.docstranslator.members;


import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.DocsTranslator;
import dev.magicmq.docstranslator.doc.DocString;
import dev.magicmq.docstranslator.utils.StringUtils;
import dev.magicmq.docstranslator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class InitFunction extends Member {

    private final ConstructorDeclaration constructorDeclaration;

    public InitFunction(int indent, ConstructorDeclaration constructorDeclaration) {
        super(indent);
        this.constructorDeclaration = constructorDeclaration;
    }

    @Override
    public String translate() {
        List<String> parameters = new ArrayList<>();
        parameters.add("self");
        constructorDeclaration.getParameters().forEach(param -> {
            if (param.isVarArgs()) {
                parameters.add(DocsTranslator.get().getSettings().getFormats().getFunction().getParameterVararg()
                        .replace("%name%", param.getNameAsString())
                        .replace("%type%", TypeUtils.convertType(param.getType())));
            } else {
                parameters.add(DocsTranslator.get().getSettings().getFormats().getFunction().getParameterRegular()
                        .replace("%name%", param.getNameAsString())
                        .replace("%type%", TypeUtils.convertType(param.getType())));
            }
        });
        if (constructorDeclaration.getComment().isPresent() && constructorDeclaration.getComment().get() instanceof JavadocComment) {
            Javadoc javadoc = ((JavadocComment) constructorDeclaration.getComment().get()).parse();
            DocString docString = newDocString(indent + 4);
            docString.parse(javadoc);
        }

        StringBuilder builder = new StringBuilder();

        String params = String.join(", ", parameters);
        String def = DocsTranslator.get().getSettings().getFormats().getFunction().getInitDefinition().replace("%params%", params);
        builder.append(StringUtils.indent(def, indent));

        builder.append("\n");

        if (docString != null) {
            builder.append(docString.serialize());
            builder.append("\n");
        }

        builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getFunction().getReturnRegular(), indent + 4));

        return builder.toString();
    }
}
