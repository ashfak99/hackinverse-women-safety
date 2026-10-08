package com.brokencoders.narisuraksha

import com.brokencoders.narisuraksha.data.EmergencyContactEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmergencyContactEntityTest {

    @Test
    fun testDefaultValues() {
        val contact = EmergencyContactEntity(
            name = "Aarti Sharma",
            phoneNumber = "+919876543210",
            relationship = "Mother"
        )

        assertEquals(0L, contact.id)
        assertEquals("Aarti Sharma", contact.name)
        assertEquals("+919876543210", contact.phoneNumber)
        assertEquals("Mother", contact.relationship)
        assertFalse(contact.isPrimary)
        assertTrue(contact.createdAt > 0L)
    }

    @Test
    fun testPrimaryFlagMutation() {
        val contact = EmergencyContactEntity(
            id = 42L,
            name = "Rohan Verma",
            phoneNumber = "9876543211",
            relationship = "Brother",
            isPrimary = true
        )

        assertTrue(contact.isPrimary)

        val updated = contact.copy(isPrimary = false)
        assertFalse(updated.isPrimary)
        assertEquals(42L, updated.id)
    }
}
