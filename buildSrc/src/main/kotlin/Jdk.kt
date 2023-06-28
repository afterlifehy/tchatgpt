import org.gradle.api.JavaVersion

object Jdk {
    val sourceCompatibility = JavaVersion.VERSION_18
    val targetCompatibility = JavaVersion.VERSION_18
    const val jvmTarget = "11"
}