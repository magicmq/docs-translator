package dev.magicmq.docstranslator.members;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.DocsTranslator;
import dev.magicmq.docstranslator.doc.DocString;
import dev.magicmq.docstranslator.utils.StringUtils;
import dev.magicmq.docstranslator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class Function extends Member {

    private final MethodDeclaration methodDeclaration;

    public Function(int indent, MethodDeclaration methodDeclaration) {
        super(indent);
        this.methodDeclaration = methodDeclaration;
    }

    @Override
    public String translate() {
        String methodName = methodDeclaration.getNameAsString();
        String returnType = TypeUtils.convertType(methodDeclaration.getType());
        List<String> parameters = new ArrayList<>();
        if (!methodDeclaration.isStatic())
            parameters.add("self");
        methodDeclaration.getParameters().forEach(param -> {
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
        if (methodDeclaration.getComment().isPresent() && methodDeclaration.getComment().get() instanceof JavadocComment) {
            Javadoc javadoc = ((JavadocComment) methodDeclaration.getComment().get()).parse();
            DocString docString = newDocString(indent + 4);
            docString.parse(javadoc);
        }

        StringBuilder builder = new StringBuilder();

        if (methodDeclaration.isStatic()) {
            builder.append(StringUtils.indent("@staticmethod", indent));
            builder.append("\n");
        }

        String params = String.join(", ", parameters);
        String def = DocsTranslator.get().getSettings().getFormats().getFunction().getDefinition()
                .replace("%name%", methodName)
                .replace("%params%", params)
                .replace("%returns%", returnType);
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
