package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.KarmaDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Karma Learning Classes", appName)
    }

    @Test
    fun `verify password hashing and salting`() {
        val hash1 = KarmaDatabase.hashPassword("KarmaAdmin@2026")
        val hash2 = KarmaDatabase.hashPassword("KarmaAdmin@2026")
        assertEquals(hash1, hash2)
        assertTrue(hash1.length == 64)
    }
}
