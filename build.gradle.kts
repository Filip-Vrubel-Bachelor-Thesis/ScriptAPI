plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "qupath.ext"
version = "0.1.0"

val qupathLibDir = providers.gradleProperty("qupathLibDir")
    .orElse(providers.environmentVariable("QUPATH_LIB_DIR"))
    .orElse("${rootDir}/../QuPath-v0.6.0-Linux/QuPath/lib/app")
    .get()

repositories {
    mavenCentral()
    flatDir {
        // QuPath core JAR — provides PathObject and related types
        dirs(qupathLibDir)
    }
}

dependencies {
    // QuPath core types (PathObject, etc.) — compile-only, provided by QuPath at runtime
    compileOnly(":qupath-core-0.6.0")
    compileOnly("org.slf4j:slf4j-api:1.7.36")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to version
        )
    }
}
