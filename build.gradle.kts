plugins {
    id("net.minecraftforge.gradle") version "6.0.54"
    id("org.spongepowered.mixin") version "0.7.38"
    jacoco
}

val minecraftVersion = property("minecraft_version") as String
val forgeVersion = property("forge_version") as String
val modId = property("mod_id") as String
val modName = property("mod_name") as String
val modVersion = property("mod_version") as String

fun betterContentJar(repository: String, artifact: String): java.io.File {
    val directory = providers.environmentVariable("BC_CUSTOM_MOD_JAR_DIR").orNull
    require(directory == null || directory.isNotBlank()) { "BC_CUSTOM_MOD_JAR_DIR must not be blank" }
    val jar = if (directory == null) file("../$repository/build/libs/$artifact") else file(directory).resolve(artifact)
    require(jar.isFile) { "Missing Better Content provider $artifact at $jar" }
    return jar
}
val latentJar = betterContentJar("better-chemlib-hazards", "better-chemlib-hazards-0.2.0.jar")
val heatSyncJar = betterContentJar("better-industrial-heat", "better-industrial-heat-0.1.0.jar")

group = "com.bettercontent"
version = modVersion
base { archivesName.set(property("artifact_name") as String) }

java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)); withSourcesJar() }

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://www.cursemaven.com") { content { includeGroup("curse.maven") } }
    maven("https://maven.createmod.net")
    maven("https://maven.ithundxr.dev/mirror")
    maven("https://maven.tterrag.com")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    flatDir { dirs(latentJar.parentFile, heatSyncJar.parentFile) }
}

minecraft {
    mappings("official", minecraftVersion)
    copyIdeResources = true
    runs {
        configureEach {
            workingDirectory(project.file("run"))
            property("forge.logging.console.level", "info")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", file("build/createSrgToMcp/output.srg").absolutePath)
            mods { create(modId) { source(sourceSets.main.get()) } }
        }
        create("client")
        create("server") { arg("--nogui") }
        create("gameTestServer") {
            workingDirectory(project.file("run-gametest"))
            property("forge.enableGameTest", "true")
            property("forge.gameTestServer", "true")
            property("forge.enabledGameTestNamespaces", "$modId,minecraft")
            arg("--nogui")
        }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    compileOnly(fg.deobf("local:better-chemlib-hazards:0.2.0"))
    runtimeOnly(fg.deobf("local:better-chemlib-hazards:0.2.0"))
    runtimeOnly(fg.deobf("local:better-industrial-heat:0.1.0"))
    runtimeOnly("thedarkcolour:kotlinforforge:4.11.0")
    runtimeOnly(fg.deobf("curse.maven:pollution-of-the-realms-269973:8554528"))
    runtimeOnly(fg.deobf("curse.maven:forgeendertech-244844:8554308"))
    compileOnly(fg.deobf("curse.maven:chemlib-340666:5128632"))
    runtimeOnly(fg.deobf("curse.maven:chemlib-340666:5128632"))
    compileOnly(fg.deobf("curse.maven:pneumaticcraft-repressurized-281849:7307654"))
    runtimeOnly(fg.deobf("curse.maven:pneumaticcraft-repressurized-281849:7307654"))
    compileOnly(fg.deobf("com.simibubi.create:create-1.20.1:6.0.8-291:slim"))
    runtimeOnly(fg.deobf("com.simibubi.create:create-1.20.1:6.0.8-291:slim"))
    compileOnly(fg.deobf("net.createmod.ponder:Ponder-Forge-1.20.1:1.0.92"))
    runtimeOnly(fg.deobf("net.createmod.ponder:Ponder-Forge-1.20.1:1.0.92"))
    compileOnly(fg.deobf("dev.engine-room.flywheel:flywheel-forge-api-1.20.1:1.0.5"))
    runtimeOnly(fg.deobf("dev.engine-room.flywheel:flywheel-forge-1.20.1:1.0.5"))
    implementation(fg.deobf("com.tterrag.registrate:Registrate:MC1.20-1.3.3"))
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.4")!!)
    implementation(jarJar("io.github.llamalad7:mixinextras-forge:[0.5.4,0.6.0)")!!)
}

tasks.named<Jar>("jar") { finalizedBy("reobfJar") }
val stageRuntimeJar by tasks.registering(Copy::class) {
    dependsOn(tasks.named("reobfJar"))
    from(layout.buildDirectory.file("reobfJar/output.jar"))
    into(layout.buildDirectory.dir("libs"))
    rename { "${base.archivesName.get()}-$version.jar" }
}
tasks.named("assemble") { dependsOn(stageRuntimeJar) }
tasks.processResources {
    val props = mapOf("mod_id" to modId, "mod_name" to modName, "mod_version" to modVersion, "minecraft_version" to minecraftVersion, "forge_version" to forgeVersion)
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(props) }
}
tasks.withType<Test>().configureEach { useJUnitPlatform(); finalizedBy("jacocoTestReport") }
jacoco { toolVersion = "0.8.12" }
tasks.jacocoTestReport { dependsOn(tasks.test); reports { xml.required.set(true); html.required.set(true) } }
tasks.register("headlessGameTest") { group = "verification"; dependsOn(tasks.named("runGameTestServer")) }
val syncGameTestStructures by tasks.registering(Copy::class) { from("src/main/resources/gameteststructures"); into("run-gametest/gameteststructures") }
tasks.matching { it.name.startsWith("prepareRunGameTestServer") }.configureEach { dependsOn(syncGameTestStructures) }
tasks.register("verifyFast") { group = "verification"; dependsOn(tasks.named("check")) }
tasks.register("verifyFull") { group = "verification"; dependsOn(tasks.named("verifyFast")); dependsOn(tasks.named("headlessGameTest")) }
tasks.withType<JavaCompile>().configureEach { options.release.set(17) }

mixin { add(sourceSets.main.get(), "better_airtight_machines.refmap.json"); config("better_airtight_machines.mixins.json") }
