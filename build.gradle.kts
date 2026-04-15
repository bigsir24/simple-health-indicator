plugins {
	alias(libs.plugins.loom)
	java
}

val osName: String = System.getProperty("os.name").lowercase().replace(" ", "")
val lwjglNativeList = arrayOf("macos", "windows", "linux")
val lwjglNativesName = "natives-${lwjglNativeList.find { it in osName }}"

val modGroup: String = providers.gradleProperty("mod_group").get()
val modName: String = providers.gradleProperty("mod_name").get()
val modVersion: String = providers.gradleProperty("mod_version").get() + "+7.4-nightly-${libs.versions.bta.get()}"

val javaVersion: Int = libs.versions.java.map { it.toInt() }.get()

base.archivesName = modName
group = modGroup
version = modVersion

loom {
	customMinecraftMetadata.set("https://downloads.betterthanadventure.net/bta-client/${libs.versions.btaChannel.get()}/${libs.versions.bta.get()}/manifest.json")
}

repositories {
	mavenCentral()
	maven("https://maven.fabricmc.net/") { name = "Fabric" }
	maven("https://maven.thesignalumproject.net/infrastructure") { name = "SignalumMavenInfrastructure" }
	maven("https://maven.thesignalumproject.net/releases") { name = "SignalumMavenReleases" }
	ivy("https://github.com/Better-than-Adventure") {
		patternLayout { artifact("[organisation]/releases/download/[revision]/[module]-bta-[revision].jar") }
		metadataSources { artifact() }
	}
	ivy("https://downloads.betterthanadventure.net/bta-client/${libs.versions.btaChannel.get()}/") {
		patternLayout { artifact("/v[revision]/client.jar") }
		metadataSources { artifact() }
	}
	ivy("https://downloads.betterthanadventure.net/bta-server/${libs.versions.btaChannel.get()}/") {
		patternLayout { artifact("/v[revision]/server.jar") }
		metadataSources { artifact() }
	}
	ivy("https://piston-data.mojang.com") {
		patternLayout { artifact("v1/[organisation]/[revision]/[module].jar") }
		metadataSources { artifact() }
	}
}

dependencies {
	minecraft("::${libs.versions.bta.get()}")

	runtimeOnly(libs.clientJar)

	implementation(libs.loader)
	implementation(libs.legacyLwjgl)

	implementation(libs.slf4jApi)
	implementation(libs.guava)
	implementation(libs.log4j.slf4j2.impl)
	implementation(libs.log4j.core)
	implementation(libs.log4j.api)
	implementation(libs.log4j.api12)
	implementation(libs.gson)

	val lwjglVer = libs.versions.lwjgl.get()
	implementation(platform("org.lwjgl:lwjgl-bom:${lwjglVer}"))

	runtimeOnly("org.lwjgl:lwjgl::$lwjglNativesName")
	runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNativesName")
	runtimeOnly("org.lwjgl:lwjgl-openal::$lwjglNativesName")
	runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNativesName")
	runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNativesName")
	implementation("org.lwjgl:lwjgl:${lwjglVer}")
	implementation("org.lwjgl:lwjgl-glfw:${lwjglVer}")
	implementation("org.lwjgl:lwjgl-openal:${lwjglVer}")
	implementation("org.lwjgl:lwjgl-opengl:${lwjglVer}")
	implementation("org.lwjgl:lwjgl-stb:${lwjglVer}")

	implementation(libs.commonsLang3)
}

java {
	sourceCompatibility = JavaVersion.toVersion(javaVersion)
	targetCompatibility = JavaVersion.toVersion(javaVersion)
	withSourcesJar()
}

val licenseFile = run {
	val rootLicense = layout.projectDirectory.file("LICENSE")
	val parentLicense = layout.projectDirectory.file("../LICENSE")
	when {
		rootLicense.asFile.exists() -> {
			logger.lifecycle("Using LICENSE from project root: {}", rootLicense.asFile)
			rootLicense
		}
		parentLicense.asFile.exists() -> {
			logger.lifecycle("Using LICENSE from parent directory: {}", parentLicense.asFile)
			parentLicense
		}
		else -> {
			logger.warn("No LICENSE file found in project or parent directory.")
			null
		}
	}
}

tasks {
	withType<JavaCompile>().configureEach {
		options.encoding = "UTF-8"
		sourceCompatibility = javaVersion.toString()
		targetCompatibility = javaVersion.toString()
		if (javaVersion > 8) options.release = javaVersion
	}

	withType<JavaExec>().configureEach { defaultCharacterEncoding = "UTF-8" }
	withType<Javadoc>().configureEach { options.encoding = "UTF-8" }
	withType<Test>().configureEach { defaultCharacterEncoding = "UTF-8" }
	withType<Jar>().configureEach {
		licenseFile?.let {
			from(it) {
				rename { original -> "${original}_${archiveBaseName.get()}" }
			}
		}
	}

	processResources {
		val resourceMap = mapOf(
			"version" to modVersion,
			"fabricloader" to libs.versions.loader.get(),
			"java" to libs.versions.java.get(),
		)
		inputs.properties(resourceMap)
		filesMatching("fabric.mod.json") { expand(resourceMap) }
		filesMatching("**/*.mixins.json") { expand(resourceMap.filterKeys { it == "java" }) }
	}
}

// Removes LWJGL2 dependencies
configurations.configureEach {
	exclude(group = "org.lwjgl.lwjgl")
}
