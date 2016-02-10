/*
 * Copyright 2016 Ali Moghnieh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blurengine.blur.utils.relationalops;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * Represents a utility class for working with {@link Relational}s.
 * @see #deserializeNumber(String)
 * @see #operator(String)
 */
public class RelationalUtils {

    /**
     * Creates and returns a {@link Relationals#number(Number)} from a String. The relational may be static if the string has an operator (See
     * {@link #operator(String)}).
     *
     * @param string string to deserialize
     * @return number relational
     */
    public static Relational<Number> deserializeNumber(@Nonnull String string) {
        Preconditions.checkNotNull(string, "string cannot be null.");
        RelationalOperator operator = operator(string);
        Relational<Number> number = Relationals.number(Double.parseDouble(fixString(string, operator)));
        if (operator != null) {
            number = number.staticOp(operator);
        }
        return number;
    }

    /**
     * Returns a {@link RelationalOperator} from a String. The operator returned is based on the first one or two characters in the given String. If
     * the String length is less than 2, null is returned as there is no room for an operator in one-long Strings.
     * <p />
     * The following tables shows what each {@link RelationalOperator} is represented as by a String.
     * <table>
     *     <tr>
     *         <th>{@link RelationalOperator}</th>
     *         <th>String representation</th>
     *         <th>Example</th>
     *     </tr>
     *     <tr>
     *         <td>{@link RelationalOperator#EQUAL}</td>
     *         <td>=</td>
     *         <td>=123</td>
     *     </tr>
     *     <tr>
     *         <td>{@link RelationalOperator#NOT_EQUAL}</td>
     *         <td>!</td>
     *         <td>!123</td>
     *     </tr>
     *     <tr>
     *         <td>{@link RelationalOperator#GREATER_THAN}</td>
     *         <td>&gt;</td>
     *         <td>&gt;123</td>
     *     </tr>
     *     <tr>
     *         <td>{@link RelationalOperator#GREATER_THAN_OR_EQUAL}</td>
     *         <td>&gt;=</td>
     *         <td>&gt;=123</td>
     *     </tr>
     *     <tr>
     *         <td>{@link RelationalOperator#LESS_THAN}</td>
     *         <td>&lt;</td>
     *         <td>&lt;123</td>
     *     </tr>
     *     <tr>
     *         <td>{@link RelationalOperator#LESS_THAN_OR_EQUAL}</td>
     *         <td>&lt;=</td>
     *         <td>&lt;=123</td>
     *     </tr>
     * </table>
     *
     * @param string string to search for operator in
     * @return operator, nullable
     * @throws IllegalArgumentException thrown if the processed string contained more than a number and couldn't find an operator
     * @see #fixString(String, RelationalOperator)
     */
    public static RelationalOperator operator(@Nonnull String string) throws IllegalArgumentException {
        Preconditions.checkNotNull(string, "string cannot be null.");

        // The following cases require at least a single operator and digits, if the string is of length 1, return null.
        if (string.length() < 2) {
            return null;
        }

        char first = string.charAt(0);

        // e.g. =123, means exactly 123.
        if (first == '=') {
            return RelationalOperator.EQUAL;
        }
        // Handle !, e.g. !123, means everything but 123.
        else if (first == '!') {
            return RelationalOperator.NOT_EQUAL;
        }
        // Handle > and >=
        else if (first == '>') {
            if (string.charAt(1) == '=') {
                return RelationalOperator.GREATER_THAN_OR_EQUAL;
            }
            return RelationalOperator.GREATER_THAN;
        }
        // Handle < and <=
        else if (first == '<') {
            if (string.charAt(1) == '=') {
                return RelationalOperator.LESS_THAN_OR_EQUAL;
            }
            return RelationalOperator.LESS_THAN;
        } else if (string.matches("^\\d.*")) {
            return null;
        }

        throw new IllegalArgumentException("Unable to parse string for RelationalOperator: " + string);
    }

    /**
     * Returns a string with the given operator removed from it. If {@code operator} is null {@code string} is immediately returned. For example, if 
     * {@code string} "123" was given and {@code operator} was null the same string is returned. However, if the operator is not null, an 
     * {@link Error} is thrown as this method expects to remove the given nonnull operator.
     * <p />
     * The fix is done by calling substring based on the given operator length hardcoded into the method.
     *
     * @param string string to fix
     * @param operator operator to remove from the string
     * @return string without operators
     * @deprecated this method may be removed at any time, please use this with caution!
     */
    @Deprecated
    public static String fixString(@Nonnull String string, RelationalOperator operator) {
        Preconditions.checkNotNull(string, "string cannot be null.");
        if (operator == null) {
            return string;
        }
        switch (operator) {
            case EQUAL:
                return string.startsWith("=") ? string.substring(1) : string;
            case NOT_EQUAL:
            case GREATER_THAN:
            case LESS_THAN:
                return string.substring(1);
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN_OR_EQUAL:
                return string.substring(2);
            default:
                throw new Error("Unknown operator " + operator.name());
        }
    }
}
