/*
 *
 * DEFCON: Nuclear warfare plugin for minecraft servers.
 * Copyright (c) 2024 mochibit.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mochibit.defcon.effects.nuclear

import com.mochibit.defcon.effects.AnimatedEffect
import com.mochibit.defcon.effects.ParticleComponent
import com.mochibit.defcon.effects.TemperatureComponent
import com.mochibit.defcon.explosions.NuclearComponent
import com.mochibit.defcon.math.Vector3
import com.mochibit.defcon.particles.ExplosionDustParticle
import com.mochibit.defcon.vertexgeometry.particle.ParticleShape
import com.mochibit.defcon.vertexgeometry.shapes.CylinderBuilder
import com.mochibit.defcon.vertexgeometry.shapes.SphereBuilder
import org.bukkit.Location

class NuclearExplosionVFX(private val nuclearComponent: NuclearComponent, val center: Location) : AnimatedEffect(.5, 3600) {
    private val maxHeight = 250.0
    private var currentHeight = 0.0
    private var riseSpeed = 5.0
    private val visibleWhenLessThanCurrentHeight = { value: Double -> value < currentHeight - 5 }

    private val condensationCloudVFX = CondensationCloudVFX(nuclearComponent, center)
    private val coreCloud: ParticleComponent = ParticleComponent(
        ParticleShape(
            ExplosionDustParticle()
                .velocity(Vector3(0.0, 1.5, 0.0)),
            SphereBuilder()
                .withRadiusXZ(30.0)
                .withRadiusY(50.0),
            center
        ),
        TemperatureComponent(temperatureCoolingRate = 100.0)
    )
    private val secondaryCloud: ParticleComponent = ParticleComponent(
        ParticleShape(
            ExplosionDustParticle()
                .scale(Vector3(12.0, 12.0, 12.0))
                .velocity(Vector3(0.0, 1.3, 0.0)),
            SphereBuilder()
                .skipRadiusXZ(30.0)
                .withRadiusXZ(50.0)
                .withRadiusY(50.0),
            center
        ),
        TemperatureComponent(temperatureCoolingRate = 105.0)
    )
    private val tertiaryCloud: ParticleComponent = ParticleComponent(
        ParticleShape(
            ExplosionDustParticle()
                .scale(Vector3(14.0, 14.0, 14.0))
                .velocity(Vector3(0.0, 1.2, 0.0)),
            SphereBuilder()
                .skipRadiusXZ(50.0)
                .withRadiusXZ(70.0)
                .withRadiusY(70.0),
            center
        ),
        TemperatureComponent(temperatureCoolingRate = 110.0)
    )
    private val quaterniaryCloud: ParticleComponent = ParticleComponent(
        ParticleShape(
            ExplosionDustParticle()
                .scale(Vector3(15.0, 15.0, 15.0))
                .velocity(Vector3(0.0, -.8, 0.0)),
            SphereBuilder()
                .skipRadiusXZ(70.0)
                .withRadiusXZ(90.0)
                .withRadiusY(60.0),
            center
        ),
        TemperatureComponent(temperatureCoolingRate = 115.0)
    ).translate(Vector3(0.0, -5.0, 0.0)).emitRate(15)

    private val coreNeck: ParticleComponent = ParticleComponent(
        ParticleShape(
            ExplosionDustParticle()
                .velocity(Vector3(0.0, -1.0, 0.0)),
            CylinderBuilder()
                .withHeight(60.0)
                .withRadiusX(30.0)
                .withRadiusZ(30.0)
                .withRate(20.0)
                .withHeightRate(1.0)
                .hollow(false),
            center
        ),
        TemperatureComponent(temperatureCoolingRate = 95.0)
    ).translate(Vector3(0.0, -30.0, 0.0))

    private val stem: ParticleComponent = ParticleComponent(
        ParticleShape(
            ExplosionDustParticle()
                .scale(Vector3(11.0, 11.0, 11.0))
                .velocity(Vector3(0.0, 2.0, 0.0)),
            CylinderBuilder()
                .withHeight(maxHeight)
                .withRadiusX(15.0)
                .withRadiusZ(15.0)
                .withRate(30.0)
                .hollow(false),
            center
        ).apply{
            yPredicate(visibleWhenLessThanCurrentHeight)
        },
        TemperatureComponent(temperatureCoolingRate = 140.0)
    )
    private val foot: ParticleComponent = ParticleComponent(
        ParticleShape(
            ExplosionDustParticle()
                .velocity(Vector3(0.0, 1.0, 0.0)),
            CylinderBuilder()
                .withHeight(15.0)
                .withRadiusX(30.0)
                .withRadiusZ(30.0)
                .withRate(32.0)
                .hollow(false),
            center
        ),
        TemperatureComponent(temperatureCoolingRate = 175.0)
    )
    private val nuclearFog: ParticleComponent = ParticleComponent(
        ParticleShape(
            ExplosionDustParticle()
                .scale(Vector3(14.0, 14.0, 14.0))
                .displacement(Vector3(.0, .5, .0)),
            SphereBuilder()
                .withRadiusXZ(200.0)
                .withRadiusY(1.0),
            center
        ).apply {
            snapToFloor(70.0, 150.0)
        },
        TemperatureComponent(temperatureCoolingRate = 200.0)
    ).apply {
        applyRadialVelocityFromCenter(Vector3(.5, 0.0, .5))
        emitRate(40)
    }

    init {
        effectComponents = mutableListOf(
            coreCloud,
            secondaryCloud,
            tertiaryCloud,
            quaterniaryCloud,

            coreNeck,

            stem,
            foot,
            nuclearFog
        )
    }

    override fun animate(delta: Double) {
        processRise(delta)
    }

    override fun start() {
        super.start()
        condensationCloudVFX.instantiate(true)
    }

    override fun stop() {
        super.stop()
        condensationCloudVFX.destroy()
    }


    private fun processRise(delta: Double) {
        if (currentHeight > maxHeight) return
        val deltaMovement = riseSpeed * delta
        // Elevate the sphere using transform translation
        coreCloud.translate(Vector3(0.0, deltaMovement, 0.0))
        coreNeck.translate(Vector3(0.0, deltaMovement, 0.0))
        secondaryCloud.translate(Vector3(0.0, deltaMovement, 0.0))
        tertiaryCloud.translate(Vector3(0.0, deltaMovement, 0.0))
        quaterniaryCloud.translate(Vector3(0.0, deltaMovement, 0.0))
        //primaryNeck.transform = primaryNeck.transform.translated(Vector3(0.0, deltaMovement, 0.0));
        currentHeight += deltaMovement
    }

}