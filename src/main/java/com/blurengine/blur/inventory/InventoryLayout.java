/*
 * Copyright 2017 Ali Moghnieh
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

package com.blurengine.blur.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import com.supaham.commons.bukkit.utils.InventoryUtils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a player's inventory layout using {@link SlotType}.
 */
public class InventoryLayout {

    private final SlotType[] slotsByType;
    private final ListMultimap<SlotType, Integer> typesToSlots = ArrayListMultimap.create();
    private final Inventory inventory;

    private int validateSlot(int slot) {
        Preconditions.checkArgument(slot >= 0, "slot cannot be smaller than 0.");
        Preconditions.checkArgument(slot <= inventory.getSize(), "slot cannot be larger than 35.");
        return slot;
    }

    /**
     * Constructs a new {@link InventoryLayout}.
     *
     * @param inventory inventory
     */
    public InventoryLayout(Inventory inventory) {
        this.inventory = inventory;
        this.slotsByType = new SlotType[inventory.getSize() + 1];
        // We need to fill using setSlot for more than just the array.
        for (int i = 0; i < inventory.getSize() + 1; i++) {
            setSlot(i, SlotType.EMPTY);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof InventoryLayout && Arrays.equals(slotsByType, ((InventoryLayout) obj).slotsByType);
    }

    /**
     * Gets a {@link List} of all slot indices registered to a {@link SlotType}.
     *
     * @param slotType slot type to get slot indexes for
     * @return a list of slots represented as {@link Integer}
     */
    public List<Integer> getSlotByType(SlotType slotType) {
        return Collections.unmodifiableList(this.typesToSlots.get(slotType));
    }

    /**
     * Gets the first available {@link SlotType} in an {@link Inventory}. This method fails silently if the given {@link SlotType} does not have
     * any registered slots to it in this class.
     *
     * @param slotType  slot type to get slot for
     * @return the first slot registered to the {@code slotType}, if there is none, -1 is returned
     */
    public int getFirstAvailableSlot(SlotType slotType) {
        return getFirstAvailableSlot(slotType, false);
    }

    /**
     * Gets the first available {@link SlotType} in an {@link Inventory}. This method fails silently if the given {@link SlotType} does not have
     * any registered slots to it in this class.
     *
     * @param slotType    slot type to get slot for
     * @param returnFirst whether to return the first slot regardless of whether it's empty
     * @return the first slot registered to the {@code slotType}, if there is none, -1 is returned
     */
    public int getFirstAvailableSlot(SlotType slotType, boolean returnFirst) {
        return getFirstAvailableSlot(slotType, null, returnFirst);
    }

    /**
     * Gets the first available {@link SlotType} in an {@link Inventory}. This method fails silently if the given {@link SlotType} does not have
     * any registered slots to it in this class.
     *
     * @param slotType    slot type to get slot for
     * @param item        item to find slot for
     * @param returnFirst whether to return the first slot regardless of whether it's empty
     * @return the first slot registered to the {@code slotType}, if there is none, -1 is returned
     */
    public int getFirstAvailableSlot(SlotType slotType, ItemStack item, boolean returnFirst) {
        ItemStack[] contents = this.inventory.getContents();
        int first = -1;
        for (Integer integer : this.typesToSlots.get(slotType)) {
            if (first == -1) { // only set the first slot variable, one time.
                first = integer;
            }
            ItemStack currItem = contents[integer];
            if (currItem == null || currItem.getType().equals(Material.AIR)
                || (currItem.isSimilar(item) && currItem.getAmount() < currItem.getMaxStackSize())) {
                return integer;
            }
        }
        return returnFirst ? first : -1;
    }

    public SlotType getTypeBySlot(int slot) {
        return slotsByType[validateSlot(slot)];
    }

    public SlotType clearSlot(int slot) {
        return setSlot(slot, SlotType.EMPTY);
    }

    public SlotType setSlot(int slot, SlotType slotType) {
        if (slotType == null) {
            slotType = SlotType.EMPTY;
        }
        SlotType old = slotsByType[validateSlot(slot)];
        if (old != slotType) {
            this.typesToSlots.remove(old, slot);
            slotsByType[slot] = slotType;
            this.typesToSlots.put(slotType, slot);
        }
        return old;
    }

    public boolean isOfSlotType(int slot, SlotType... slotType) {
        SlotType matched = slotsByType[validateSlot(slot)];
        for (SlotType type : slotType) {
            if (matched.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean addItem(ItemStack item) {
        return addItem(SlotType.EMPTY, item, false);
    }

    public boolean addItem(SlotType slotType, ItemStack item) {
        return addItem(slotType, item, false);
    }

    public boolean addItem(SlotType slotType, ItemStack item, boolean force) {
        int slot = getFirstAvailableSlot(slotType, item, force);
        if (slot != -1) {
            InventoryUtils.addItem(this.inventory, item, slot);
            return true;
        }
        return false;
    }

    public boolean setItem(SlotType slotType, ItemStack item) {
        return setItem(slotType, item, false);
    }

    public boolean setItem(SlotType slotType, ItemStack item, boolean force) {
        int slot = getFirstAvailableSlot(slotType, force);
        if (slot != -1) {
            inventory.setItem(slot, item);
            return true;
        }
        return false;
    }
}
