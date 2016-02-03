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

package com.blurengine.blur.framework.ticking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a Field annotation that only works if the field is of type {@link BAutoInt}. The purpose of this annotation is to provide a convenient
 * way of automatically modifying an integer field. A common use of this is ticking to keep track of the timing of certain events such as how long
 * a flag has been carried, or the last time since a kill (where increment = true, and the value is reset.)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TickField {

    /**
     * The interval of the field ticking. Default is 1.
     */
    int value() default 1;

    /**
     * Whether to increment or decrement every tick. Default is false.
     */
    boolean increment() default false;

    /**
     * The initial value of the field. Please keep in mind that this is overridden by the actual field declaration, if it is declared. Default is 0
     */
    int initial() default 0;

    /**
     * The amount of change, incrementation and decrementation. Default is 1. 
     * <p />
     * TODO Not sure if this is absolutely necessary. (doubting it)
     */
    int amount() default 1;
}
