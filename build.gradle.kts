buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.45.0")
    }

    extra.apply {
        set("kotlinVersion", "1.9.23")
        set("minSdkVersion", 21)
        set("targetSdkVersion", 34)
        set("buildToolsVersion", 34)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}
