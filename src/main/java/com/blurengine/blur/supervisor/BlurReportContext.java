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

package com.blurengine.blur.supervisor;

import com.google.common.base.Preconditions;

import com.blurengine.blur.Blur;
import com.blurengine.blur.BlurPlugin;
import com.blurengine.blur.framework.InternalModule;
import com.blurengine.blur.framework.Module;
import com.blurengine.blur.framework.ModuleInfo;
import com.blurengine.blur.session.BlurSession;
import com.blurengine.blur.session.SessionManager;
import com.blurengine.blur.session.WorldBlurSession;
import com.supaham.commons.utils.MapBuilder;
import com.supaham.supervisor.bukkit.SupervisorPlugin;
import com.supaham.supervisor.report.AbstractReportContextEntry;
import com.supaham.supervisor.report.ReportContext;
import com.supaham.supervisor.report.ReportContextEntry;
import com.supaham.supervisor.report.ReportSpecifications;
import com.supaham.supervisor.report.ReportSpecifications.ReportLevel;
import com.supaham.supervisor.report.SimpleReportFile;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class BlurReportContext extends ReportContext {

    private final BlurPlugin blurPlugin;
    private final Blur blur;
    private final SessionManager sessionManager;

    public static void load(BlurPlugin blurPlugin) {
        SupervisorPlugin.get().registerContext(blurPlugin, new BlurReportContext(blurPlugin));
    }

    public BlurReportContext(@Nonnull BlurPlugin blurPlugin) {
        super("blur", "Blur", "1");
        this.blurPlugin = Preconditions.checkNotNull(blurPlugin, "blurPlugin cannot be null.");
        blur = blurPlugin.getBlur();
        sessionManager = blur.getSessionManager();
    }

    @Override
    public ReportContextEntry createEntry(@Nonnull ReportSpecifications specs) {
        return new BlurContext(this, specs);
    }

    private final class BlurContext extends AbstractReportContextEntry {

        public BlurContext(@Nonnull ReportContext parentContext, @Nonnull ReportSpecifications reportSpecifications) {
            super(parentContext, reportSpecifications);
        }

        @Override
        public void run() {
            append("active-sessions", sessionManager.getBlurSessions().size());
            sessionManager.getBlurSessions().forEach(s -> addFile(new SessionFile(this, s)));
        }
    }

    private final class SessionFile extends SimpleReportFile {

        public SessionFile(BlurContext context, BlurSession blurSession) {
            super(context, blurSession.getName().replaceAll("\\s+", "-"), blurSession.getName());

            append("state", blurSession.getState());
            append("started", blurSession.isStarted());
            append("paused", blurSession.isPaused());
            append("on_stop_task_count", blurSession.getOnStopTasks().size());
            append("player_count", blurSession.getPlayers().size());
            if (blurSession instanceof WorldBlurSession) {
                WorldBlurSession wbs = (WorldBlurSession) blurSession;
                append("world_name", wbs.getWorld().getName());
            }
            append("children_session", blurSession.getChildrenSessions().stream()
                .map(BlurSession::getName).collect(Collectors.toList()));

            append("modules", blurSession.getModuleManager().getModules().values().stream()
                .map(this::moduleToString).filter(Objects::nonNull).collect(Collectors.toList()));
        }

        private Object moduleToString(Module module) {
            // Only log internal modules if the report level is >= VERBOSE and they extend SupervisorContext.
            if (getReportLevel() < ReportLevel.VERBOSE
                && (module.getClass().getDeclaredAnnotation(InternalModule.class) != null
                && !(module instanceof SupervisorContext))) {
                return null;
            }
            LinkedHashMap<Object, Object> map = moduleInfoToString(module.getClass(), module.getModuleInfo());

            if (module instanceof SupervisorContext) {
                Amendable amendable = new SimpleAmendable();
                ((SupervisorContext) module).run(amendable); // Fill amendable
                map.put("data", amendable);
            }
            return map;
        }

        private Object moduleToString(Class<? extends Module> moduleClass) {
            // Only log internal modules if the report level is >= VERBOSE.
            if (getReportLevel() < ReportLevel.VERBOSE && moduleClass.getDeclaredAnnotation(InternalModule.class) != null) {
                return null;
            }
            return moduleInfoToString(moduleClass, moduleClass.getDeclaredAnnotation(ModuleInfo.class));
        }

        private LinkedHashMap<Object, Object> moduleInfoToString(Class<? extends Module> clazz, ModuleInfo info) {
            if (info == null) {
                return null;
            }
            return MapBuilder.newLinkedHashMap()
                .put("name", info.name())
                .put("module_class", clazz.getName())
                .put("data_class", info.dataClass().getName())
                .put("load", info.load())
                .put("depends", Arrays.stream(info.depends()).map(this::moduleToString).collect(Collectors.toList()))
                .build();
        }
    }
}
