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

package com.blurengine.blur.modules.framework.ticking;

import com.google.common.base.Preconditions;

import com.blurengine.blur.modules.framework.InternalModule;
import com.blurengine.blur.modules.framework.Module;
import com.blurengine.blur.modules.framework.ModuleInfo;
import com.blurengine.blur.modules.framework.ModuleManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

@InternalModule
@ModuleInfo(name = "TickFieldHolder")
public class TickFieldHolder extends Module implements Runnable{

    private final List<TickFieldGenerated> list = new ArrayList<>();

    public TickFieldHolder(@Nonnull ModuleManager moduleManager) {
        super(moduleManager);

        // This is where the ticking happens
        newTask(this).interval((long) 1).build();
    }

    public void load(@Nonnull Object object) {
        Preconditions.checkNotNull(object, "object cannot be null.");
        for (Field field : object.getClass().getDeclaredFields()) {
            TickField annotation = field.getDeclaredAnnotation(TickField.class);
            if (annotation != null) {
                Preconditions.checkArgument(field.getType().isAssignableFrom(BAutoInt.class), "%s must by of type %s", field, BAutoInt.class.getName());

                field.setAccessible(true);
                try {
                    TickFieldGenerated generated = new TickFieldGenerated((BAutoInt) field.get(object), annotation);
                    field.set(object, generated);
                    list.add(generated);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println();
    }

    @Override
    public void run() {
        for (TickFieldGenerated tickFieldGenerated : this.list) {
            // try-catch as a precaution in case anyone tries to be cheeky with their top level bants and break things for everyone.
            try {
                tickFieldGenerated.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
