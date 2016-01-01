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

package com.blurengine.blur.modules.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to reference the {@link ModuleData} class used to process the annotated class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleInfo {

    /**
     * Name of this module. This is mostly used for debugging and such
     *
     * @return module name
     */
    String name();

    /**
     * Serializable {@link ModuleData} class used to serialize and deserialize this module.
     * <b>Note:</b> The data class will automatically be registered as a serializable when this module is registered to {@link ModuleManager}.
     *
     * @return data class
     */
    Class<? extends ModuleData> dataClass() default ModuleData.class;

    /**
     * Module loading type. Defaults to {@link ModuleLoadType#POST_WORLD}.
     *
     * @return the loading type to allow for this module
     */
    ModuleLoadType load() default ModuleLoadType.POST_WORLD;

    /**
     * List of {@link Module}s this module depends on, thus loading them before this module.
     *
     * @return array of module classes
     */
    Class<? extends Module>[] depends() default {};
}
