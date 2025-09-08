plugins {
    java
}

group = "com.example"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.runelite.net")
}

val runeLiteVersion = "1.11.16-SNAPSHOT"

dependencies {
    compileOnly(group = "net.runelite", name = "client", version = runeLiteVersion)

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("junit:junit:4.12")
    testImplementation(group = "net.runelite", name = "client", version = runeLiteVersion)
    testImplementation(group = "net.runelite", name = "jshell", version = runeLiteVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(11)
}