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


import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.SettingsProvider;
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
        List<String> values = new ArrayList<>();
        for (VariableDeclarator declarator : fieldDeclaration.getVariables()) {
            String fieldName = declarator.getNameAsString();
            Expression initializer = declarator.getInitializer().orElse(null);
            String type = TypeUtils.convertType(declarator.getType());
            String replaced = SettingsProvider.get().getSettings().getFormats().getField().getInitializer()
                    .replace("%name%", fieldName)
                    .replace("%type%", type);

            variables.add(StringUtils.indent(replaced, indent));
            values.add(initializer != null ? "`" + TypeUtils.convertValue(initializer.toString()) + "`" : "`None`");
        }
        if (fieldDeclaration.getComment().isPresent() && fieldDeclaration.getComment().get() instanceof JavadocComment) {
            Javadoc javadoc = ((JavadocComment) fieldDeclaration.getComment().get()).parse();
            newDocString().parse(javadoc);
        }

        StringBuilder builder = new StringBuilder();

        builder.append(String.join("\n", variables));

        if (docString == null) {
            newDocString();
            docString.setDescription(SettingsProvider.get().getSettings().getFormats().getField().getValuesDocString()
                    .replace("%values%", String.join(",", values)));
        } else
            docString.addToDescription(SettingsProvider.get().getSettings().getFormats().getField().getValuesDocString()
                    .replace("%values%", String.join(",", values)));

        if (docString != null) {
            builder.append("\n");
            builder.append(docString.serialize());
        }

        return builder.toString();
    }

}
