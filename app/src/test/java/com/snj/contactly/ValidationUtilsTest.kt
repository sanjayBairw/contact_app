package com.snj.contactly

import com.snj.contactly.utils.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `validateName rejects blank and single character names`() {
        assertNotNull(ValidationUtils.validateName(""))
        assertNotNull(ValidationUtils.validateName("   "))
        assertNotNull(ValidationUtils.validateName("A"))
    }

    @Test
    fun `validateName accepts valid names`() {
        assertNull(ValidationUtils.validateName("Aarav Sharma"))
        assertNull(ValidationUtils.validateName("  Li  "))
    }

    @Test
    fun `validatePhone rejects blank invalid chars and wrong digit counts`() {
        assertNotNull(ValidationUtils.validatePhone(""))
        assertNotNull(ValidationUtils.validatePhone("12345"))
        assertNotNull(ValidationUtils.validatePhone("1234567890123456"))
        assertNotNull(ValidationUtils.validatePhone("abc-123-4567"))
    }

    @Test
    fun `validatePhone accepts formatted international and local numbers`() {
        assertNull(ValidationUtils.validatePhone("+91 98765 43210"))
        assertNull(ValidationUtils.validatePhone("(415) 890-2341"))
        assertNull(ValidationUtils.validatePhone("9876543210"))
    }

    @Test
    fun `validateEmail allows blank and valid emails but rejects malformed ones`() {
        assertNull(ValidationUtils.validateEmail(null))
        assertNull(ValidationUtils.validateEmail(""))
        assertNull(ValidationUtils.validateEmail("   "))
        assertNull(ValidationUtils.validateEmail("clara.bennett@northstar.io"))

        assertNotNull(ValidationUtils.validateEmail("not-an-email"))
        assertNotNull(ValidationUtils.validateEmail("missing@domain"))
        assertNotNull(ValidationUtils.validateEmail("@example.com"))
    }

    @Test
    fun `validateContactForm aggregates field errors accurately`() {
        val invalid = ValidationUtils.validateContactForm("", "123", "bad-email")
        assertFalse(invalid.isValid)
        assertEquals("Full name is required", invalid.nameError)
        assertNotNull(invalid.phoneError)
        assertNotNull(invalid.emailError)

        val valid = ValidationUtils.validateContactForm("Maya Lin", "+1 415 555 0134", "")
        assertTrue(valid.isValid)
        assertNull(valid.nameError)
        assertNull(valid.phoneError)
        assertNull(valid.emailError)
    }
}

