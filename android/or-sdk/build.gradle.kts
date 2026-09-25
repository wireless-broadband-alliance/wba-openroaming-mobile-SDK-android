plugins {
    id("com.android.library")
}

android {
    namespace = "com.wba.sdk"

    compileSdk {

        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        minSdk = 30
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation("com.google.android.material:material:1.14.0")
    implementation("com.squareup.okhttp3:okhttp:5.5.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.startup:startup-runtime:1.2.0")

    implementation("io.ktor:ktor-client-core:3.5.2")
    implementation("io.ktor:ktor-client-okhttp:3.5.2")
}

configurations.all {

    exclude(group = "javax.annotation", module = "javax.annotation-api")
    exclude(group = "net.java.dev.jna")
    exclude(group = "xpp3", module = "xpp3")
    exclude(group = "net.sf.kxml", module = "kxml2")
    exclude(group = "org.junit.platform")
    exclude(group = "org.junit.jupiter")
    exclude(group = "junit", module = "junit")

    resolutionStrategy {

        force(
            "com.google.guava:guava:33.6.0-android",
            "org.apache.httpcomponents:httpclient:4.5.13",
            "org.apache.commons:commons-lang3:3.18.0"
        )

        eachDependency {

            when (requested.group) {
                "io.netty" -> useVersion("4.2.17.Final")
                "com.google.protobuf" -> useVersion("4.35.0")
                "org.bouncycastle" -> useVersion("1.86")
            }
        }
    }
}