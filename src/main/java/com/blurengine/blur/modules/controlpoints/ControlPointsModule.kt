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

package com.blurengine.blur.modules.controlpoints

import com.blurengine.blur.Blur
import com.blurengine.blur.events.players.PlayerMoveBlockEvent
import com.blurengine.blur.framework.Module
import com.blurengine.blur.framework.ModuleData
import com.blurengine.blur.framework.ModuleInfo
import com.blurengine.blur.framework.ModuleManager
import com.blurengine.blur.framework.SerializedModule
import com.blurengine.blur.framework.WorldModule
import com.blurengine.blur.framework.ticking.Tick
import com.blurengine.blur.modules.controlpoints.ControlPointsModule.ControlPointEntry
import com.blurengine.blur.modules.controlpoints.ControlPointsModule.ControlPointsData
import com.blurengine.blur.modules.extents.AutoCircleExtent.MutableAutoCircleExtent
import com.blurengine.blur.modules.extents.CylinderExtent
import com.blurengine.blur.modules.extents.Extent
import com.blurengine.blur.modules.filters.Filter
import com.blurengine.blur.modules.filters.Filters
import com.blurengine.blur.modules.teams.BlurTeam
import com.blurengine.blur.session.BlurPlayer
import com.supaham.commons.relatives.RelativeDuration
import com.supaham.commons.relatives.RelativeNumber
import org.bukkit.ChatColor
import org.bukkit.Effect
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.util.Vector
import pluginbase.config.annotation.Name
import java.time.Duration
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.Optional

@ModuleInfo(name = "BControlPoints", dataClass = ControlPointsData::class)
class ControlPointsModule(manager: ModuleManager, val data: ControlPointsData) : WorldModule(manager) {

    private var controlPoints = ArrayList<ControlPoint>()
    private var playerControlPoints = HashMap<BlurPlayer, ControlPoint>() // Serves as a cache of players inside a control point

