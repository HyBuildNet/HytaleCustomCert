plugins {
    java
}

group = "net.hybuild"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Javassist for bytecode manipulation - bundled in output JAR
    implementation("org.javassist:javassist:3.30.2-GA")

    // HytaleServer.jar for compilation only (not bundled)
    compileOnly(files("lib/HytaleServer.jar"))
}

sourceSets {
    main {
        java {
            srcDirs("src")
        }
        resources {
            srcDirs("resources")
        }
    }
}

// Fat JAR task that bundles Javassist
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Creates a fat JAR with Javassist bundled"

    archiveBaseName.set("CustomCert")
    archiveVersion.set("")
    archiveClassifier.set("")

    destinationDirectory.set(file("build/latest"))

    // Include compiled classes
    from(sourceSets.main.get().output)

    // Bundle Javassist classes (excluding its META-INF)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.contains("javassist") }
            .map { zipTree(it) }
    }) {
        exclude("META-INF/**")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Make 'build' task also create the fat JAR
tasks.named("build") {
    dependsOn("fatJar")
}

// Clean task already handles build/ directory by default
tasks.named<Delete>("clean") {
    delete("out")
    delete("lib/HytaleServer")
}
