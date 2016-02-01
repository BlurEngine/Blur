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

package com.blurengine.blur.utils;

import com.google.common.base.Preconditions;

import com.supaham.commons.bukkit.CommonPlugin;
import com.supaham.commons.bukkit.TickerTask;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;


/**
 * Created by Ali on 22/11/2015.
 */
public class TaskBuilder {

    private CommonPlugin plugin;
    private long delay;
    private Long interval;
    private RunnableWithTask runnable; // Use custom interface instead of Runnable to provide generated TickerTask when needed.
    private boolean async;

    public TaskBuilder() {
    }

    public TaskBuilder(@Nonnull CommonPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin cannot be null.");
    }

    public TickerTask build() {
        Preconditions.checkNotNull(plugin, "plugin cannot be null.");
        Preconditions.checkNotNull(runnable, "runnable cannot be null.");
        this.delay = Math.max(this.delay, 0);

        TickerTask task = new TickerTask(this.plugin, this.delay / 50) {
            @Override
            public void run() {
                runnable.run(this);
            }
        };
        if (this.interval != null) {
            task.setInterval(this.interval / 50);
        }
        return task;
    }

    public TaskBuilder plugin(@Nonnull CommonPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin cannot be null.");
        return this;
    }

    public TaskBuilder delay(@Nonnull Duration delay) {
        Preconditions.checkNotNull(delay, "delay cannot be null.");
        return delay(delay.toMillis());
    }

    public TaskBuilder delay(long delay, @Nonnull TimeUnit unit) {
        return delay(TimeUnit.MILLISECONDS.convert(delay, Preconditions.checkNotNull(unit, "unit cannot be null.")));
    }

    public TaskBuilder delay(long delay) {
        this.delay = delay;
        return this;
    }

    public TaskBuilder interval(Duration interval) {
        return interval(interval == null ? null : interval.toMillis());
    }

    public TaskBuilder interval(long interval, @Nonnull TimeUnit unit) {
        Preconditions.checkNotNull(unit, "unit cannot be null.");
        return interval(TimeUnit.MILLISECONDS.convert(interval, unit));
    }

    public TaskBuilder interval(long interval) {
        this.interval = interval;
        return this;
    }

    public TaskBuilder run(@Nonnull Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable cannot be null.");
        this.runnable = (task) -> runnable.run();
        return this;
    }

    public TaskBuilder run(@Nonnull RunnableWithTask runnableWithTask) {
        Preconditions.checkNotNull(runnableWithTask, "runnableWithTask cannot be null.");
        this.runnable = runnableWithTask;
        return this;
    }

    public TaskBuilder async() {
        return async(true);
    }

    public TaskBuilder async(boolean async) {
        this.async = async;
        return this;
    }
    
    /* ================================
     * >> GETTERS
     * ================================ */

    public CommonPlugin getPlugin() {
        return plugin;
    }

    public long getDelay() {
        return delay;
    }

    public Long getInterval() {
        return interval;
    }

    public RunnableWithTask getRunnable() {
        return runnable;
    }

    public boolean isAsync() {
        return async;
    }

    public interface RunnableWithTask {

        void run(TickerTask task); // Yes yes I know, the user has control over task state.
    }
}
