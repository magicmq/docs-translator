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


public class DocString {

    private String author;
    private String deprecated;
    private String param;
    private String typeParam;
    private String return_;
    private String see;
    private String serial;
    private String serialData;
    private String serialField;
    private String since;
    private String throw_;
    private String version;
    private String unknown;
    private String unknownTag;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = deprecated;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getTypeParam() {
        return typeParam;
    }

    public void setTypeParam(String typeParam) {
        this.typeParam = typeParam;
    }

    public String getReturn() {
        return return_;
    }

    public void setReturn(String return_) {
        this.return_ = return_;
    }

    public String getSee() {
        return see;
    }

    public void setSee(String see) {
        this.see = see;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getSerialData() {
        return serialData;
    }

    public void setSerialData(String serialData) {
        this.serialData = serialData;
    }

    public String getSerialField() {
        return serialField;
    }

    public void setSerialField(String serialField) {
        this.serialField = serialField;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getThrow() {
        return throw_;
    }

    public void setThrow(String throw_) {
        this.throw_ = throw_;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUnknown() {
        return unknown;
    }

    public void setUnknown(String unknown) {
        this.unknown = unknown;
    }

    public String getUnknownTag() {
        return unknownTag;
    }

    public void setUnknownTag(String unknownTag) {
        this.unknownTag = unknownTag;
    }
}
