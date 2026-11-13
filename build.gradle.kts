plugins {
    id("java")
}

group = "org.sylvarion"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.nexomc.com/releases")
}

dependencies {
    // Compile against the minimum supported API to keep one jar for 1.21.10 through 26.3.
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.nexomc:nexo:1.30.2") { isTransitive = false }
    compileOnly("it.unimi.dsi:fastutil:8.5.15")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.20")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("net.kyori:adventure-api:4.20.0")
    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.encoding = "UTF-8"
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.test {
    useJUnitPlatform()
}
