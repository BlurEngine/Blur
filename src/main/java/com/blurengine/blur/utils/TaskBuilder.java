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

import lombok.Getter;
import lombok.NonNull;

/**
 * Created by Ali on 22/11/2015.
 */
@Getter
public class TaskBuilder {

    private CommonPlugin plugin;
    private long delay;
    private Long interval;
    private RunnableWithTask runnable; // Use custom interface instead of Runnable to provide generated TickerTask when needed.
    private boolean async;

    public TaskBuilder() {
    }

    public TaskBuilder(@NonNull CommonPlugin plugin) {
        this.plugin = plugin;
    }

    public TickerTask build() {
        Preconditions.checkNotNull(plugin, "plugin cannot be null.");
        Preconditions.checkNotNull(runnable, "runnable cannot be null.");
        this.delay = Math.max(this.delay, 0);

        TickerTask task = new TickerTask(this.plugin, this.delay / 50) {
            @Override public void run() {
                runnable.run(this);
            }
        };
        if (this.interval != null) {
            task.setInterval(this.interval / 50);
        }
        return task;
    }

    public TaskBuilder plugin(@NonNull CommonPlugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public TaskBuilder delay(@NonNull Duration delay) {
        return delay(delay.toMillis());
    }

    public TaskBuilder delay(long delay, @NonNull TimeUnit unit) {
        return delay(TimeUnit.MILLISECONDS.convert(delay, unit));
    }

    public TaskBuilder delay(long delay) {
        this.delay = delay;
        return this;
    }

    public TaskBuilder interval(Duration interval) {
        return interval(interval == null ? null : interval.toMillis());
    }

    public TaskBuilder interval(Long interval, TimeUnit unit) {
        return interval(interval == null ? null : TimeUnit.MILLISECONDS.convert(interval, unit));
    }

    public TaskBuilder interval(Long interval) {
        this.interval = interval;
        return this;
    }

    public TaskBuilder run(@NonNull Runnable runnable) {
        this.runnable = (task) -> runnable.run();
        return this;
    }

    public TaskBuilder run(@NonNull RunnableWithTask runnableWithTask) {
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

    public interface RunnableWithTask {

        void run(TickerTask task); // Yes yes I know, the user has control over task state.
    }
    
}