    init {
        data.controlPoints.forEach {
            controlPoints.add(ControlPoint(this, it))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMoveBlock(event: PlayerMoveBlockEvent) {
        val controlPoint = playerControlPoints[event.blurPlayer]
        if (controlPoint != null) {
            // Player is still inside the capture point, terminate code
            if (controlPoint.captureExtent.contains(event.blurPlayer.location)) return

            // Player is no longer in their previous control point, remove them from the cache.
            controlPoint.removePlayer(event.blurPlayer)
            playerControlPoints.remove(event.blurPlayer)
            this.session.callEvent(ControlPointExitEvent(event.blurPlayer, controlPoint))
        }

        // If the player is in a control point, cache it.
        getControlPoint(event.to.toVector()).ifPresent {
            it.addPlayer(event.blurPlayer)
            playerControlPoints.put(event.blurPlayer, it)
            this.session.callEvent(ControlPointEnterEvent(event.blurPlayer, it))
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        if (logger.debugLevel == 0) return
        if (event.message.startsWith(".neutralise")) {
            if (!Blur.isDev(event.player)) return

            event.isCancelled = true
            if (event.message.split(" ", limit = 2).size < 2) {
                event.player.sendMessage("Supply control point...")
                return
            }
            var cpName = event.message.split(" ", limit = 2)[1]
            val cp = controlPoints.find { it.id.equals(cpName) || it.name.equals(cpName) }
            if (cp != null) {
                cp.owner = null
                event.player.sendMessage("${ChatColor.GREEN}Neutralised ${ChatColor.YELLOW}$cpName ${ChatColor.GREEN}.")
            } else {
                event.player.sendMessage("${ChatColor.RED} $cpName is not a valid control point.")
            }
        }
    }

    fun getControlPoint(vector: Vector): Optional<ControlPoint> {
        return Optional.ofNullable(controlPoints.find { it.captureExtent.contains(vector) })
    }

    fun getPlayerControlPoint(blurPlayer: BlurPlayer): Optional<ControlPoint> {
        return Optional.ofNullable(playerControlPoints[blurPlayer])
    }

    /*
     * DELEGATES
     */

    fun getPoints() = data.points
    fun getCaptureTime() = data.captureTime
    fun getTimeMultiplier() = data.timeMultiplier
    fun getCaptureRule() = data.captureRule
    fun isIncremental() = data.incremental
    fun isPermanent() = data.permanent
    fun getInitialOwner() = data.initialOwner
    fun getVisualMaterials() = data.visualMaterials

    class ControlPointsData : CommonData(), ModuleData {
        @Name("control-points") var controlPoints = ArrayList<ControlPointEntry>()

        override fun parse(moduleManager: ModuleManager, serialized: SerializedModule): Module? {
            // Defaults are set here instead of CommonData to prevent the assumption that ControlPoints is always overriding the common data. 
            points = RelativeNumber.ZERO
            captureTime = RelativeDuration.ZERO
            timeMultiplier = RelativeNumber.ZERO
            captureRule = CaptureRule.EXCLUSIVE
            incremental = false
            permanent = false
            initialOwner = null
            visualMaterials = Filters.ALWAYS_ALLOW


            serialized.load(this)
            check(controlPoints.isNotEmpty(), "At least one control point must be specified.")
            controlPoints.forEach {
                checkNotNullLateInit({ it.id }, "All control points need to specify an id.")
                checkNotNull({ it.capture }, "${it.id} must specify a capture extent.")
                if (it.name.isNullOrEmpty()) {
                    it.name = it.id
                }
            }
            return ControlPointsModule(moduleManager, this)
        }
    }

    open class CommonData {
        var points: RelativeNumber? = null
        @Name("capture-time") var captureTime: RelativeDuration? = null
        @Name("time-multiplier") var timeMultiplier: RelativeNumber? = null
        @Name("capture-rule") var captureRule: CaptureRule? = null
        var incremental: Boolean? = null
        var permanent: Boolean? = null
        @Name("initial-owner") var initialOwner: BlurTeam? = null
        @Name("visual-materials") var visualMaterials: Filter? = null
        var particles = true
    }

    class ControlPointEntry : CommonData() {
        lateinit var id: String
        var name: String? = null
        lateinit var capture: Extent
        var progress: Extent? = null
    }
}

/**
 * Represents an enum of rules for capturing a control point.
 */
enum class CaptureRule {
    /**
     * To capture a control point, only one team may be on the control point at a time.
     */
    EXCLUSIVE,
    /**
     * To capture a control point, a team must have more players than all other teams combined on the same control point.
     */
    MAJORITY,
    /**
     * To capture a control point, a team must have more players than any one single team on the same control point.
     */
    LEAD;
}

/**
 * Represents a control point in a [ControlPointsModule]. A few things to note, most importantly all logic is done via private setters. [progress]
 * will never be greater than 0, or less than 0.
 */
class ControlPoint(val module: ControlPointsModule, private val data: ControlPointEntry) {

    val points: Double
    val captureDuration: Duration
    val timeMultiplier: Double
    val captureRule: CaptureRule
    val incremental: Boolean
    val permanent: Boolean
    val initialOwner: BlurTeam?
    val visualMaterial: Filter
    val id: String
    val name: String
    val captureExtent: Extent
    val progressExtent: Extent?
    val particles: Boolean

    private val teamManager = module.teamManager
    private val _players: MutableSet<BlurPlayer> = HashSet()
    val players: MutableSet<BlurPlayer>
        get() = Collections.unmodifiableSet(_players)

    private var _owner: BlurTeam? = null
    var owner: BlurTeam?
        get() = _owner
        set(value) {
            _owner = value
            progress.setOwner(value)
        }
    private lateinit var progress: Progress

    init {
        points = handleRelNumber(data.points, module.getPoints()!!)
        captureDuration = handleRelDuration(data.captureTime, module.getCaptureTime()!!)
        timeMultiplier = handleRelNumber(data.timeMultiplier, module.getTimeMultiplier()!!)
        captureRule = data.captureRule ?: module.getCaptureRule()!!
        incremental = data.incremental ?: module.isIncremental()!!
        permanent = data.permanent ?: module.isPermanent()!!
        initialOwner = data.initialOwner ?: module.getInitialOwner()
        visualMaterial = data.visualMaterials ?: module.getVisualMaterials()!!
        id = data.id
        name = data.name!!
        captureExtent = data.capture
        progressExtent = data.progress
        particles = data.particles

        progress = Progress(1F / module.session.millisecondsToTicks(captureDuration.toMillis()))

        progress.setOwner(initialOwner)
        module.addTickable(progress)
    }

    private fun handleRelNumber(rel: RelativeNumber?, moduleRel: RelativeNumber): Double {
        if (rel == null) return moduleRel.number
        return if (rel.isRelative) rel.apply(moduleRel.number) else rel.number
    }

    private fun handleRelDuration(rel: RelativeDuration?, moduleRel: RelativeDuration): Duration {
        if (rel == null) return moduleRel.duration
        return if (rel.isRelative) rel.apply(moduleRel.duration) else rel.duration
    }

    fun addPlayer(blurPlayer: BlurPlayer): Boolean {
        if (_players.add(blurPlayer)) {
            reevaluate()
            return true
        }
        return false
    }

    fun removePlayer(blurPlayer: BlurPlayer): Boolean {
        if (_players.remove(blurPlayer)) {
            reevaluate()
            return true
        }
        return false
    }

    private fun reevaluate() {
        // No players to capture this control point, terminate code
        if (_players.isEmpty()) {
            progress.resetProgress() // Ensure we clear any previous progress data
            return
        }
        val countTeams = countTeams()

        // Only one team is capturing, no need to check capture rules.
        if (countTeams == 1) {
            progress.capturingTeam = teamManager.getPlayerTeam(_players.first())
            return
        }

        when (captureRule) {
        /*
         * EXCLUSIVE
         */
            CaptureRule.EXCLUSIVE -> {
                // If there's more than one team capturing, halt progress but don't reset it
                if (countTeams > 1) {
                    progress.capturing = false
                    return
                }
                progress.capturingTeam = teamManager.getPlayerTeam(_players.first())
            }
        /*
         * MAJORITY
         */
            CaptureRule.MAJORITY -> {
                val teamSizes = ArrayList<Pair<BlurTeam, Int>>()
                teamManager.teams.map { teamSizes.add(Pair(it, it.playerCount)) }
                var found: BlurTeam? = null
                teamSizes.forEach { it1 ->
                    val sum = teamSizes.filter { it.first != it1.first }.sumBy { it.second }
                    found = if (it1.first.playerCount > sum) it1.first else null
                }
                if (found != null) {
                    progress.capturingTeam = found
                } else {
                    progress.capturingTeam = null
                }
            }
        /*
         * LEAD
         */
            CaptureRule.LEAD -> {
                val teamSizes = ArrayList<Pair<BlurTeam, Int>>()
                teamManager.teams.map { teamSizes.add(Pair(it, it.playerCount)) }
                val largest = ArrayList<Pair<BlurTeam, Int>>()
                teamSizes.forEach {
                    if (largest.isEmpty()) {
                        largest.add(it)
                    } else {
                        largest.forEach { it2 ->
                            if (it.second >= it2.second) {
                                if (it.second > it2.second) {
                                    // clear current largest if there's a larger pair
                                    largest.clear()
                                }
                                largest.add(it)
                                return
                            }
                        }
                    }
                }
                if (largest.size == 1) {
                    progress.capturingTeam = largest[0].first
                } else {
                    // 2 or more teams with the most yet same amount of players
                    progress.capturingTeam = null
                }
            }
        }
    }

    private fun countTeams(): Int {
        return _players.map { teamManager.getPlayerTeam(it) }.distinct().count()
    }

    /**
     * @param progressIncr how much to increment per tick
     */
    private inner class Progress(val progressIncr: Float) {
        var timesCaptured: Int = 0
        var capturing: Boolean = false
            set(value) {
                if (value && permanent && timesCaptured >= 1) {
                    return
                }
                field = value
            }
        /**
         * Specifies the team currently capturing this control point. Null means this control point isn't being captured.
         */
        var capturingTeam: BlurTeam? = null
            internal set(value) {
                if (value == owner) {
                    return
                }
                field = value
                capturing = value != null // Automatically set the state of capturing, can still be overridden in other areas.

                // Control point isn't captured and this control point doesn't retain progress, reset progress
                if (value == null && progress < 1F && !incremental) {
                    progress = if (owner != null) 1F else 0F // Reset progress back to 1 if owned, or 0 if unowned. 
                }
            }
        var progressTeam: BlurTeam? = null
        private var _progress: Float = 0F
        var progress: Float
            get() = _progress
            private set(value) {
                _progress = Math.max(0F, Math.min(value, 1F)) // min value of 0, max value of 1
                module.session.callEvent(ControlPointProgressTickEvent(this@ControlPoint))
                module.logger.finest("Progress $_progress progressTeam ${progressTeam?.id} captureTeam ${capturingTeam?.id} " +
                        "owner ${_owner?.id} players ${_players.size}")
            }

        private val particlesExtent = if (captureExtent is CylinderExtent && particles)
            MutableAutoCircleExtent(captureExtent.base.add(0.0, 0.2, 0.0).toVector(), captureExtent.radius, 20, 0.0) else null

        @Tick
        fun tick() {
            // TODO add support for progress extent
            showParticles()
            if (!capturing) {
                return
            }
            // The team capturing this control point has not done this progress, neutralise it first.
            if ((owner != null && owner != capturingTeam) || (progressTeam != null && capturingTeam != progressTeam)) {
                progress -= progressIncr
                // No other code needs to be executed when progressing towards neutralising a point.
                if (progress > 0) {
                    return
                }
                // This else-if is what makes a control point neutral from a state of ownership.
                else if (progress == 0F) {
                    module.session.callEvent(ControlPointProgressResetEvent(this@ControlPoint))

                    if (owner != null) {
                        var previousOwner = owner!!
                        setOwner(null)
                        module.session.callEvent(ControlPointLostEvent(this@ControlPoint, previousOwner))
                    }
                }
            }
            // No one owns/has progress on this control point, begin progress for capturingTeam
            else {
                progressTeam = capturingTeam
                progress += progressIncr
                // The capturingTeam has fully progressed, capture the point.
                if (progress == 1F) {
                    setOwner(progressTeam)
                    module.session.callEvent(ControlPointCapturedEvent(this@ControlPoint))
                    timesCaptured++
                }
            }
        }

        fun showParticles() {
            if (particlesExtent == null) return
            var radius = progress * (captureExtent as CylinderExtent).radius

            if (radius <= 0) {
                return
            }

            particlesExtent.radius = radius
            particlesExtent.offsetRadians = progress * (Math.PI * 2)
            particlesExtent.regenerate()
            particlesExtent.pointsList.forEach {
                module.world.spigot().playEffect(it.toLocation(module.world), Effect.COLOURED_DUST, 0, 0, 0f, 0f, 0f, 0f, 2, 64)
            }
        }

        fun setOwner(blurTeam: BlurTeam?) {
            if (owner?.equals(blurTeam) ?: false) return

            // Reset all capturing variables
            progressTeam = null
            capturingTeam = null
            capturing = false

            _owner = blurTeam
            _progress = if (blurTeam == null) 0F else 1F
        }

        fun resetProgress() {
            if (capturingTeam == null || owner == capturingTeam) {
                // Progress already reset or
                // the capturing team is the owner, so there's nothing to reset.
                return
            }

            if (!incremental) {
                progress = 0F
                progressTeam = null
                module.session.callEvent(ControlPointProgressResetEvent(this@ControlPoint))
            }
            capturingTeam = null
            capturing = false
        }
    }
}
