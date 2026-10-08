package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.data.SafeZoneCategory
import com.brokencoders.narisuraksha.data.SafeZonesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeZonesRepositoryTest {

    @Test
    fun testPreCachedSafeZonesNotEmpty() {
        val zones = SafeZonesRepository.preCachedSafeZones
        assertTrue(zones.isNotEmpty())
        assertTrue(zones.size >= 5)

        // Verify each zone has valid coordinates and names
        for (zone in zones) {
            assertTrue(zone.id.isNotEmpty())
            assertTrue(zone.name.isNotEmpty())
            assertTrue(zone.lat in -90.0..90.0)
            assertTrue(zone.lon in -180.0..180.0)
        }
    }

    @Test
    fun testCategoryCoverage() {
        val zones = SafeZonesRepository.preCachedSafeZones
        val categories = zones.map { it.category }.toSet()

        assertTrue(categories.contains(SafeZoneCategory.POLICE))
        assertTrue(categories.contains(SafeZoneCategory.HOSPITAL))
        assertTrue(categories.contains(SafeZoneCategory.WOMEN_HELP_DESK))
        assertTrue(categories.contains(SafeZoneCategory.METRO_SECURITY))
    }

    @Test
    fun testHaversineDistanceAccuracy() {
        // Zero distance
        val dist0 = SafeZonesRepository.calculateDistanceKm(28.0, 77.0, 28.0, 77.0)
        assertEquals(0.0, dist0, 0.0001)

        // Distance between CP and Parliament Street Police Station (~1.5 km)
        val dist = SafeZonesRepository.calculateDistanceKm(28.6304, 77.2177, 28.6238, 77.2115)
        assertTrue(dist in 0.8..2.0)
    }

    @Test
    fun testCompassBearingCalculation() {
        // Heading directly North: lat increases, lon constant -> ~0 degrees
        val bearingNorth = SafeZonesRepository.calculateBearing(28.0, 77.0, 29.0, 77.0)
        assertEquals(0f, bearingNorth, 2.0f)

        // Heading directly East: lat constant, lon increases -> ~90 degrees
        val bearingEast = SafeZonesRepository.calculateBearing(28.0, 77.0, 28.0, 78.0)
        assertEquals(90f, bearingEast, 2.0f)

        // Heading directly South: lat decreases, lon constant -> ~180 degrees
        val bearingSouth = SafeZonesRepository.calculateBearing(28.0, 77.0, 27.0, 77.0)
        assertEquals(180f, bearingSouth, 2.0f)

        // Heading directly West: lat constant, lon decreases -> ~270 degrees
        val bearingWest = SafeZonesRepository.calculateBearing(28.0, 77.0, 28.0, 76.0)
        assertEquals(270f, bearingWest, 2.0f)
    }
}
