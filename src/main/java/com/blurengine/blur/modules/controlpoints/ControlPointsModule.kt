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
import com.blurengine.blur.events.players.PlayerLeaveSessionEvent
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
import com.blurengine.blur.utils.allMaxBy
import com.blurengine.blur.utils.getTeam
import com.google.common.collect.ImmutableList
import com.supaham.commons.relatives.RelativeDuration
import com.supaham.commons.relatives.RelativeNumber
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerMoveEvent
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
    fun onPlayerMove(event: PlayerMoveEvent) {
        val blurPlayer = getPlayer(event.player)
        val controlPoint = playerControlPoints[blurPlayer]
        if (controlPoint != null) {
            // Player is still inside the capture point, terminate code
            if (controlPoint.captureExtent.contains(blurPlayer.player.location)) return

            // Player is no longer in their previous control point, remove them from the cache.
            controlPoint.removePlayer(blurPlayer)
            playerControlPoints.remove(blurPlayer)
            this.session.callEvent(ControlPointExitEvent(blurPlayer, controlPoint))
        }

        // If the player is in a control point, cache it.
        getControlPoint(event.to!!.toVector()).ifPresent {
            it.addPlayer(blurPlayer)
            playerControlPoints.put(blurPlayer, it)
            this.session.callEvent(ControlPointEnterEvent(blurPlayer, it))
        }
    }

    @EventHandler
    fun onPlayerLeaveSession(event: PlayerLeaveSessionEvent) {
        if (!isSession(event)) return
        val controlPoint = playerControlPoints[event.blurPlayer]
        if (controlPoint != null) {
            controlPoint.removePlayer(event.blurPlayer)
            playerControlPoints.remove(event.blurPlayer)
            this.session.callEvent(ControlPointExitEvent(event.blurPlayer, controlPoint))
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

    fun getControlPoints(): ImmutableList<ControlPoint> {
        return ImmutableList.copyOf(controlPoints)
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
    fun getTimeModifierPerPlayer() = data.timeModifierPerPlayer
    fun getMinCaptureTime() = data.minCaptureTime

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
            timeModifierPerPlayer = RelativeNumber.ZERO
            minCaptureTime = RelativeDuration.ZERO


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

        @Name("time-modifier-per-player") var timeModifierPerPlayer: RelativeNumber? = null
        @Name("min-capture-time") var minCaptureTime: RelativeDuration? = null

        // Using material here because deserialising block data is not trivial.
        @Name("neutral-material") var neutralMaterial: Material? = null
        @Name("team-materials") var teamMaterials: List<TeamMaterialEntry>? = null
        var particles = true
    }

    class ControlPointEntry : CommonData() {
        lateinit var id: String
        var name: String? = null
        lateinit var capture: Extent
        var progress: Extent? = null
        var indicator: Extent? = null
    }

    class TeamMaterialEntry {
        lateinit var id: String
        lateinit var material: Material
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
    val timeModifierPerPlayer: RelativeNumber
    val minCaptureTime: Duration

    private val teamManager = module.teamManager
    private val _players: MutableSet<BlurPlayer> = HashSet()
    val players: MutableSet<BlurPlayer>
        get() = Collections.unmodifiableSet(_players)

    private val indicatorBlocks: List<Block>?

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
        timeModifierPerPlayer = data.timeModifierPerPlayer ?: module.getTimeModifierPerPlayer()!!
        minCaptureTime = handleRelDuration(data.minCaptureTime, module.getMinCaptureTime()!!)

        indicatorBlocks = if (data.indicator != null) {
            data.indicator!!.map { b -> module.world.getBlockAt(b.blockX, b.blockY, b.blockZ) }
                    .filter { b -> b.blockData.material == data.neutralMaterial }
                    .toList()
        } else {
            null
        }

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

    fun getProgress(): Float {
        return progress.progress
    }

    fun getProgressTeam(): BlurTeam? {
        return progress.progressTeam
    }

    fun getCapturingTeam(): BlurTeam? {
        return progress.capturingTeam
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
            if (teamManager.getPlayerTeam(_players.first()) == owner) {
                progress.resetProgress()  // Make sure it doesn't get stuck partway through defending.
            }
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
                val largest = teamManager.teams.allMaxBy { it.playerCount }
                if (largest.size == 1) {
                    progress.capturingTeam = largest[0]
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
                        indicatorBlocks?.forEach { it.blockData = data.neutralMaterial!!.createBlockData() }  // This is null safe because we only get any indicator blocks if neutralBlockData is non-null.
                    }
                    progressTeam = null  // There is no progress to have.
                    reevaluate()
                }
            }
            // No one owns/has progress on this control point, begin progress for capturingTeam
            else {
                progressTeam = capturingTeam

                // Only count players that are making a difference (i.e. if the capturing team has 3 players, and the other has 2, then only 1 of the capturing team players actually matters for this).
                val numCapturingPlayers = 2 * _players.count { it.getTeam() == progressTeam } - _players.size  // Equivalent to progressTeamPlayers - (playersOnPoint - progressTeamPlayers).

                var thisTotalCaptureTime = captureDuration.toMillis().toDouble()  // This is how long it would take to capture the whole thing with this many players on point.
                for (i in 2..numCapturingPlayers) {  // Only modify if there is more than 1 capturing player.
                    thisTotalCaptureTime = timeModifierPerPlayer.apply(thisTotalCaptureTime)
                }

                if (thisTotalCaptureTime < minCaptureTime.toMillis()) {
                    thisTotalCaptureTime = minCaptureTime.toMillis().toDouble()
                }

                // Scale this progress increment based on how much faster capturing would be with this many capturing players.
                val thisProgressIncr = progressIncr * captureDuration.toMillis() / thisTotalCaptureTime

                progress += thisProgressIncr.toFloat()
                // The capturingTeam has fully progressed, capture the point.
                if (progress == 1F) {
                    setOwner(progressTeam)
                    module.session.callEvent(ControlPointCapturedEvent(this@ControlPoint))
                    indicatorBlocks?.forEach { it.blockData = data.teamMaterials?.firstOrNull { m -> m.id == owner!!.id }?.material?.createBlockData() ?: it.blockData }
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

            val particleColor = if (owner == null && 0 < progress && progress < 1) {
                progressTeam!!.color
            } else {
                owner!!.color
            }
            particlesExtent.pointsList.forEach {
                module.world.spawnParticle(Particle.REDSTONE, it.toLocation(module.world), 2, 0.0, 0.0, 0.0, 0.0, Particle.DustOptions(particleColor, 1F))
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
                progress = 1F  // Set it to full progress for the owner so it doesn't get stuck in the middle
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
