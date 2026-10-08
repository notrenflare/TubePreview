// Security fixes for libraries used by the Android build tools, not shipped in the app.
buildscript {
  dependencies {
    constraints {
      add("classpath", "org.apache.commons:commons-lang3:3.18.0") {
        because("Fix CVE-2025-48924")
      }
      add("classpath", "org.bitbucket.b_c:jose4j:0.9.6") {
        because("Fix CVE-2024-29371")
      }
      add("classpath", "org.jdom:jdom2:2.0.6.1") {
        because("Fix CVE-2021-33813")
      }
      listOf("bcprov-jdk18on", "bcpkix-jdk18on", "bcutil-jdk18on").forEach { module ->
        add("classpath", "org.bouncycastle:$module:1.86") {
          because("Align Bouncy Castle build libraries with the current security fixes")
        }
      }
    }
  }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.roborazzi) apply false
}
