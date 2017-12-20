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

package com.blurengine.blur.components.shared

import com.blurengine.blur.framework.SharedComponent
import com.blurengine.blur.framework.ticking.Tick
import com.blurengine.blur.session.BlurSession
import com.blurengine.blur.utils.elapsed
import com.supaham.commons.bukkit.utils.BlockFaceUtils
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.material.MaterialData
import java.time.Duration
import java.time.Instant
import java.util.Collections

class BlockRestore(session: BlurSession) : SharedComponent(session) {

    private val _blocks = HashMap<Block, BlockRestoreData>()
    val blocks: Map<Block, BlockRestoreData> = Collections.unmodifiableMap(_blocks)

    var restoreOnDisable = false

    init {
        addListener(BlockListener())
    }

    override fun disable() {
        if (restoreOnDisable) {
            restoreAll()
        }
    }

    @Tick
    fun tick() {
        val it = _blocks.values.iterator()
        while (it.hasNext()) {
            val data = it.next()
            if (data.checkExpiry()) {
                it.remove()
            }
        }
    }

    fun restoreAll() {
        val it = _blocks.values.iterator()
        while (it.hasNext()) {
            it.next().restore()
            it.remove()
        }
    }

    fun restore(location: Location) = restore(location.block)

    fun restore(block: Block) {
        _blocks.remove(block)?.restore()
    }

    fun add(block: Block, to: MaterialData, expiry: Duration): Boolean {
        if (block in _blocks) {
            _blocks[block]!!.update(to, expiry)
        } else {
            _blocks[block] = BlockRestoreData(block, to, expiry)
        }
        return true
    }

    fun add(block: Block, to: MaterialData, expiry: Duration, listener: BlockRestoreListener? = null): Boolean {
        if (block in _blocks) {
            _blocks[block]!!.update(to, expiry, listener)
        } else {
            _blocks[block] = BlockRestoreData(block, to, expiry, listener)
        }
        return true
    }

    operator fun contains(block: Block) = _blocks.contains(block)

    operator fun get(block: Block) = _blocks[block]

    private inner class BlockListener : Listener {

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        fun onBlockPhysics(event: BlockPhysicsEvent) {
            if (shouldCancel(event.block)) event.isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        fun onBlockBreak(event: BlockBreakEvent) {
            if (shouldCancel(event.block)) event.isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        fun onBlockSpread(event: BlockSpreadEvent) {
            if (shouldCancel(event.block) || shouldCancel(event.source)) event.isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        fun onBlockForm(event: BlockFormEvent) {
            if (shouldCancel(event.block)) event.isCancelled = true
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        fun onBlockExplode(event: BlockExplodeEvent) {
            event.blockList().removeIf { shouldCancel(it) }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        fun onBlockGrow(event: BlockGrowEvent) {
            if (shouldCancel(event.block)) event.isCancelled = true
        }

        private fun shouldCancel(block: Block): Boolean {
            if (_blocks.containsKey(block)) {
                return true
            }
            return BlockFaceUtils.getAdjacents().any { _blocks.containsKey(block.getRelative(it)) }
        }
    }
}

interface BlockRestoreListener {
    fun onRestore(blockData: BlockRestoreData)
}

class BlockRestoreData(val block: Block, to: MaterialData, expiry: Duration, listener: BlockRestoreListener? = null) {
    var to: MaterialData = to
        private set
    var listener: BlockRestoreListener? = listener
        private set
    var startedAt = Instant.now()
        private set
    var expiry: Duration = expiry
        private set
    val from: BlockState = block.state

    val expired: Boolean get() = startedAt.elapsed(expiry)

    init {
        set()
    }

    fun update(to: MaterialData, expiry: Duration, listener: BlockRestoreListener? = this.listener) {
        if (to != this.to) {
            this.to = to
            set()
        }
        this.expiry = expiry
        startedAt = Instant.now()
        this.listener = listener
    }

    private fun set() {
        this.block.setTypeIdAndData(to.itemType.id, to.data, false)
    }

    fun restore() {
        from.update(true, false)
        this.listener?.onRestore(this)
    }

    fun checkExpiry(): Boolean {
        if (expired) {
            try {
                restore()
            } catch(e: Exception) {
                e.printStackTrace()
            }
            return true
        }
        return false
    }
}
