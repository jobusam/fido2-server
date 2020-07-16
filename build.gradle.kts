plugins {
    java
    application
}

group = "de.busam"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin","javalin","3.8.0")
    implementation("com.auth0","java-jwt","3.10.3")
    implementation("org.slf4j","slf4j-simple","1.7.28")
    // Data Serialization
    implementation("com.fasterxml.jackson.core","jackson-core","2.11.0")
    implementation("com.fasterxml.jackson.core","jackson-databind","2.11.0")
    // Webjars for web frontend
    implementation("org.webjars.npm","vue","2.6.11")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_14
}

// compile with preview functions tu support data records
tasks.compileJava{
    options.compilerArgs.add("--enable-preview")
}

// run tests with preview functions
tasks.compileTestJava{
    options.compilerArgs.add("--enable-preview")
}
tasks.test{
    jvmArgs?.add("--enable-preview")
}

application {
    mainClass.set("de.busam.fido2.Main")
    this.applicationDefaultJvmArgs += "--enable-preview"
}

