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

package dev.magicmq.docstranslator.members;


import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.SettingsProvider;
import dev.magicmq.docstranslator.utils.StringUtils;
import dev.magicmq.docstranslator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class EnumEntry extends Member {

    private final EnumConstantDeclaration enumConstantDeclaration;
    private final String enumName;

    public EnumEntry(int indent, EnumConstantDeclaration enumConstantDeclaration, String enumName) {
        super(indent);
        this.enumConstantDeclaration = enumConstantDeclaration;
        this.enumName = enumName;
    }

    @Override
    public String translate() {
        List<String> arguments = new ArrayList<>();
        for (Expression argument : enumConstantDeclaration.getArguments()) {
            arguments.add("`" + TypeUtils.convertValue(argument.toString()) + "`");
        }

        if (enumConstantDeclaration.getComment().isPresent() && enumConstantDeclaration.getComment().get() instanceof JavadocComment) {
            Javadoc javadoc = ((JavadocComment) enumConstantDeclaration.getComment().get()).parse();
            newDocString().parse(javadoc);
        }

        StringBuilder builder = new StringBuilder();

        String replaced = SettingsProvider.get().getSettings().getFormats().getEnum().getEntry()
                .replace("%name%", enumConstantDeclaration.getNameAsString())
                .replace("%type%", enumName);
        builder.append(StringUtils.indent(replaced, indent));

        if (!arguments.isEmpty()) {
            if (docString == null) {
                newDocString();
                docString.setDescription(SettingsProvider.get().getSettings().getFormats().getEnum().getValuesDocString()
                        .replace("%values%", String.join(", ", arguments)));
            } else
                docString.addToDescription(SettingsProvider.get().getSettings().getFormats().getEnum().getValuesDocString()
                        .replace("%values%", String.join(", ", arguments)));
        }

        if (docString != null) {
            builder.append("\n");
            builder.append(docString.serialize());
        }

        return builder.toString();

    }

}
