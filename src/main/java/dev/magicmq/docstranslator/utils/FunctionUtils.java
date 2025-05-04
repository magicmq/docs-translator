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


import dev.magicmq.docstranslator.members.Function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FunctionUtils {

    private FunctionUtils() {}

    public static void markOverloadedFunctions(List<Function> functions) {
        Map<String, Integer> nameCount = new HashMap<>();

        for (Function function : functions) {
            nameCount.put(function.getFunctionName(), nameCount.getOrDefault(function.getFunctionName(), 0) + 1);
        }

        for (Function function : functions) {
            if (nameCount.get(function.getFunctionName()) > 1) {
                function.markOverloaded();
            }
        }
    }
}
