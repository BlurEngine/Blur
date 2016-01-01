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

package com.blurengine.blur.modules.filters.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Represents a simple lexer that simply creates tokens matching {@link TokenType}.
 */
public class FilterLexer {

    public Collection<Token> lex(String input) {
        List<Token> result = new ArrayList<>();
        Matcher matcher = TokenType.getFinalPattern().matcher(input);
        tokenIteration:
        while (matcher.find()) {
            for (TokenType tk : TokenType.values()) {
                String group = matcher.group(tk.name());
                if (group != null && tk != TokenType.WHITESPACE) {
                    result.add(tk.toToken(group));
                    continue tokenIteration;
                }
            }
        }
        return result;
    }
}
