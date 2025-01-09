package dev.magicmq.docstranslator.members;


import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.DocsTranslator;
import dev.magicmq.docstranslator.doc.DocString;
import dev.magicmq.docstranslator.utils.StringUtils;
import dev.magicmq.docstranslator.utils.TypeUtils;

public class AnnotationMember extends Member {

    private final AnnotationMemberDeclaration memberDeclaration;

    public AnnotationMember(int indent, AnnotationMemberDeclaration memberDeclaration) {
        super(indent);
        this.memberDeclaration = memberDeclaration;
    }

    @Override
    public String translate() {
        String methodName = memberDeclaration.getNameAsString();
        String returnType = TypeUtils.convertType(memberDeclaration.getType());
        if (memberDeclaration.getComment().isPresent() && memberDeclaration.getComment().get() instanceof JavadocComment) {
            Javadoc javadoc = ((JavadocComment) memberDeclaration.getComment().get()).parse();
            DocString docString = newDocString(indent + 4);
            docString.parse(javadoc);
        }
        String defaultReturn = null;
        if (memberDeclaration.getDefaultValue().isPresent()) {
            defaultReturn = TypeUtils.convertValue(memberDeclaration.getDefaultValue().get().toString());
        }

        StringBuilder builder = new StringBuilder();

        String def = DocsTranslator.get().getSettings().getFormats().getFunction().getDefinition()
                .replace("%name%", methodName)
                .replace("%params%", "self")
                .replace("%returns%", returnType);
        builder.append(StringUtils.indent(def, indent));

        builder.append("\n");

        if (docString != null) {
            builder.append(docString.serialize());
            builder.append("\n");
        }

        if (defaultReturn != null)
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getFunction().getReturnWithValue().replace("%value%", defaultReturn), indent + 4));
        else
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getFunction().getReturnRegular(), indent + 4));

        return builder.toString();
    }
}
