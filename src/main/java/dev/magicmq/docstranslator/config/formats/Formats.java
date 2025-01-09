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
package dev.magicmq.docstranslator.config.formats;


public class Formats {

    private Module module_;
    private Class class_;
    private Enum enum_;
    private Function function;
    private Field field;
    private DocString docString;
    private Packaging packaging;

    public Module getModule() {
        return module_;
    }

    public void setModule(Module module_) {
        this.module_ = module_;
    }

    public Class getClass_() {
        return class_;
    }

    public void setClass_(Class class_) {
        this.class_ = class_;
    }

    public Enum getEnum() {
        return enum_;
    }

    public void setEnum(Enum enum_) {
        this.enum_ = enum_;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public DocString getDocString() {
        return docString;
    }

    public void setDocString(DocString docString) {
        this.docString = docString;
    }

    public Packaging getPackaging() {
        return packaging;
    }

    public void setPackaging(Packaging packaging) {
        this.packaging = packaging;
    }
}
