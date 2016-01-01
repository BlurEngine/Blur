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

import com.google.common.base.Function;

import java.util.regex.Pattern;

public enum TokenType {
    TRUE("true"),
    FALSE("false"),
    AND("&"),
    OR("\\|"),
    NOT("!"),
    LEFTPAREN("\\("),
    RIGHTPAREN("\\)"),
    WHITESPACE("\\s+"),
    PLAINTEXT("[A-Za-z0-9]+", true);

    private static Pattern FINAL_PATTERN;

    private final String pattern;
    private final Function<String, Token> tokenProducer;

    public static Pattern getFinalPattern() {
        if (FINAL_PATTERN == null) {
            constructPattern();
        }
        return FINAL_PATTERN;
    }

    private static void constructPattern() {
        StringBuffer tokenPatternsBuffer = new StringBuffer();
        for (TokenType tokenType : TokenType.values()) {
            tokenPatternsBuffer.append(String.format("|(?<%s>%s)", tokenType.name(), tokenType.pattern));
        }
        FINAL_PATTERN = Pattern.compile(tokenPatternsBuffer.substring(1));
    }

    TokenType(String pattern) {
        this(pattern, false);
    }

    TokenType(String pattern, boolean symbol) {
        this.pattern = pattern;
        if (symbol) {
            this.tokenProducer = (s) -> new Token(this, s);
        } else {
            this.tokenProducer = (s) -> new Token(this, null);
        }
    }

    public Token toToken(String s) {
        return tokenProducer.apply(s);
    }
}
