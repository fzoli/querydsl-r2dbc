val versionName: String by project

buildscript {

    repositories {
        google()
        jcenter()
    }

    configurations.classpath {
        resolutionStrategy.activateDependencyLocking()
    }

}

group = "com.farcsal.querydsl.r2dbc"
version = versionName

subprojects {

    group = rootProject.group
    version = rootProject.version

    buildscript {

        repositories {
            google()
            jcenter()
        }

        configurations.classpath {
            resolutionStrategy.activateDependencyLocking()
        }

    }

    repositories {
        google()
        jcenter()
    }

    dependencyLocking {
        lockAllConfigurations()
    }

}
