import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.MavenBuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

class ReleaseExtension(val extension: String) : BuildType({
    name = "release"
    id("${extension}_Release".toId())

    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }

    params {
        // TC is wiping the maven opts from the docker image
        param("env.MAVEN_OPTS", "-Dmaven.repo.local=/maven-cache")
    }

    steps {
        maven {
            goals = "package"
            mavenVersion = defaultProvidedVersion()
            workingDir = "extensions/$extension"
            pomLocation = "extensions/$extension/pom.xml"
            runnerArgs = "-T4C"
            dockerImage = "${TeamcityKotlinConstants.KOTLIN_MVN_IMAGE}:${TeamcityKotlinConstants.KOTLIN_MVN_TAG}"
            dockerImagePlatform = MavenBuildStep.ImagePlatform.Linux
            param("org.jfrog.artifactory.selectedDeployableServer.publishBuildInfo", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
            param("org.jfrog.artifactory.selectedDeployableServer.urlId", "0")
            param("org.jfrog.artifactory.selectedDeployableServer.envVarsExcludePatterns", "*password*,*secret*")
            param("org.jfrog.artifactory.selectedDeployableServer.targetSnapshotRepo", "teamcity-kotlin-snapshot-local")
            param("org.jfrog.artifactory.selectedDeployableServer.deployArtifacts", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.targetRepo", "teamcity-kotlin-release-local")
        }
    }

    triggers {
        vcs {
            triggerRules = """
                +:extensions/${extension}/*
            """.trimIndent()
            branchFilter = """
                +:<default>
            """.trimIndent()
        }
    }
})