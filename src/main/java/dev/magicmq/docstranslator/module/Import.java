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


import dev.magicmq.docstranslator.DocsTranslator;

public record Import(String packageName, String className) implements Comparable<Import> {

    @Override
    public int compareTo(Import o) {
        return (this.packageName + "." + this.className).compareTo(o.packageName + "." + o.className);
    }

    @Override
    public String toString() {
        return DocsTranslator.get().getSettings().getFormats().getModule().getImportDeclaration()
                .replace("%package%", this.packageName)
                .replace("%module%", this.className);
    }
}
