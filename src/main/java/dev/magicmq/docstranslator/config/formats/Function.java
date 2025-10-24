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


public class Function {

    private String initDefinition;
    private String definition;
    private String parameterRegular;
    private String parameterVararg;
    private String returnStatement;

    public String getInitDefinition() {
        return initDefinition;
    }

    public void setInitDefinition(String initDefinition) {
        this.initDefinition = initDefinition;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getParameterRegular() {
        return parameterRegular;
    }

    public void setParameterRegular(String parameterRegular) {
        this.parameterRegular = parameterRegular;
    }

    public String getParameterVararg() {
        return parameterVararg;
    }

    public void setParameterVararg(String parameterVararg) {
        this.parameterVararg = parameterVararg;
    }

    public String getReturnStatement() {
        return returnStatement;
    }

    public void setReturnStatement(String returnStatement) {
        this.returnStatement = returnStatement;
    }
}
