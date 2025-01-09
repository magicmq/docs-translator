package dev.magicmq.docstranslator.doc;


import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import dev.magicmq.docstranslator.DocsTranslator;
import dev.magicmq.docstranslator.base.Indented;
import dev.magicmq.docstranslator.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocString extends Indented {

    private static final Pattern TYPE_PARAMETER_PATTERN = Pattern.compile("<([^>])>");

    private final List<String> params;
    private final List<String> serial;
    private final List<String> serialData;
    private final List<String> serialField;
    private final List<String> throwz;
    private final List<String> unknown;

    private String description;
    private String author;
    private String deprecated;
    private String returns;
    private String see;
    private String since;
    private String version;

    public DocString(int indent) {
        super(indent);
        this.params = new ArrayList<>();
        this.serial = new ArrayList<>();
        this.serialData = new ArrayList<>();
        this.serialField = new ArrayList<>();
        this.throwz = new ArrayList<>();
        this.unknown = new ArrayList<>();
    }

    public void parse(Javadoc javadoc) {
        StringBuilder builder = new StringBuilder();
        for (JavadocDescriptionElement element : javadoc.getDescription().getElements()) {
            if (element instanceof JavadocInlineTag inlineTag) {
                String content = StringUtils.replaceInlineTag(inlineTag.getContent().trim());
                if (inlineTag.getType() == JavadocInlineTag.Type.CODE)
                    builder.append("`" + content + "`");
                else
                    builder.append(content);
            } else
                builder.append(element.toText());
        }
        setDescription(StringUtils.replaceFormatting(builder.toString()));

        for (JavadocBlockTag tag : javadoc.getBlockTags()) {
            JavadocBlockTag.Type type = tag.getType();
            StringBuilder tagBuilder = new StringBuilder();
            for (JavadocDescriptionElement element : tag.getContent().getElements()) {
                if (element instanceof JavadocInlineTag inlineTag) {
                    String content = StringUtils.replaceInlineTag(inlineTag.getContent().trim());
                    if (inlineTag.getType() == JavadocInlineTag.Type.CODE)
                        tagBuilder.append("`" + content + "`");
                    else
                        tagBuilder.append(content);
                } else
                    tagBuilder.append(element.toText());
            }
            String text = StringUtils.replaceFormatting(tagBuilder.toString());

            if (type == JavadocBlockTag.Type.AUTHOR)
                setAuthor(text);
            else if (type == JavadocBlockTag.Type.DEPRECATED)
                setDeprecated(text);
            else if (type == JavadocBlockTag.Type.EXCEPTION)
                addThrows(tag.getName().orElseThrow(), text);
            else if (type == JavadocBlockTag.Type.PARAM) {
                addParam(tag.getName().orElseThrow(), text);
            } else if (type == JavadocBlockTag.Type.RETURN)
                addReturns(text);
            else if (type == JavadocBlockTag.Type.SEE)
                setSee(text.replace("#", "."));
            else if (type == JavadocBlockTag.Type.SERIAL)
                addSerial(text);
            else if (type == JavadocBlockTag.Type.SERIAL_DATA)
                addSerialData(text);
            else if (type == JavadocBlockTag.Type.SERIAL_FIELD)
                addSerialField(text);
            else if (type == JavadocBlockTag.Type.SINCE)
                setSince(text);
            else if (type == JavadocBlockTag.Type.THROWS)
                addThrows(tag.getName().orElseThrow(), text);
            else if (type == JavadocBlockTag.Type.VERSION)
                setVersion(text);
            else if (type == JavadocBlockTag.Type.UNKNOWN)
                addUnknown(text);
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addToDescription(String toAdd) {
        this.description += "\n\n";
        this.description += toAdd;
    }

    public void setAuthor(String author) {
        this.author = "- " + author;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = "- " + deprecated;
    }

    public void addParam(String param, String text) {
        Matcher matcher = TYPE_PARAMETER_PATTERN.matcher(param);
        if (matcher.find()) {
            String typeChar = matcher.group(0);
            addToDescription(DocsTranslator.get().getSettings().getFormats().getDocString().getTypeParam()
                    .replace("%type%", typeChar)
                    .replace("%text%", text));
            return;
        }

        this.params.add(DocsTranslator.get().getSettings().getFormats().getDocString().getParam()
                .replace("%param%", param)
                .replace("%text%", text));
    }

    public void addReturns(String returns) {
        this.returns = "- " + returns;
    }

    public void setSee(String see) {
        this.see = "- " + see;
    }

    public void addSerial(String serial) {
        this.serial.add("- " + serial);
    }

    public void addSerialData(String serialData) {
        this.serialData.add("- " + serialData);
    }

    public void addSerialField(String serialField) {
        this.serialField.add("- " + serialField);
    }

    public void setSince(String since) {
        this.since = "- " + since;
    }

    public void addThrows(String throwz, String text) {
        this.throwz.add(DocsTranslator.get().getSettings().getFormats().getDocString().getThrow()
                .replace("%throw%", throwz)
                .replace("%text%", text));
    }

    public void setVersion(String version) {
        this.version = "- " + version;
    }

    public void addUnknown(String unknown) {
        this.unknown.add("- " + unknown);
    }

    public String serialize() {
        StringBuilder builder = new StringBuilder();

        builder.append(StringUtils.indent("\"\"\"", indent));
        builder.append("\n");

        boolean contentAdded = false;

        if (description != null && !description.isEmpty()) {
            builder.append(StringUtils.indent(description, indent));
            contentAdded = true;
        }

        if (author != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getAuthor(), 4));
            builder.append("\n");
            builder.append(StringUtils.indent(author, indent));
            contentAdded = true;
        }

        if (version != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getVersion(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(version, indent));
            contentAdded = true;
        }

        if (!params.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getParams(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(String.join("\n", params), indent));
            contentAdded = true;
        }

        if (returns != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getReturns(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(returns, indent));
            contentAdded = true;
        }

        if (!throwz.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getThrows(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(String.join("\n", throwz), indent));
            contentAdded = true;
        }

        if (see != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getSee(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(see, indent));
            contentAdded = true;
        }

        if (since != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getSince(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(since, indent));
            contentAdded = true;
        }

        if (!serial.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getSerial(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(String.join("\n", serial), indent));
            contentAdded = true;
        }

        if (!serialField.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getSerialField(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(String.join("\n", serialField), indent));
            contentAdded = true;
        }

        if (!serialData.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getSerialData(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(String.join("\n", serialData), indent));
            contentAdded = true;
        }

        if (deprecated != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getDeprecated(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(String.join("\n", deprecated), indent));
            contentAdded = true;
        }

        if (!unknown.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(DocsTranslator.get().getSettings().getFormats().getDocString().getUnknown(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(String.join("\n", unknown), indent));
        }

        builder.append("\n");
        builder.append(StringUtils.indent("\"\"\"", indent));

        return builder.toString();
    }
}
