plugins {
    id("java")
    war
    id("io.freefair.aspectj.post-compile-weaving") version "8.10.2"
}

group = "org.habitsapp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.testcontainers:testcontainers:1.20.1")
    testImplementation("org.testcontainers:junit-jupiter:1.20.1")
    testImplementation("org.testcontainers:postgresql:1.20.1")
    testImplementation("org.skyscreamer:jsonassert:1.5.3")

    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.liquibase:liquibase-core:4.9.1")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")

    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-orgjson:0.12.6")

    implementation("org.aspectj:aspectjrt:1.9.22")
    implementation("org.aspectj:aspectjweaver:1.9.22")

    implementation("org.springframework:spring-context:6.1.11")
    implementation("org.springframework:spring-web:6.1.11")
    implementation("org.springframework:spring-webmvc:6.1.11")
    implementation("org.springframework:spring-aop:6.1.11")

    implementation("io.springfox:springfox-swagger2:3.0.0")
    implementation("io.springfox:springfox-swagger-ui:3.0.0")

    implementation("org.yaml:snakeyaml:2.3")

    testImplementation("org.springframework:spring-test:6.1.11")
    testImplementation("org.springframework:spring-webmvc:6.1.11")
}

tasks.war {
    archiveFileName.set("HabitsAssistant.war")
    webAppDirectory = file("src/main/webapp")
}

tasks.test {
    useJUnitPlatform()
}
