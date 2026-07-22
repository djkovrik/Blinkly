# Blinkly currently relies on consumer rules provided by its dependencies.
# Add narrowly scoped application keep rules here if a runtime-only access path
# cannot be inferred by R8.

# The shared SQLDelight graph also exposes the JVM SQLite JDBC implementation.
# Its optional JDBC 4.2 type is not available on Android and is not used by the
# Android driver.
-dontwarn java.sql.JDBCType
