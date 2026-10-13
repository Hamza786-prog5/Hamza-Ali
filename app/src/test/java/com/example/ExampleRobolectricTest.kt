package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.AdminConfigRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hamza AI", appName)
  }

  @Test
  fun `admin config repository default password and update`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = AdminConfigRepository(context)
    val config = repo.getAdminConfig()
    assertEquals("admin123", config.adminPassword)

    repo.updatePassword("newAdminPass456")
    val updatedConfig = repo.getAdminConfig()
    assertEquals("newAdminPass456", updatedConfig.adminPassword)
  }
}
