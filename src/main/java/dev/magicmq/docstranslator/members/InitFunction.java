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


import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.SettingsProvider;
import dev.magicmq.docstranslator.doc.DocString;
import dev.magicmq.docstranslator.utils.StringUtils;
import dev.magicmq.docstranslator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class InitFunction extends Member {

    private final ConstructorDeclaration constructorDeclaration;

    private boolean overloaded;

    public InitFunction(int indent, ConstructorDeclaration constructorDeclaration) {
        super(indent);
        this.constructorDeclaration = constructorDeclaration;
        this.overloaded = false;
    }

    public void markOverloaded() {
        this.overloaded = true;
    }

    @Override
    public String translate() {
        List<String> parameters = new ArrayList<>();
        parameters.add("self");
        constructorDeclaration.getParameters().forEach(param -> {
            if (param.isVarArgs()) {
                parameters.add(SettingsProvider.get().getSettings().getFormats().getFunction().getParameterVararg()
                        .replace("%name%", param.getNameAsString())
                        .replace("%type%", TypeUtils.convertType(param.getType())));
            } else {
                parameters.add(SettingsProvider.get().getSettings().getFormats().getFunction().getParameterRegular()
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

        if (overloaded) {
            builder.append(StringUtils.indent("@overload", indent));
            builder.append("\n");
        }

        String params = String.join(", ", parameters);
        String def = SettingsProvider.get().getSettings().getFormats().getFunction().getInitDefinition().replace("%params%", params);
        builder.append(StringUtils.indent(def, indent));

        builder.append("\n");

        if (docString != null) {
            builder.append(docString.serialize());
            builder.append("\n");
        }

        builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getFunction().getReturnStatement(), indent + 4));

        return builder.toString();
    }
}
