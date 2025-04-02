plugins {
    id("java")
    id("application")
}

group = "come.codesnack"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // JUnit для тестов
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.google.api-client:google-api-client:1.32.1")

    // OAuth бібліотека
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")

    // HTTP клієнт і JSON
    implementation("com.google.http-client:google-http-client-gson:1.43.3")

    // Google Sheets API
    implementation("com.google.apis:google-api-services-sheets:v4-rev612-1.25.0")
}

tasks.test {
    useJUnitPlatform()
}





