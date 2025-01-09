package dev.magicmq.docstranslator.members;


import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.DocsTranslator;
import dev.magicmq.docstranslator.utils.StringUtils;
import dev.magicmq.docstranslator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class EnumEntry extends Member {

    private final EnumConstantDeclaration enumConstantDeclaration;
    private final int value;

    public EnumEntry(int indent, int value, EnumConstantDeclaration enumConstantDeclaration) {
        super(indent);
        this.enumConstantDeclaration = enumConstantDeclaration;
        this.value = value;
    }

    @Override
    public String translate() {
        List<String> arguments = new ArrayList<>();
        for (Expression argument : enumConstantDeclaration.getArguments()) {
            arguments.add(TypeUtils.convertValue(argument.toString()));
        }

        if (enumConstantDeclaration.getComment().isPresent() && enumConstantDeclaration.getComment().get() instanceof JavadocComment) {
            Javadoc javadoc = ((JavadocComment) enumConstantDeclaration.getComment().get()).parse();
            newDocString().parse(javadoc);
        }

        StringBuilder builder = new StringBuilder();

        String replaced;
        if (!arguments.isEmpty()) {
            replaced = DocsTranslator.get().getSettings().getFormats().getEnum().getEntryWithArgs()
                    .replace("%name%", enumConstantDeclaration.getNameAsString())
                    .replace("%args%", String.join(", ", arguments));
        } else {
            replaced = DocsTranslator.get().getSettings().getFormats().getEnum().getEntryRegular()
                    .replace("%name%", enumConstantDeclaration.getNameAsString())
                    .replace("%num%", "" + value);
        }
        builder.append(StringUtils.indent(replaced, indent));

        if (docString != null) {
            builder.append("\n");
            builder.append(docString.serialize());
        }

        return builder.toString();

    }

}