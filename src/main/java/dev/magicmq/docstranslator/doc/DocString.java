/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.docstranslator.doc;


import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import dev.magicmq.docstranslator.SettingsProvider;
import dev.magicmq.docstranslator.base.Indented;
import dev.magicmq.docstranslator.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocString extends Indented {

    private static final Logger logger = LoggerFactory.getLogger(DocString.class);
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
                if (inlineTag.getType() == JavadocInlineTag.Type.CODE || inlineTag.getType() == JavadocInlineTag.Type.LINK)
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
                    if (inlineTag.getType() == JavadocInlineTag.Type.CODE || inlineTag.getType() == JavadocInlineTag.Type.LINK)
                        tagBuilder.append("`" + content + "`");
                    else
                        tagBuilder.append(content);
                } else
                    tagBuilder.append(element.toText());
            }
            String text = StringUtils.replaceFormatting(tagBuilder.toString());

            if (type == JavadocBlockTag.Type.AUTHOR) {
                setAuthor(text);
            } else if (type == JavadocBlockTag.Type.DEPRECATED) {
                setDeprecated(text);
            } else if (type == JavadocBlockTag.Type.EXCEPTION) {
                try {
                    addThrows(tag.getName().orElseThrow(), text);
                } catch (NoSuchElementException e) {
                    logger.warn("Error when parsing JavaDocs @exception tag '{}'. Unable to get exception", tag.toText());
                }
            } else if (type == JavadocBlockTag.Type.PARAM) {
                try {
                    addParam(tag.getName().orElseThrow(), text);
                } catch (NoSuchElementException e) {
                    logger.warn("Error when parsing JavaDocs @param tag '{}'. Unable to get parameter", tag.toText());
                }
            } else if (type == JavadocBlockTag.Type.RETURN)
                addReturns(text);
            else if (type == JavadocBlockTag.Type.SEE) {
                setSee(text.replace("#", "."));
            } else if (type == JavadocBlockTag.Type.SERIAL) {
                addSerial(text);
            } else if (type == JavadocBlockTag.Type.SERIAL_DATA) {
                addSerialData(text);
            } else if (type == JavadocBlockTag.Type.SERIAL_FIELD) {
                addSerialField(text);
            } else if (type == JavadocBlockTag.Type.SINCE) {
                setSince(text);
            } else if (type == JavadocBlockTag.Type.THROWS) {
                try {
                    addThrows(tag.getName().orElseThrow(), text);
                } catch (NoSuchElementException e) {
                    logger.warn("Error when parsing JavaDocs @throws tag '{}'. Unable to get exception", tag.toText());
                }
            } else if (type == JavadocBlockTag.Type.VERSION) {
                setVersion(text);
            } else if (type == JavadocBlockTag.Type.UNKNOWN) {
                addUnknown("@" + tag.getTagName(), text);
            }
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
        this.author = author;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = deprecated;
    }

    public void addParam(String param, String text) {
        Matcher matcher = TYPE_PARAMETER_PATTERN.matcher(param);
        if (matcher.find()) {
            String typeChar = matcher.group(0);
            addToDescription(SettingsProvider.get().getSettings().getFormats().getDocString().getTypeParam()
                    .replace("%type%", typeChar)
                    .replace("%text%", text));
            return;
        }

        this.params.add(SettingsProvider.get().getSettings().getFormats().getDocString().getParam()
                .replace("%param%", param)
                .replace("%text%", text));
    }

    public void addReturns(String returns) {
        this.returns = returns;
    }

    public void setSee(String see) {
        this.see = see;
    }

    public void addSerial(String serial) {
        this.serial.add(serial);
    }

    public void addSerialData(String serialData) {
        this.serialData.add(serialData);
    }

    public void addSerialField(String serialField) {
        this.serialField.add(serialField);
    }

    public void setSince(String since) {
        this.since = since;
    }

    public void addThrows(String throwz, String text) {
        this.throwz.add(SettingsProvider.get().getSettings().getFormats().getDocString().getThrow()
                .replace("%exception%", throwz)
                .replace("%text%", text));
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void addUnknown(String tag, String unknown) {
        this.unknown.add(SettingsProvider.get().getSettings().getFormats().getDocString().getUnknownTag()
                .replace("%tag%", tag)
                .replace("%text%", unknown));
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
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getAuthor()
                    .replace("%text%", author), indent));
            contentAdded = true;
        }

        if (version != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getVersion()
                    .replace("%text%", version), indent));
            builder.append(" ");
            builder.append(version);
            contentAdded = true;
        }

        if (since != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getSince()
                    .replace("%text%", since), indent));
            contentAdded = true;
        }

        if (!serial.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getSerial()
                    .replace("%text%", String.join(", ", serial)), indent));
            contentAdded = true;
        }

        if (!serialData.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getSerialData()
                    .replace("%text%", String.join(", ", serialData)), indent));
            contentAdded = true;
        }

        if (!serialField.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getSerialField()
                    .replace("%text%", String.join(", ", serialField)), indent));
            contentAdded = true;
        }

        if (!unknown.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getUnknown(), indent));
            builder.append("\n");
            builder.append(StringUtils.indent(String.join("\n", unknown), indent));
        }

        if (!params.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(String.join("\n", params), indent));
            contentAdded = true;
        }

        if (returns != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getReturn()
                    .replace("%text%", returns), indent));
            contentAdded = true;
        }

        if (!throwz.isEmpty()) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(String.join("\n", throwz), indent));
            contentAdded = true;
        }

        if (see != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getSee()
                    .replace("%text%", see), indent));
            contentAdded = true;
        }

        if (deprecated != null) {
            if (contentAdded)
                builder.append("\n\n");
            builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getDocString().getDeprecated()
                    .replace("%text%", deprecated), indent));
        }

        builder.append("\n");
        builder.append(StringUtils.indent("\"\"\"", indent));

        return builder.toString();
    }
}
