package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.CommitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2018_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'XeroflowDockerBuildMasterCompile'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("XeroflowDockerBuildMasterCompile")) {
    features {
        val feature1 = find<CommitStatusPublisher> {
            commitStatusPublisher {
                vcsRootExtId = "${DslContext.settingsRoot.id}"
                publisher = github {
                    githubUrl = "https://github.dev.xero.com/api/v3"
                    authType = password {
                        userName = "%github.user%"
                        password = "%github.token%"
                    }
                }
            }
        }
        feature1.apply {
            publisher = github {
                githubUrl = "https://github.dev.xero.com/api/v3"
                authType = password {
                    userName = "%github.user%"
                    password = "credentialsJSON:486d7f9d-3418-4673-9acb-9c5dd09f72d9"
                }
            }
        }
    }
}
