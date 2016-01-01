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
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import com.blurengine.blur.modules.filters.Filters;
import com.blurengine.blur.modules.filters.serializer.FilterSerializer;
import com.blurengine.blur.modules.framework.ModuleLoader;
import com.blurengine.blur.modules.filters.Filter;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * Represents a {@link Filter} parser where everything is done recursively. Please refrain from using this class directly and instead use 
 * {@link FilterSerializer} accessible through {@link ModuleLoader#getFilterSerializer()}.
 */
public class FilterRecursiveDescentParser implements Callable<Filter> {

    private final Function<String, Filter> filterIdGetter;
    private final String input;
    private final Collection<Token> tokens;
    private final Iterator<Token> iterator;

    private int index = -1;
    private Token current;

    public FilterRecursiveDescentParser(Function<String, Filter> filterIdGetter, String input) {
        this.filterIdGetter = filterIdGetter;
        this.input = input;
        this.tokens = new FilterLexer().lex(input);
        this.iterator = tokens.iterator();
    }

    private Token next() {
        Token token = iterator.hasNext() ? iterator.next() : null;
        index++;
        if (token != null) {
            current = token;
        }
        return current;
    }

    @Override
    public Filter call() {
        return expression();
    }

    public Filter expression() {
        Filter term = term();
        while (current.getType() == TokenType.OR) {
            term = term.or(term());
        }
        return term;
    }

    private Filter term() {
        Filter factor = factor();
        while (current.getType() == TokenType.AND) {
            factor = factor.and(factor());
        }
        return factor;
    }

    private Filter factor() {
        Filter result = null;

        next();
        if (current == null) {
            throw new RuntimeException("Expression Malformed: " + this.input);
        }
        switch (current.getType()) {
            case PLAINTEXT:
                String filterId = current.getData().trim();
                check(!filterId.isEmpty(), "Filter name is somehow empty.");

                if (this.index < this.tokens.size() - 1) {
                    // Peek to make sure syntax is correct.
                    Token token = Iterators.get(this.tokens.iterator(), index + 1);
                    check(token.getType() != TokenType.PLAINTEXT, "A token must follow a filter.");
                }
                result = filterIdGetter.apply(filterId);
                Preconditions.checkArgument(result != null, "Filter id '%s' not found.", filterId);
                next();
                return result;
            case TRUE:
                result = Filters.ALWAYS_ALLOW;
                next();
                return result;
            case FALSE:
                result = Filters.ALWAYS_DENY;
                next();
                return result;
            case NOT:
                return factor().inverse();
            case LEFTPAREN:
                result = expression();
                next();
                return result;
        }
        return result;
    }

    private void check(boolean b, String message) {
        if (!b) {
            throw new SyntaxException(message + " AT " + index);
        }
    }

    private final class SyntaxException extends RuntimeException {


        public SyntaxException(String message) {
            super(message);
        }
    }
}
