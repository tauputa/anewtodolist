import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

version = "2018.2"

val extensions = listOf(
        "example-extension",
        "aws-assume-role",
        "aws-get-secretmanager-secret",
        "aws-get-ssm-parameter",
        "choco-pack",
        "choco-push",
        "comment-on-pr",
        "generic-docker-build",
        "kotlin-validation-build",
        "new-relic-deployment-marker",
        "new-relic-release-event",
        "pk8s-deploy",
        "pk8s-helm-install",
        "release-freeze",
        "release-register",
        "setup-deployer-env",
        "slack-notification",
        "update-slack-notification",
        "slack-release-event",
        "xeroci-artifactory-build",
        "xeroci-docker-build",
        "xeroflow-docker-build-master",
        "xeroflow-docker-build-pr",
        "xeroflow-docker-publish-custom",
        "xop-publish",
        "set-git-commit-info-parameters",
        "calculate-semantic-version-from-git-tags",
        "push-git-tag"
)

project {
    params {
        // This makes it impossible to change the build settings through the UI
        param("teamcity.ui.settings.readOnly", "true")
        param("git.url", "https://github.dev.xero.com/Xero/teamcity-kotlin-extensions")
        param("git.branch", "refs/heads/master")
        param("github.user", "buildservice2016")
        password("github.token", "credentialsJSON:c4043f5a-4055-4d3c-963e-936f91d93f8e")
    }

    for (extension in extensions) {
        subProject(ExtensionProject(extension))
    }

    subProject(Project {
        name = "Utilities"
        id("Utilities".toId())

        buildType(DockerBuild {
            name = "kotlin-mvn Docker Image"
            id("Utilities kotlin-mvn docker image".toId())

            buildContext = "utility/kotlin-mvn-image"
            dockerfile = "utility/kotlin-mvn-image/Dockerfile"
            imageName = "kotlin-mvn"
            tag = "%build.vcs.number%"
            publish = true
            repository = "paas-docker-common.artifactory.xero-support.com"
            awsRoleArn = "arn:aws:iam::640077214053:role/XRO-Deployment"
            username = "pbd_automation"

            triggers {
                vcs {
                    triggerRules = """
                        +:utility/kotlin-mvn-image/*
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
    })
}
