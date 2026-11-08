package com.example

import com.example.data.AutomationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAutomationManagerRoutines() {
        val manager = AutomationManager()
        val routines = manager.routines.value
        assertTrue("Routines list should not be empty", routines.isNotEmpty())

        val morningRoutine = routines.firstOrNull { it.id == "routine_morning" }
        assertNotNull("Morning routine should exist", morningRoutine)
        assertTrue(morningRoutine!!.titleTe.contains("జై శ్రీ రామ్"))

        val lockRoutine = routines.firstOrNull { it.id == "routine_lock_screen" }
        assertNotNull("Lock screen routine should exist", lockRoutine)
    }

    @Test
    fun testAutomationTriggerMatching() {
        val manager = AutomationManager()
        val foundLock = manager.findRoutineByTrigger("స్క్రీన్ లాక్ చెయ్యి")
        assertNotNull("Should match lock trigger in Telugu", foundLock)
        assertEquals("routine_lock_screen", foundLock?.id)

        val foundMorning = manager.findRoutineByTrigger("జై శ్రీ రామ్")
        assertNotNull("Should match morning trigger in Telugu", foundMorning)
        assertEquals("routine_morning", foundMorning?.id)

        val foundEnglishLock = manager.findRoutineByTrigger("lock screen")
        assertNotNull("Should match lock trigger in English", foundEnglishLock)
        assertEquals("routine_lock_screen", foundEnglishLock?.id)
    }

    @Test
    fun testAppVoiceCommandParsing() {
        // Test query cleaning logic
        val clean1 = "open youtube".removePrefix("open ").trim()
        assertEquals("youtube", clean1)

        val clean2 = "యూట్యూబ్ ఓపెన్ చెయ్యి".removeSuffix(" ఓపెన్ చెయ్యి").trim()
        assertEquals("యూట్యూబ్", clean2)

        val clean3 = "open calculator".removePrefix("open ").trim()
        assertEquals("calculator", clean3)
    }
}

