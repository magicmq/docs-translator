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

package dev.magicmq.docstranslator.module;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Name;
import dev.magicmq.docstranslator.JdkTranslator;
import dev.magicmq.docstranslator.SettingsProvider;
import dev.magicmq.docstranslator.base.Translatable;
import dev.magicmq.docstranslator.members.Annotation;
import dev.magicmq.docstranslator.members.Class;
import dev.magicmq.docstranslator.members.Enum;
import dev.magicmq.docstranslator.members.Member;

import java.util.TreeSet;
import java.util.stream.Collectors;

public class Module implements Translatable {

    private final String groupId;
    private final String artifactId;
    private final String artifactVersion;
    private final String packageName;
    private final String moduleName;
    private final JdkTranslator javaTranslator;

    private final TreeSet<Import> imports;

    private Member member;

    public Module(String groupId, String artifactId, String artifactVersion, String packageName, String moduleName, JdkTranslator javaTranslator) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.artifactVersion = artifactVersion;
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.javaTranslator = javaTranslator;
        this.imports = new TreeSet<>();
        imports.add(new Import("typing", "Any, Callable, Iterable, Tuple, overload"));
    }

    public void init(CompilationUnit cu) {
        for (ImportDeclaration imp : cu.getImports()) {
            if (imp.isStatic())
                continue;

            Name name = imp.getName();
            String packageName = name.getQualifier().orElseThrow().asString();
            String className = name.getIdentifier();

            if (imp.isAsterisk()) {
                if (!SettingsProvider.get().getSettings().getImportExclusions().getClasses().contains(name.asString()) && !SettingsProvider.get().getSettings().getImportExclusions().getPackages().contains(packageName + "." + className)) {
                    this.imports.add(new Import(name.asString(), "*"));
                }
            } else {
                if (!SettingsProvider.get().getSettings().getImportExclusions().getClasses().contains(name.asString()) && !SettingsProvider.get().getSettings().getImportExclusions().getPackages().contains(packageName)) {
                    if (name.asString().startsWith("java.") && !this.packageName.startsWith("java."))
                        javaTranslator.addSourceFile(packageName, className);

                    this.imports.add(new Import(packageName, className));
                }
            }
        }
        this.imports.add(new Import(packageName, "*"));

        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (type instanceof ClassOrInterfaceDeclaration classDeclaration) {
                Class clazz = new Class(0, this);
                clazz.init(classDeclaration);
                this.member = clazz;
            } else if (type instanceof EnumDeclaration enumDeclaration) {
                Enum enum_ = new Enum(0, this);
                enum_.init(enumDeclaration);
                this.member = enum_;
            } else if (type instanceof AnnotationDeclaration annotationDeclaration) {
                Annotation annotation = new Annotation(0, this);
                annotation.init(annotationDeclaration);
                this.member = annotation;
            }
            break;
        }
    }

    public boolean hasImport(String className) {
        for (Import imp : imports) {
            if (imp.className().equals(className))
                return true;
        }
        return false;
    }

    public void addImport(String packageName, String type) {
        imports.add(new Import(packageName, type));
    }

    @Override
    public String translate() {
        StringBuilder builder = new StringBuilder();

        builder.append(SettingsProvider.get().getSettings().getFormats().getModule().getDocString()
                .replace("%class%", packageName + "." + moduleName)
                .replace("%group_id%", groupId)
                .replace("%artifact_id%", artifactId)
                .replace("%artifact_version%", artifactVersion));

        if (!imports.isEmpty()) {
            builder.append(imports.stream().map(Import::toString).collect(Collectors.joining("\n")));
            builder.append("\n\n\n");
        } else {
            builder.append("\n\n");
        }

        if (member != null)
            builder.append(member.translate());

        builder.append("\n");

        return builder.toString();
    }

}
