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

final class TickFieldGenerated implements BAutoInt, Runnable {

    private static final BAutoInt ZERO_SUPPLIER = () -> 0;

    private final BAutoInt defaultSupplier;
    private final TickField data;

    private int nextTick; // next time to update this value

    private int currentValue; // See get()

    public TickFieldGenerated(BAutoInt defaultSupplier, TickField data) {
        if (defaultSupplier != null) {
            this.defaultSupplier = defaultSupplier;
        } else if (data.initial() != 0) {
            this.defaultSupplier = data::initial;
        } else { // if initial is 0 (default), use static ZERO_SUPPLIER
            this.defaultSupplier = ZERO_SUPPLIER;
        }

        this.data = data;
        this.currentValue = this.defaultSupplier.get();
    }

    // This is what the modules will be receiving.
    @Override
    public int get() {
        return this.currentValue;
    }

    @Override
    public int set(int n) {
        return this.currentValue = n;
    }

    @Override
    public int add(int n) {
        return this.currentValue += n;
    }

    @Override
    public int subtract(int n) {
        return this.currentValue -= n;
    }

    @Override
    public void run() {
        if (--this.nextTick <= 0) {
            if (this.data.increment()) {
                this.currentValue += this.data.amount();
            } else {
                this.currentValue -= this.data.amount();
                if (this.currentValue < 0) {
                    this.currentValue = this.defaultSupplier.get();
                }
            }
            this.nextTick = this.data.value();
        }
    }
}
