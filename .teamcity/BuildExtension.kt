import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.MavenBuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

class BuildExtension(val extension: String) : BuildType({
    name = "compile"
    id("${extension}_Compile".toId())

    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }

    params {
        // TC is wiping the maven opts from the docker image
        param("env.MAVEN_OPTS", "-Dmaven.repo.local=/maven-cache")
    }

    steps {
        script {
            scriptContent = "java -version"
        }
        maven {
            goals = "compile"
            mavenVersion = defaultProvidedVersion()
            pomLocation = "extensions/$extension/pom.xml"
            workingDir = "extensions/$extension"
            runnerArgs = "-T4C"
            dockerImage = "${TeamcityKotlinConstants.KOTLIN_MVN_IMAGE}:${TeamcityKotlinConstants.KOTLIN_MVN_TAG}"
            dockerImagePlatform = MavenBuildStep.ImagePlatform.Linux
        }
    }

    triggers {
        vcs {
            triggerRules = """
                +:extensions/${extension}/*
            """.trimIndent()
        }
    }

    features {
        commitStatusPublisher {
            vcsRootExtId = DslContext.settingsRootId.value
            publisher = github {
                githubUrl = "https://github.dev.xero.com/api/v3"
                authType = password {
                    userName = "%github.user%"
                    password = "%github.token%"
                }
            }
        }
        pullRequests {
            provider = github {
                authType = vcsRoot()
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER_OR_COLLABORATOR
            }
        }
    }
})