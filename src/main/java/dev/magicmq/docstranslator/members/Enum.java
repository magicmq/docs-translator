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


import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import dev.magicmq.docstranslator.SettingsProvider;
import dev.magicmq.docstranslator.module.Module;
import dev.magicmq.docstranslator.utils.FunctionUtils;
import dev.magicmq.docstranslator.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Enum extends Member {

    private final Module parent;

    private String name;
    private List<Field> staticFields;
    private List<EnumEntry> entries;
    private List<Function> functions;
    private List<Member> innerClasses;

    private int counter;

    public Enum(int indent, Module parent) {
        super(indent);
        this.parent = parent;
        this.staticFields = new ArrayList<>();
        this.entries = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.innerClasses = new ArrayList<>();
        this.counter = 0;

        if (!parent.hasImport("Enum"))
            parent.addImport("enum", "Enum");
    }

    public void init(EnumDeclaration declaration) {
        this.name = declaration.getNameAsString();

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

        for (EnumConstantDeclaration enumConst : declaration.getEntries()) {
            entries.add(new EnumEntry(indent + 4, this.counter, enumConst));
            counter++;
        }

        for (MethodDeclaration method : declaration.getMethods()) {
            if (method.isPublic()) {
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

        String declaration = SettingsProvider.get().getSettings().getFormats().getEnum().getDeclaration().replace("%name%", name);
        builder.append(StringUtils.indent(declaration, indent));

        if (docString != null) {
            builder.append("\n");
            builder.append(docString.serialize());
        }

        builder.append("\n\n");

        if (!staticFields.isEmpty()) {
            builder.append("# Static fields\n");
            builder.append(staticFields.stream().map(Field::translate).collect(Collectors.joining("\n", "",
                    (!entries.isEmpty() || !functions.isEmpty() || !innerClasses.isEmpty()) ? "\n\n\n" : "")));
        }

        if (!entries.isEmpty()) {
            builder.append(entries.stream().map(EnumEntry::translate).collect(Collectors.joining("\n", "",
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
