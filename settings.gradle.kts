val modName: Provider<String> = providers.gradleProperty("mod_name")
rootProject.name = modName.get()

pluginManagement {
	fun isRepoHealthy(url: String): Boolean {
		var connection: javax.net.ssl.HttpsURLConnection? = null
		return try {
			connection = java.net.URI(url).toURL().openConnection() as javax.net.ssl.HttpsURLConnection
			connection.requestMethod = "HEAD"
			connection.connectTimeout = 2000
			connection.readTimeout = 2000
			connection.connect()
			connection.responseCode in 200..399
		} catch (_: Exception) {
			false
		} finally {
			connection?.disconnect()
		}
	}
	fun repoUrlWithFallbacks(vararg urls: String): String {
		if (urls.isEmpty()) return throw IllegalArgumentException("No urls provided.")

		val chosenRepository = urls.firstOrNull { isRepoHealthy(it) } ?: run {
			logger.error("All {} repositories could not be resolved. Defaulting to: {}", urls.size, urls.first())
			return urls.first()
		}
		logger.lifecycle("Using \"{}\" as the Fabric repository.", chosenRepository)
		return chosenRepository
	}
	repositories {
		maven(
			repoUrlWithFallbacks(
				"https://maven.fabricmc.net",
				"https://maven2.fabricmc.net",
				"https://maven3.fabricmc.net"
			)
		) { name = "Fabric" }
		maven("https://maven.thesignalumproject.net/infrastructure") { name = "SignalumMavenInfrastructure" }
		mavenCentral()
		gradlePluginPortal()
	}
}
