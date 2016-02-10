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

import org.junit.Assert;
import org.junit.Test;

public class RelationalOpsTest {

    public static final int NUMBER = 123;
    private static final String PLAIN_NUMBER = "" + NUMBER;
    private static final String EQUAL_NUMBER = "=" + NUMBER;
    private static final String NOT_EQUAL_NUMBER = "!" + NUMBER;
    private static final String GT_NUMBER = ">" + NUMBER;
    private static final String LT_NUMBER = "<" + NUMBER;
    private static final String GTE_NUMBER = ">=" + NUMBER;
    private static final String LTE_NUMBER = "<=" + NUMBER;

    @Test
    public void testFixString() throws Exception {
        Assert.assertEquals("123", RelationalUtils.fixString(PLAIN_NUMBER, RelationalOperator.EQUAL));
        Assert.assertEquals("123", RelationalUtils.fixString(EQUAL_NUMBER, RelationalOperator.EQUAL));

        Assert.assertEquals("123", RelationalUtils.fixString(NOT_EQUAL_NUMBER, RelationalOperator.NOT_EQUAL));
        Assert.assertEquals("123", RelationalUtils.fixString(GT_NUMBER, RelationalOperator.GREATER_THAN));
        Assert.assertEquals("123", RelationalUtils.fixString(LT_NUMBER, RelationalOperator.LESS_THAN));

        Assert.assertEquals("123", RelationalUtils.fixString(GTE_NUMBER, RelationalOperator.GREATER_THAN_OR_EQUAL));
        Assert.assertEquals("123", RelationalUtils.fixString(LTE_NUMBER, RelationalOperator.LESS_THAN_OR_EQUAL));

    }

    @Test
    public void testOperator() throws Exception {
        Assert.assertEquals(null, RelationalUtils.operator(PLAIN_NUMBER));
        Assert.assertEquals(RelationalOperator.EQUAL, RelationalUtils.operator(EQUAL_NUMBER));

        Assert.assertEquals(RelationalOperator.NOT_EQUAL, RelationalUtils.operator(NOT_EQUAL_NUMBER));
        Assert.assertEquals(RelationalOperator.GREATER_THAN, RelationalUtils.operator(GT_NUMBER));
        Assert.assertEquals(RelationalOperator.LESS_THAN, RelationalUtils.operator(LT_NUMBER));

        Assert.assertEquals(RelationalOperator.GREATER_THAN_OR_EQUAL, RelationalUtils.operator(GTE_NUMBER));
        Assert.assertEquals(RelationalOperator.LESS_THAN_OR_EQUAL, RelationalUtils.operator(LTE_NUMBER));
    }

    @Test
    public void testDeserializationNoOperator() throws Exception {
        Relational<Number> relational = RelationalUtils.deserializeNumber(PLAIN_NUMBER);
        // PLAIN_NUMBER has no operator, so whatever RelationalOperator we provide is what it will follow.
        Assert.assertTrue(relational.apply(NUMBER, RelationalOperator.EQUAL)); // We want only 123, so this will return true.
        Assert.assertFalse(relational.apply(NUMBER + 123123, RelationalOperator.EQUAL)); // however this number isn't 123, so this will return false.
    }

    @Test
    public void testDeserializationNotEqual() throws Exception {
        Relational<Number> relational = RelationalUtils.deserializeNumber(NOT_EQUAL_NUMBER);
        Assert.assertFalse(relational.apply(NUMBER, null)); // We want everything but 123, so this will return false.
        Assert.assertTrue(relational.apply(NUMBER + 123, null)); // however this number isn't 123, so this will return true.
    }

    @Test
    public void testDeserializationGreaterThan() throws Exception {
        Relational<Number> relational = RelationalUtils.deserializeNumber(GT_NUMBER);
        Assert.assertFalse(relational.apply(NUMBER, null)); // We only allow numbers greater than NUMBER
        Assert.assertTrue(relational.apply(NUMBER + 1, null)); // NUMBER + 1 is greater than NUMBER.
    }

    @Test
    public void testDeserializationGreaterThanOrEqual() throws Exception {
        Relational<Number> relational = RelationalUtils.deserializeNumber(GTE_NUMBER);
        Assert.assertFalse(relational.apply(NUMBER - 1, null)); // We only allow numbers greater than or equal to NUMBER
        Assert.assertTrue(relational.apply(NUMBER, null)); // NUMBER is equal to NUMBER so allow it.
        Assert.assertTrue(relational.apply(NUMBER + 1, null)); // NUMBER + 1 is greater than NUMBER.
    }

    @Test
    public void testDeserializationLessThan() throws Exception {
        Relational<Number> relational = RelationalUtils.deserializeNumber(LT_NUMBER);
        Assert.assertFalse(relational.apply(NUMBER, null)); // We only allow numbers less than NUMBER
        Assert.assertTrue(relational.apply(NUMBER - 1, null)); // NUMBER - 1 is less than NUMBER.
    }

    @Test
    public void testDeserializationLessThanOrEqual() throws Exception {
        Relational<Number> relational = RelationalUtils.deserializeNumber(LTE_NUMBER);
        Assert.assertFalse(relational.apply(NUMBER + 1, null)); // We only allow numbers less than or equal to NUMBER
        Assert.assertTrue(relational.apply(NUMBER, null)); // NUMBER is equal to NUMBER so allow it.
        Assert.assertTrue(relational.apply(NUMBER - 1, null)); // NUMBER - 1 is less than NUMBER.
    }
}
