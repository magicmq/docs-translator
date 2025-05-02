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
