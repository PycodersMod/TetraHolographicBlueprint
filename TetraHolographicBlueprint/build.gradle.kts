import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.file.DuplicatesStrategy
import java.nio.charset.StandardCharsets
import java.util.Base64

plugins {
    java
    id("net.minecraftforge.gradle") version "6.0.54"
    id("org.spongepowered.mixin") version "0.7.+"
}

fun prop(name: String): String = providers.gradleProperty(name).get()

val minecraftVersion = prop("minecraft_version")
val minecraftVersionRange = prop("minecraft_version_range")
val forgeVersion = prop("forge_version")
val forgeVersionRange = prop("forge_version_range")
val javaVersionRange = prop("java_version_range")
val modId = prop("mod_id")
val modName = prop("mod_name")
val modVersion = prop("mod_version")
val modGroupId = prop("mod_group_id")
val modAuthors = prop("mod_authors")
val modDescription = prop("mod_description")
val modLicense = prop("mod_license")

group = modGroupId
version = modVersion

base {
    archivesName.set(modId)
}

val pycodersRunDir = file(
    providers.gradleProperty("pycodersRuntimeDir")
        .orElse("../../runtime/legacy-import/TetraHolographicBlueprint/run")
        .get()
)
fun decodeArgs(name: String): List<String> = providers.gradleProperty(name).orNull?.takeIf { it.isNotEmpty() }?.split('.')?.map { if (it == "_") "" else String(Base64.getDecoder().decode(it), StandardCharsets.UTF_8) } ?: emptyList()
val pycodersGameArgs = decodeArgs("pycodersGameArgsB64")
val pycodersJavaArgs = decodeArgs("pycodersJavaArgsB64")
val pycodersUsername = providers.gradleProperty("pycodersUsername").orElse("Dev").get()
val refMapRemappingFile = file("build/createSrgToMcp/output.srg")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net")
    maven("https://www.cursemaven.com")
}

dependencies {
    minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    implementation(fg.deobf("curse.maven:mutil-351914:6418901"))
    implementation(fg.deobf("curse.maven:tetra-289712:6418957"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}.mixins.json")
}

minecraft {
    mappings("official", minecraftVersion)

    runs {
        create("client") {
            workingDirectory(pycodersRunDir)
            args("--username", pycodersUsername)
            pycodersGameArgs.forEach { args(it) }
            pycodersJavaArgs.forEach { jvmArg(it) }
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", refMapRemappingFile.absolutePath)
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("clientMinimal") {
            parent(null, "client")
            workingDirectory(pycodersRunDir)
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("clientMixinCheck") {
            parent(null, "client")
            workingDirectory(pycodersRunDir)
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("clientMixinMapped") {
            parent(null, "client")
            workingDirectory(pycodersRunDir)
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(pycodersRunDir)
            pycodersGameArgs.forEach { args(it) }
            pycodersJavaArgs.forEach { jvmArg(it) }
            arg("nogui")
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("data") {
            workingDirectory(pycodersRunDir)
            args(
                "--mod", modId,
                "--all",
                "--output", file("src/generated/resources/"),
                "--existing", file("src/main/resources/")
            )
        }
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
}

val copyMixinRefmap = tasks.register<Copy>("copyMixinRefmap") {
    dependsOn(tasks.named("compileJava"))
    from(layout.buildDirectory.file("tmp/compileJava/${modId}.refmap.json"))
    into(layout.buildDirectory.dir("resources/main"))
    mustRunAfter(tasks.named("processResources"))
}

tasks.named("classes") {
    dependsOn(copyMixinRefmap)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val resourceProperties = mapOf(
    "mod_id" to modId,
    "mod_name" to modName,
    "mod_version" to modVersion,
    "mod_authors" to modAuthors,
    "mod_description" to modDescription,
    "mod_license" to modLicense,
    "minecraft_version" to minecraftVersion,
    "minecraft_version_range" to minecraftVersionRange,
    "forge_version" to forgeVersion,
    "forge_version_range" to forgeVersionRange,
    "java_version_range" to javaVersionRange
)

tasks.named<Copy>("processResources") {
    inputs.properties(resourceProperties)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(resourceProperties)
    }
}
