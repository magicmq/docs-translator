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


import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.DocsTranslator;
import dev.magicmq.docstranslator.module.Module;
import dev.magicmq.docstranslator.utils.FunctionUtils;
import dev.magicmq.docstranslator.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Class extends Member {

    private final Module parent;

    private String name;
    private final List<String> extendedClasses;
    private final List<Field> staticFields;
    private final List<InitFunction> constructors;
    private final List<Function> functions;
    private final List<Member> innerClasses;

    public Class(int indent, Module parent) {
        super(indent);
        this.parent = parent;
        this.extendedClasses = new ArrayList<>();
        this.staticFields = new ArrayList<>();
        this.constructors = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.innerClasses = new ArrayList<>();
    }

    public void init(ClassOrInterfaceDeclaration declaration) {
        this.name = declaration.getNameAsString();

        for (ClassOrInterfaceType extending : declaration.getExtendedTypes()) {
            extendedClasses.add(extending.getNameAsString());
        }

        for (ClassOrInterfaceType implementing : declaration.getImplementedTypes()) {
            extendedClasses.add(implementing.getNameAsString());
        }

        if (declaration.getComment().isPresent() && declaration.getComment().get() instanceof JavadocComment) {
            Javadoc javadoc = ((JavadocComment) declaration.getComment().get()).parse();
            newDocString(indent + 4).parse(javadoc);
        }

        for (FieldDeclaration field : declaration.getFields()) {
            if (!field.isPublic())
                continue;

            if (field.isStatic()) {
                staticFields.add(new Field(indent + 4, field));
            }
        }

        for (ConstructorDeclaration constructor : declaration.getConstructors()) {
            if (constructor.isPublic())
                constructors.add(new InitFunction(indent + 4, constructor));
        }
        if (constructors.size() > 1) {
            constructors.forEach(InitFunction::markOverloaded);
        }

        for (MethodDeclaration method : declaration.getMethods()) {
            if (declaration.isInterface() || method.isPublic()) {
                Function function = new Function(indent + 4, method);
                this.functions.add(function);
            }
        }
        FunctionUtils.markOverloadedFunctions(this.functions);

        List<ClassOrInterfaceDeclaration> innerClasses = declaration.findAll(ClassOrInterfaceDeclaration.class, inner -> inner.getParentNode().orElseThrow().equals(declaration));
        for (ClassOrInterfaceDeclaration innerClass : innerClasses) {
            if (innerClass.isPublic()) {
                Class clazz = new Class(indent + 4, parent);
                clazz.init(innerClass);
                this.innerClasses.add(clazz);
            }
        }

        List<EnumDeclaration> innerEnums = declaration.findAll(EnumDeclaration.class, inner -> inner.getParentNode().orElseThrow().equals(declaration));
        for (EnumDeclaration innerEnum : innerEnums) {
            if (innerEnum.isPublic()) {
                Enum enum_ = new Enum(indent + 4, parent);
                enum_.init(innerEnum);
                this.innerClasses.add(enum_);
            }
        }
    }

    @Override
    public String translate() {
        StringBuilder builder = new StringBuilder();

        String declaration;
        if (extendedClasses.isEmpty()) {
            String replaced = DocsTranslator.get().getSettings().getFormats().getClass_().getDeclaration().replace("%name%", name);
            declaration = StringUtils.indent(replaced, indent);
        } else {
            String replaced = DocsTranslator.get().getSettings().getFormats().getClass_().getDeclarationExtending()
                    .replace("%name%", name)
                    .replace("%extends%", String.join(", ", extendedClasses));
            declaration = StringUtils.indent(replaced, indent);
        }
        builder.append(declaration);

        if (docString != null) {
            builder.append("\n");
            builder.append(docString.serialize());
        }

        builder.append("\n\n");

        if (!staticFields.isEmpty()) {
            builder.append(staticFields.stream().map(Field::translate).collect(Collectors.joining("\n", "",
                    (!constructors.isEmpty() || !functions.isEmpty() || !innerClasses.isEmpty()) ? "\n\n\n" : "")));
        }

        if (!constructors.isEmpty()) {
            builder.append(constructors.stream().map(InitFunction::translate).collect(Collectors.joining("\n\n\n", "",
                    (!functions.isEmpty() || !innerClasses.isEmpty()) ? "\n\n\n" : "")));
        }

        if (!functions.isEmpty()) {
            builder.append(functions.stream().map(Function::translate).collect(Collectors.joining("\n\n\n", "",
                    (!innerClasses.isEmpty()) ? "\n\n\n" : "")));
        }

        if (!innerClasses.isEmpty()) {
            builder.append(innerClasses.stream().map(Member::translate).collect(Collectors.joining("\n\n\n")));
        }

        return builder.toString();
    }

}
