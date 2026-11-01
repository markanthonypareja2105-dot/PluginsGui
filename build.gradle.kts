plugins {
    java
}

group = "com.plugingui"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("paperApi").get()}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    val v = project.version
    inputs.property("version", v)
    filesMatching("plugin.yml") {
        expand("version" to v)
    }
}

tasks.jar {
    archiveBaseName.set("PluginGui")
}
