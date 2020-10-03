import com.google.protobuf.gradle.*

val grpcVersion = "1.29.0" // CURRENT_GRPC_VERSION
val protobufVersion = "3.13.0"
val kotlinVersion = "1.4.10"
val coroutinesVersion = "1.3.3"
val grpcKotlinVersion = "0.2.0"

plugins {
    java
    kotlin("jvm") version "1.4.10"
    idea
    application
    id("com.google.protobuf") version "0.8.13"
}

application {
    mainClassName = "MainKt"
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin"))
        classpath("com.google.protobuf","protobuf-gradle-plugin","0.8.13")
        classpath("org.jetbrains.kotlin","kotlin-gradle-plugin", "1.4.10")
    }
}

group = "io.yeongcheon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("javax.annotation", "javax.annotation-api", "1.3.2")

    implementation("io.grpc", "grpc-kotlin-stub", grpcKotlinVersion)
    implementation("com.google.protobuf","protobuf-java",protobufVersion)
    implementation("com.google.protobuf", "protobuf-java-util", protobufVersion)
    implementation("io.grpc","grpc-stub", grpcVersion)
    implementation("io.grpc","grpc-protobuf", grpcVersion)
    implementation("io.grpc","grpc-kotlin-stub", grpcKotlinVersion)

    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.9")
    runtimeOnly("io.grpc","grpc-netty",grpcVersion)
    protobuf(files("src/main/resources/proto/model.proto"))

    testCompile("junit", "junit", "4.12")
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}