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

package com.blurengine.blur.framework;

import com.google.common.base.Preconditions;

import com.blurengine.blur.framework.playerdata.PlayerDataSupplier;
import com.blurengine.blur.modules.stages.StageManager;
import com.blurengine.blur.session.BlurPlayer;
import com.blurengine.blur.session.BlurSession;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Module framework base class. Upon extending this class, you will need to annotate the extension with {@link ModuleInfo} to tell the framework
 * how to load this module from the config file. Immediately after implementation, ensure that the class is also registered to the
 * {@link ModuleManager} using {@link ModuleLoader#register(Class)}.
 *
 * @see #Module(ModuleManager)
 */
public abstract class Module extends AbstractComponent {

    private final ModuleInfo moduleInfo;
    private final Set<Component> subcomponents = new HashSet<>();
    private final Map<Class<?>, PlayerDataSupplier<Object>> registeredPlayerDataClassSuppliers = new HashMap<>();
    private final Set<Class> registeredPlayerDataClasses = new HashSet<>();

    public Module(ModuleManager moduleManager) {
        super(moduleManager);
        this.moduleInfo = ModuleLoader.getModuleInfoByModule(getClass());
    }

    /**
     * Returns sub {@link Component} stream without any {@link Module}. This is important to the stability of module loading overall.
     *
     * When a Module registers a sub module, the sub module is registered to the ModuleManager. The ModuleManager then loads that sub module as a
     * normal module. When the main Module invokes any of the state-changing methods it fails because those sub modules are already loaded and return
     * false (indicating a failure to load) due to it being loaded already.
     */
    private Stream<Component> getSubcomponentsStream() {
        return this.subcomponents.stream().filter(component -> !(component instanceof Module));
    }

    @Override
    public boolean tryLoad() {
        if (!super.tryLoad()) {
            return false;
        }
        boolean resultOfAllLoads = getSubcomponentsStream().map(Component::tryLoad).filter(b -> !b).findFirst().orElse(true);
        return resultOfAllLoads;
    }

    @Override
    public boolean tryUnload() {
        if (!super.tryUnload()) {
            return false;
        }
        boolean resultOfAllUnloads = getSubcomponentsStream().map(Component::tryUnload).filter(b -> !b).findFirst().orElse(true);
        return resultOfAllUnloads;
    }

    @Override
    public boolean tryEnable() {
        if (!super.tryEnable()) {
            return false;
        }
        boolean resultOfAllEnables = getSubcomponentsStream().map(Component::tryEnable).filter(b -> !b).findFirst().orElse(true);
        return resultOfAllEnables;
    }

    @Override
    public boolean tryDisable() {
        if (!super.tryDisable()) {
            return false;
        }
        boolean resultOfAllDisables = getSubcomponentsStream().map(Component::tryDisable).filter(b -> !b).findFirst().orElse(true);
        return resultOfAllDisables;
    }

    public ModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    public boolean addSubcomponent(@Nonnull Component component) {
        Preconditions.checkNotNull(component, "component cannot be null.");
        if (component instanceof Module) {
            return addSubmodule((Module) component);
        } else {
            if (this.subcomponents.add(component)) {
                if (getState() == ComponentState.UNLOADED) {
                    return true; // Added successfully without loading.
                }

                if (component.tryLoad()) {
                    if (getState() == ComponentState.ENABLED) {
                        if (component.tryEnable()) {
                            return true; // Added, loaded, and enabled successfully.
                        } else {
                            return false; // Failed to enable
                        }
                    }
                    return true; // Added and loaded successfully.
                } else {
                    return false; // Failed to load
                }
            }
            return false;
        }
    }

    public boolean removeSubcomponent(@Nonnull Component component) {
        Preconditions.checkNotNull(component, "component cannot be null.");
        if (component instanceof Module) {
            return removeSubmodule((Module) component);
        } else {
            if (this.subcomponents.remove(component)) {
                component.tryDisable();
                component.tryUnload();
                return true;
            }
            return false;
        }
    }

    public boolean addSubmodule(@Nonnull Module module) {
        Preconditions.checkNotNull(module, "module cannot be null.");
        if (this.subcomponents.add(module)) {
            // This is a must to ensure that any added behaviour from ModuleManager is respected.
            getModuleManager().addModule(module);

            if (getState() == ComponentState.UNLOADED) {
                return true;  // Added successfully without loading.
            }

            if (getModuleManager().loadModule(module)) {
                if (getState() == ComponentState.ENABLED) {
                    if (getModuleManager().enableModule(module)) {
                        return true; // Added, loaded, and enabled successfully.
                    } else {
                        return false; // Failed to enable
                    }
                }
                return true; // Added and loaded successfully.
            } else {
                return false; // Failed to load
            }
        }
        return false;
    }

    public boolean removeSubmodule(@Nonnull Module module) {
        Preconditions.checkNotNull(module, "module cannot be null.");
        if (this.subcomponents.remove(module)) {
            getModuleManager().disableModule(module);
            getModuleManager().unloadModule(module);
            return true;
        }
        return false;
    }

    public Collection<Component> getSubcomponents() {
        return Collections.unmodifiableCollection(subcomponents);
    }

    public StageManager getStagesManager() {
        return getModuleManager().getStageManager();
    }

    public Set<Class> getRegisteredPlayerDataClasses() {
        return Collections.unmodifiableSet(this.registeredPlayerDataClasses);
    }

    public Map<Class, PlayerDataSupplier<Object>> getRegisteredPlayerDataClassSuppliers() {
        return Collections.unmodifiableMap(this.registeredPlayerDataClassSuppliers);
    }

    /**
     * Registers a class as a Player Data class. Player Data classes are classes that are instantiated automatically when a player is being adding to
     * a {@link BlurSession}. In order for this feature to function properly, the given {@link Class} <b>MUST</b> have one of the following:
     * <ul>
     *     <li>A publicly accessible zero-arg constructor</li>
     *     <li>A publicly accessible one-arg constructor of type {@link BlurPlayer}</li>
     * </ul>
     * Failure to do so will cause valid errors. <p />
     *
     * For more control over class instantiation see {@link #registerPlayerDataClass(Class, PlayerDataSupplier)}
     *
     * @param clazz Player Data class
     * @see #registerPlayerDataClass(Class, PlayerDataSupplier)
     */
    public void registerPlayerDataClass(@Nonnull Class clazz) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");

        Preconditions.checkArgument(!this.registeredPlayerDataClassSuppliers.containsKey(clazz),
            "%s already registered (with supplier)", clazz);
        Preconditions.checkArgument(!this.registeredPlayerDataClasses.contains(clazz),
            "%s already registered (without supplier)", clazz);
        this.registeredPlayerDataClasses.add(clazz);
    }

    /**
     * Registers a Player Data class instance supplier alongside its class (given). 
     * @param clazz Player Data class that is supplied from the {@code supplier}
     * @param supplier supplier of Player Data class instance
     * @param <T> type of class being supplied
     */
    public <T> void registerPlayerDataClass(@Nonnull Class<T> clazz, @Nonnull PlayerDataSupplier<T> supplier) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");
        Preconditions.checkNotNull(supplier, "supplier cannot be null.");

        Preconditions.checkArgument(!this.registeredPlayerDataClassSuppliers.containsKey(clazz),
            "%s already registered (with supplier)", clazz);
        Preconditions.checkArgument(!this.registeredPlayerDataClasses.contains(clazz),
            "%s already registered (without supplier)", clazz);
        this.registeredPlayerDataClassSuppliers.put(clazz, (PlayerDataSupplier<Object>) supplier);
    }

    public boolean unregisterPlayerDataClass(@Nonnull Class clazz) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null.");
        if (this.registeredPlayerDataClasses.remove(clazz)) {
            return true;
        } else if (this.registeredPlayerDataClassSuppliers.remove(clazz) != null) {
            return true;
        }
        return false;
    }
}
