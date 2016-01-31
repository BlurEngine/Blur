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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.blurengine.blur.modules.framework.ticking.Tick;
import com.blurengine.blur.session.Tickable;
import com.blurengine.blur.utils.TaskBuilder;
import com.supaham.commons.bukkit.TickerTask;
import com.supaham.commons.utils.ReflectionUtils;
import com.supaham.commons.utils.TimeUtils;

import org.apache.commons.lang.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;


public final class TickMethodsCache {

    private static final Multimap<Class<?>, TickMethod> classTickMethods = HashMultimap.create();

    /**
     * Loads a {@link Tickable} class and generates {@link TaskBuilder}s of the {@code tickable}'s relevant methods. All given TaskBuilders require
     * the plugin to be set.
     *
     * @param tickable tickable to load task builders for.
     *
     * @return mutable collection of {@link TaskBuilder}
     */
    public static Collection<TaskBuilder> loadTickableReturnTaskBuilders(@Nonnull Tickable tickable) {
        Preconditions.checkNotNull(tickable, "tickable cannot be null.");
        return loadClass(tickable.getClass()).stream().map(t -> t.toBuilder(tickable)).collect(Collectors.toList());
    }

    /**
     * Loads and caches a {@link Class}
     *
     * @param clazz class to load and cache for future usage
     *
     * @return list of {@link TickMethod} represented the cached methods
     *
     * @throws IllegalArgumentException thrown if {@code clazz} is {@code Tickable.class}
     */
    public static Collection<TickMethod> loadClass(@Nonnull Class<?> clazz) throws IllegalArgumentException {
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");
        Preconditions.checkArgument(!clazz.equals(Tickable.class), "Cannot register class Tickable.");
        if (!classTickMethods.containsKey(clazz)) {
            { // Load superclasses first
                Class<?> superclass = clazz.getSuperclass();
                while (!superclass.equals(Tickable.class) && superclass.isAssignableFrom(Tickable.class)) {
                    loadClass(superclass);
                }
            }
            Collection<TickMethod> tickMethods = classTickMethods.get(clazz); // empty cache list, automatically updates
            for (Method method : clazz.getDeclaredMethods()) {
                TickMethod tickMethod = getTickMethod(clazz, method);
                if (tickMethod != null) {
                    tickMethods.add(tickMethod);
                } else {
                    Tick tick = method.getDeclaredAnnotation(Tick.class);
                    if (tick != null) {
                        try {
                            Preconditions.checkArgument(
                                method.getParameterCount() <= 1, "too many parameters in tick method " + method.getName() + ".");
                            if (method.getParameterCount() > 0) {
                                Preconditions.checkArgument(method.getParameterTypes()[0].isAssignableFrom(TickerTask.class),
                                    "Invalid parameter in tick method " + method.getName() + ".");
                            }
                            boolean passParams = method.getParameterCount() > 0;
                            // Tickables may be marked private for organisation.
                            method.setAccessible(true);

                            tickMethods.add(new TickMethod(method, passParams, tick));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableCollection(classTickMethods.get(clazz));
    }

    // It's important to use Class and not depend on the method class as subclasses might have modified overridden methods
    // So we use clazz to iterate over it's superclass in order, and find the latest TickMethod.
    private static TickMethod getTickMethod(Class clazz, Method method) {
        // Order of list is from bottom (subclass) to top (superclass).
        List<Class> superclasses = (List<Class>) ClassUtils.getAllSuperclasses(clazz);
        superclasses.add(0, clazz);
        // Loop over clazz and its superclasses
        for (Class superclass : superclasses) {
            // find and iterate over the current class' tick methods, if it has any.
            for (TickMethod tickMethod : classTickMethods.get(superclass)) {
                // if the superclass TickMethod has the same method as ours, return it.
                if (ReflectionUtils.sameMethodSignature(method, tickMethod.method)) {
                    return tickMethod;
                }
            }
        }
        return null;
    }

    public static final class TickMethod {

        private final Method method;
        private final boolean passParams;
        private final Tick tick;

        public TickMethod(@Nonnull Method method, boolean passParams, @Nonnull Tick tick) {
            this.method = Preconditions.checkNotNull(method, "method cannot be null.");
            this.passParams = passParams;
            this.tick = Preconditions.checkNotNull(tick, "tick cannot be null.");
        }

        public TaskBuilder toBuilder(@Nonnull Tickable tickable) {
            Preconditions.checkNotNull(tickable, "tickable cannot be null.");
            long delay = TimeUtils.parseDurationMs(tick.delay());
            long interval = TimeUtils.parseDurationMs(tick.interval());
            return new TaskBuilder().run((task) -> invoke(tickable, task)).delay(delay).interval(interval).async(tick.async());
        }

        private void invoke(Tickable tickable, TickerTask task) {
            try {
                if (this.passParams) {
                    this.method.invoke(tickable);
                } else {
                    this.method.invoke(tickable, task);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
