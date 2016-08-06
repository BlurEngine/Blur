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

package com.blurengine.blur.modules.extents;

import com.google.common.base.Preconditions;

import com.blurengine.blur.framework.InternalModule;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.framework.ModuleLoader;
import com.blurengine.blur.framework.ModuleManager;
import com.blurengine.blur.session.BlurSession;
import com.supaham.commons.utils.StringUtils;

import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Represents an {@link Extent} manager. This manager contains all extents registered by a {@link ModuleManager} meaning these same extents can be 
 * reused in child {@link BlurSession}.
 */
@ModuleInfo(name = "BlurExtentManager")
@InternalModule
public class ExtentManager extends Module {

    public static final String FILTER_PREFIX = "extent-";

    private Map<String, Extent> extents = new HashMap<>();

    static {
        ModuleLoader.register(ExtentsModule.class);
    }

    public ExtentManager(ModuleManager moduleManager) {
        super(moduleManager);
    }

    public void addExtent(String id, Extent extent) {
        // FIXME use a mutable union region instead?
        if (id == null) {
            id = UUID.randomUUID().toString();
            while (this.extents.containsKey(id)) {
                id = UUID.randomUUID().toString();
            }
        } else {
            Preconditions.checkArgument(!this.extents.containsKey(id), "Extent with id '%s' already exists.", id);
            // Add filter reference by default for convenience.
            getModuleManager().getFilterManager().addFilter(FILTER_PREFIX + id, extent);

        }
        this.extents.put(id, extent);
    }

    /* ================================
     * >> GETTERS
     * ================================ */

    @Nonnull
    public Extent getExtentByString(String string) {
        try {
            return getExtentById(string);
        } catch (ExtentNotFoundException e) {
            try {
                return getExtentByRegex(string);
            } catch (ExtentNotFoundException ignored) {
                throw e;
            }
        }
    }

    @Nonnull
    public Extent getOrCreateExtent(Object object) {
        if (object instanceof String) {
            return getExtentByString(object.toString());
        }
        return getModuleManager().getModuleLoader().getExtentSerializer().deserializeExtent(((Map<String, Object>) object));
    }

    @Nonnull
    public Collection<Extent> getExtents() {
        return Collections.unmodifiableCollection(extents.values());
    }

    public Extent getExtentByRegex(String regex) throws ExtentNotFoundException {
        StringUtils.checkNotNullOrEmpty(regex, "regex");
        return getExtentByRegex(Pattern.compile(regex));
    }

    @Nonnull
    public Extent getExtentByRegex(Pattern pattern) throws ExtentNotFoundException {
        Preconditions.checkNotNull(pattern, "pattern cannot be null.");
        List<Extent> found = this.extents.entrySet().stream()
            .filter(e -> pattern.matcher(e.getKey()).matches())
            .map(Entry::getValue)
            .collect(Collectors.toList());
        switch (found.size()) {
            case 0:
                throw new ExtentNotFoundException(pattern);
            case 1:
                return found.get(0);
            default:
                return new UnionExtent(found);
        }
    }

    @Nonnull
    public Extent getExtentById(String id) throws ExtentNotFoundException {
        Extent extent = this.extents.get(id);
        if (extent == null) {
            throw new ExtentNotFoundException(id);
        }
        return extent;
    }

    public Optional<Extent> getFirstExtentWithin(@Nonnull Vector vector) {
        Preconditions.checkNotNull(vector, "vector cannot be null.");
        return getExtents().stream().filter(e -> e.contains(vector)).findFirst();
    }

    public List<Extent> getExtentsWithin(@Nonnull Vector vector) {
        Preconditions.checkNotNull(vector, "vector cannot be null.");
        return getExtents().stream().filter(e -> e.contains(vector)).collect(Collectors.toList());
    }
}
