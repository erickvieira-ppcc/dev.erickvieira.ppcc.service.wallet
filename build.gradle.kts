import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
    id("info.solidsoft.pitest")
    kotlin("jvm") version "1.6.0"
    kotlin("kapt") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
}

group = "dev.erickvieira.ppcc.service"
java.sourceCompatibility = JavaVersion.VERSION_1_8

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val springBootVersion = property("springBootVersion")
    val kotlinPluginsVersion = "1.6.0"
    val springCloudConfigVersion = "3.0.6"
    val eurekaClientVersion = "3.0.5"
    val jacksonVersion = "2.10.0"
    val lombokVersion = "1.18.10"
    val mockkVersion = "1.9.3"
    val postgreSQLVersion = "42.2.9"
    val nullableVersion = "0.2.0"
    val springFoxVersion = "3.0.0"
    val springMockkVersion = "1.1.3"
    val swaggerVersion = "1.6.0"
    val testContainersVersion = "1.15.3"
    val flywaydbVersion = "6.4.4"
    val hibernateValidatorVersion = "7.0.2.Final"
    val javaxValidationVersion = "2.0.1.Final"
    val junitVersion = "4.13.1"
    val restAssuredVersion = "3.3.0"
    val dbRiderVersion = "1.19.0"
    val dbRiderSpringVersion = "1.23.1"
    val approvaltestsVersion = "11.5.0"
    val assertkVersion = "0.24"
    val gsonVersion = "2.8.5"
    val descartesVersion = "1.3.1"

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hppc:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("io.springfox:springfox-boot-starter:$springFoxVersion")
    implementation("io.swagger:swagger-annotations:$swaggerVersion")
    implementation("io.swagger:swagger-models:$swaggerVersion")
    implementation("org.hibernate:hibernate-validator:$hibernateValidatorVersion")
    implementation("org.openapitools:jackson-databind-nullable:$nullableVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-amqp:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.cloud:spring-cloud-starter-config:$springCloudConfigVersion")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:$eurekaClientVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinPluginsVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinPluginsVersion")

    developmentOnly("org.springframework.boot:spring-boot-devtools:$springBootVersion")

    compileOnly("javax.validation:validation-api:$javaxValidationVersion")
    compileOnly("org.projectlombok:lombok:$lombokVersion")

    runtimeOnly("org.flywaydb:flyway-core:$flywaydbVersion")
    runtimeOnly("org.postgresql:postgresql:$postgreSQLVersion")

    kapt("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("com.github.database-rider:rider-junit5:$dbRiderVersion")
    testImplementation("com.github.database-rider:rider-spring:$dbRiderSpringVersion")
    testImplementation("com.approvaltests:approvaltests:$approvaltestsVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")

    pitest("eu.stamp-project:descartes:$descartesVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.dependsOn(":openApiGenerate")
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "${property("jvm")}"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.dependsOn(":openApiGenerate")
compileTestKotlin.kotlinOptions {
    jvmTarget = "${property("jvm")}"
}

tasks.withType<Test> {
    dependsOn(":openApiGenerate")
    useJUnitPlatform()
    systemProperty("spring.test.constructor.autowire.mode", "all")
}

sourceSets {
    getByName("test").java.srcDirs("src/test")
    getByName("main").java.srcDirs("build/generated/src/main/kotlin")
}

pitest {
    targetClasses.set(
        arrayListOf(
            "dev.erickvieira.ppcc.service.wallet.domain.service.*",
            "dev.erickvieira.ppcc.service.wallet.domain.extension.*",
            "dev.erickvieira.ppcc.service.wallet.unit.*"
        )
    )
    testSourceSets.set(arrayListOf(sourceSets.test.get()))
    mainSourceSets.set(arrayListOf(sourceSets.main.get()))
    junit5PluginVersion.set("0.12")
    mutationEngine.set("descartes")
    useClasspathFile.set(true)
    outputFormats.set(arrayListOf("HTML"))
    pitestVersion.set("1.6.7")
}

openApiGenerate {
    apiPackage.set("${property("generateApiPackage")}")
    generatorName.set("${property("apiGeneratorName")}")
    inputSpec.set("$rootDir/src/main/resources/swagger/api.yml")
    modelPackage.set("${property("generateModelPackage")}")
    outputDir.set("${buildDir}/generated")

    configOptions.set(
        mapOf(
            "delegatePattern" to "true",
            "dateLibrary" to "java8",
            "useTags" to "true",
        )
    )

    importMappings.set(
        mapOf(
            "Pageable" to "org.springframework.data.domain.Pageable",
            "Sort" to "org.springframework.data.domain.Sort",
            "Page" to "org.springframework.data.domain.Page"
        )
    )
}
