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


import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.WildcardType;

public final class TypeUtils {

    private TypeUtils() {}

    public static String convertValue(String value) {
        if (value.endsWith(".class"))
            return value.substring(0, value.length() - 6);

        if (value.contains("new ")) {
            value = value.replaceAll("new ", "");
        }

        return switch(value) {
            case "true", "Boolean.TRUE" -> "True";
            case "false", "Boolean.FALSE" -> "False";
            case "null" -> "None";
            default -> value;
        };
    }

    public static String convertType(Type type) {
        if (type.isPrimitiveType()) {
            return convertPrimitiveType(type.asPrimitiveType());
        }

        if (type.isVoidType()) {
            return "None";
        }

        if (type.isClassOrInterfaceType()) {
            return convertClassOrInterfaceType(type.asClassOrInterfaceType());
        }

        if (type.isArrayType()) {
            return convertArrayType(type.asArrayType());
        }

        if (type.isWildcardType()) {
            return convertWildcardType(type.asWildcardType());
        }

        if (type.isTypeParameter()) {
            return type.asTypeParameter().getNameAsString();
        }

        return type.toString();
    }

    private static String convertPrimitiveType(PrimitiveType type) {
        return switch (type.getType()) {
            case BOOLEAN -> "bool";
            case BYTE, SHORT, INT, LONG -> "int";
            case CHAR -> "str";
            case FLOAT, DOUBLE -> "float";
        };
    }

    private static String convertClassOrInterfaceType(ClassOrInterfaceType type) {
        String baseType = type.getNameWithScope();

        baseType = switch(baseType) {
            case "String" -> "str";
            case "List", "ArrayList", "LinkedList" -> "list";
            case "Set", "HashSet", "TreeSet" -> "set";
            case "Map", "HashMap", "TreeMap", "LinkedHashMap" -> "dict";
            case "Collection", "Iterable" -> "Iterable";
            case "Iterator" -> "Iterator";
            case "Class" -> "type";
            case "PyFunction" -> "Callable";
            case "PyObject" -> "Any";
            default -> "\"" + baseType + "\"";
        };

        if (type.getTypeArguments().isPresent()) {
            StringBuilder genericTypes = new StringBuilder();
            type.getTypeArguments().get().forEach(arg -> {
                if (!genericTypes.isEmpty()) {
                    genericTypes.append(", ");
                }
                genericTypes.append(convertType(arg));
            });
            return baseType + "[" + genericTypes + "]";
        }

        return baseType;
    }

    private static String convertArrayType(ArrayType type) {
        String componentType = convertType(type.getComponentType());
        return "list[" + componentType + "]";
    }

    private static String convertWildcardType(WildcardType type) {
        if (type.getExtendedType().isPresent())
            return convertType(type.getExtendedType().get());
        else if (type.getSuperType().isPresent())
            return convertType(type.getSuperType().get());
        else
            return "Any";
    }
}
