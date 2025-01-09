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


public class Enum {

    private String declaration;
    private String entryRegular;
    private String entryWithArgs;

    public String getDeclaration() {
        return declaration;
    }

    public void setDeclaration(String declaration) {
        this.declaration = declaration;
    }

    public String getEntryRegular() {
        return entryRegular;
    }

    public void setEntryRegular(String entryRegular) {
        this.entryRegular = entryRegular;
    }

    public String getEntryWithArgs() {
        return entryWithArgs;
    }

    public void setEntryWithArgs(String entryWithArgs) {
        this.entryWithArgs = entryWithArgs;
    }
}
