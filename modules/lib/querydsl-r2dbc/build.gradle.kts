val querydsl_version: String by project
val r2dbc_version: String by project
val reactor_version: String by project
val junit_jupiter_version: String by project
val postgresql_jdbc_version: String by project
val testcontainers_postgresql_version: String by project
val javax_annotation_api: String by project
val logback_version: String by project

plugins {
    `java-library`
}

dependencies {
    api("javax.annotation:javax.annotation-api:$javax_annotation_api")
    api("com.querydsl:querydsl-core:$querydsl_version")
    api("com.querydsl:querydsl-sql:$querydsl_version")
    api("io.r2dbc:r2dbc-spi:$r2dbc_version")
    api("io.projectreactor:reactor-core:$reactor_version")
    testImplementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("org.testcontainers:postgresql:${testcontainers_postgresql_version}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_jupiter_version")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junit_jupiter_version")
    testRuntime("org.postgresql:postgresql:$postgresql_jdbc_version")
    testRuntime("io.r2dbc:r2dbc-postgresql:$r2dbc_version")
    testRuntime("io.r2dbc:r2dbc-pool:$r2dbc_version")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
