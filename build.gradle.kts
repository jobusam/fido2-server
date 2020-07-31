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
    implementation("org.slf4j","slf4j-simple","1.7.28")

    // Security - JWT
    implementation("com.auth0","java-jwt","3.10.3")
    // Security - WebAuthN Standard for FIDO2 Support
    implementation("com.yubico","webauthn-server-core","1.6.4")
    implementation("com.yubico","yubico-util","1.6.4")

    // Data serialization
    implementation("com.fasterxml.jackson.core","jackson-core","2.11.1")
    implementation("com.fasterxml.jackson.core","jackson-databind","2.11.1")
    // use for serialization of JAVA 8 Optionals
    implementation("com.fasterxml.jackson.datatype","jackson-datatype-jdk8","2.11.1")

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
    // this doesn't work correctly
    jvmArgs?.add("--enable-preview")
    // only this way o setting the properties works currently
    setJvmArgs(listOf("--enable-preview"))
    //println("jvmArgs = "+jvmArgs)
}

application {
    mainClass.set("de.busam.fido2.Main")
    this.applicationDefaultJvmArgs += "--enable-preview"
}

