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

import static org.junit.Assert.assertTrue;

import com.blurengine.blur.modules.filters.Filter;
import com.blurengine.blur.modules.filters.Filter.FilterResponse;
import com.blurengine.blur.modules.filters.lexer.FilterRecursiveDescentParser;

import org.junit.Test;

/**
 * Tests the filter lexer.
 */
public class FilterLexerTest {

    public static final String TRUE = "true";
    public static final String TRUE_OR_FALSE = "true|false";
    public static final String TRUE_OR_FALSE_WHITESPACE = "true | false";
    public static final String COMPLEX_EXPRESSION_RETURN_FALSE = "((false & true) & true) & (true & false) | ((false) | false)";

    @Test
    public void testTrue() throws Exception {
        Filter filter = parse(TRUE);
        assertTrue(filter.test(null) == FilterResponse.ALLOW);
    }

    @Test
    public void testTrueOrFalse() throws Exception {
        Filter filter = parse(TRUE_OR_FALSE);
        assertTrue(filter.test(null) == FilterResponse.ALLOW);

        filter = parse(TRUE_OR_FALSE_WHITESPACE);
        assertTrue(filter.test(null) == FilterResponse.ALLOW);
    }

    @Test
    public void testComplex() throws Exception {
        Filter filter = parse(COMPLEX_EXPRESSION_RETURN_FALSE);
        assertTrue(filter.test(null) == FilterResponse.DENY);
    }

    public Filter parse(String str) {
        return new FilterRecursiveDescentParser(a -> null, str).call(); // FIXME First param is a null supplier for filter references by id.
    }
}
