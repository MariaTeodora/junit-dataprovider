plugins {
    `dataprovider-library`
    `integration-test`
}

val deps: Dependencies by rootProject.extra

base {
    archivesBaseName = "junit-jupiter-params-dataprovider"
    description = "A TestNG like dataprovider runner for JUnit Jupiter Parameterized Tests which is largely compatible to JUnit4 dataprovider."
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    "api"(project(":core"))
    "api"(deps.junitJupiterEngine)
    "api"(deps.junitJupiterParams)

    "testImplementation"(deps.assertJ8)
    "testImplementation"(deps.mockito8)
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-parameters"))
    }

    jar {
        manifest {
            attributes(
                    "Automatic-Module-Name" to "com.tngtech.junit.dataprovider.jupiter.params"
            )
        }
    }

    test {
        useJUnitPlatform()
    }
}
