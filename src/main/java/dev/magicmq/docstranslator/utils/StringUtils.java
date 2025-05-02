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

package dev.magicmq.docstranslator.utils;


import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StringUtils {

    public static String indent(String string, int n) {
        if (string.isEmpty()) {
            return "";
        }

        Stream<String> stream = string.lines();
        if (n > 0) {
            final String spaces = " ".repeat(n);
            stream = stream.map(s -> spaces + s);
        } else if (n == Integer.MIN_VALUE) {
            stream = stream.map(String::stripLeading);
        }

        return stream.collect(Collectors.joining("\n", "", ""));
    }

    public static String replaceFormatting(String string) {
        string = string.replace("<p>", "");
        string = string.replace("</p>", "");
        string = string.replace("<br>", "");
        string = string.replace("</br>", "");

        string = string.replace("<code>", "`");
        string = string.replace("</code>", "`");

        string = string.replace("<b>", "**");
        string = string.replace("</b>", "**");

        string = string.replace("<em>", "*");
        string = string.replace("</em>", "*");
        string = string.replace("<i>", "*");
        string = string.replace("</i>", "*");

        string = string.replace("<pre>", "```");
        string = string.replace("</pre>", "```");

        string = string.replace("<ul>", "");
        string = string.replace("</ul>", "");
        string = string.replace("<li>", "- ");
        string = string.replace("</li>", "");

        return string;
    }

    public static String replaceInlineTag(String string) {
        string = string.replace("#", ".");

        return string;
    }
}
