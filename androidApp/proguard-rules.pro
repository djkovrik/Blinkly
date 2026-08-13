# Blinkly currently relies on consumer rules provided by its dependencies.
# Add narrowly scoped application keep rules here if a runtime-only access path
# cannot be inferred by R8.

# The shared SQLDelight graph also exposes the JVM SQLite JDBC implementation.
# Its optional JDBC 4.2 type is not available on Android and is not used by the
# Android driver.
-dontwarn java.sql.JDBCType

# KMPAuth 2.x does not publish the narrow consumer rules added upstream in 3.x.
# Credential Manager discovers the Play Services provider reflectively, and
# GoogleIdTokenCredential is reconstructed from a Bundle by class name.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
    *;
}
-keep class com.google.android.libraries.identity.googleid.** { *; }
