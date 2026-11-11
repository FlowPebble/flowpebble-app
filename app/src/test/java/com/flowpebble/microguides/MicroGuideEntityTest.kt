package com.flowpebble.microguides

import com.flowpebble.microguides.data.local.MicroGuideEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MicroGuideEntityTest {

    @Test
    fun stepMaskToggleAndCountWorkCorrectly() {
        val steps = listOf(
            "Step one: open storage settings",
            "Step two: inspect large downloads",
            "Step three: back up essential photos",
            "Step four: empty trash safely"
        )
        var entity = MicroGuideEntity(
            id = "test-guide",
            title = "Test Micro-Guide",
            category = "Phone & tech",
            emoji = "📱",
            summary = "Verify bitmask step tracking.",
            stepsJson = "//",
            checkedStepsMask = 0
        )

        assertFalse(entity.isStepChecked(0))
        assertEquals(0, entity.checkedStepCount(steps.size))

        entity = entity.withToggledStep(0)
        entity = entity.withToggledStep(2)

        assertTrue(entity.isStepChecked(0))
        assertFalse(entity.isStepChecked(1))
        assertTrue(entity.isStepChecked(2))
        assertEquals(2, entity.checkedStepCount(steps.size))

        entity = entity.withToggledStep(0)
        assertFalse(entity.isStepChecked(0))
        assertEquals(1, entity.checkedStepCount(steps.size))
    }
}
