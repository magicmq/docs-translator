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


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.SettingsProvider;
import dev.magicmq.docstranslator.doc.DocString;
import dev.magicmq.docstranslator.utils.StringUtils;
import dev.magicmq.docstranslator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.List;

public class Function extends Member {

    private final MethodDeclaration methodDeclaration;

    private boolean overloaded;

    public Function(int indent, MethodDeclaration methodDeclaration) {
        super(indent);
        this.methodDeclaration = methodDeclaration;
        this.overloaded = false;
    }

    public void markOverloaded() {
        this.overloaded = true;
    }

    public String getFunctionName() {
        return methodDeclaration.getNameAsString();
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
                parameters.add(SettingsProvider.get().getSettings().getFormats().getFunction().getParameterVararg()
                        .replace("%name%", param.getNameAsString())
                        .replace("%type%", TypeUtils.convertType(param.getType())));
            } else {
                parameters.add(SettingsProvider.get().getSettings().getFormats().getFunction().getParameterRegular()
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

        if (overloaded) {
            builder.append(StringUtils.indent("@overload", indent));
            builder.append("\n");
        }

        if (methodDeclaration.isStatic()) {
            builder.append(StringUtils.indent("@staticmethod", indent));
            builder.append("\n");
        }

        String params = String.join(", ", parameters);
        String def = SettingsProvider.get().getSettings().getFormats().getFunction().getDefinition()
                .replace("%name%", methodName)
                .replace("%params%", params)
                .replace("%returns%", returnType);
        builder.append(StringUtils.indent(def, indent));

        builder.append("\n");

        if (docString != null) {
            builder.append(docString.serialize());
            builder.append("\n");
        }

        builder.append(StringUtils.indent(SettingsProvider.get().getSettings().getFormats().getFunction().getReturnRegular(), indent + 4));

        return builder.toString();
    }
}
