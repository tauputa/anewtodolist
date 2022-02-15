import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerBuild
import com.xero.kotlin.v2018_2.buildSteps.assumeRole
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script

open class DockerBuild() : BuildType() {
    var buildContext: String = "."
    var dockerfile: String = "Dockerfile"
    lateinit var imageName: String
    var tag: String = "latest"
    var publish: Boolean = false
    var additionalArgs: String? = null
    lateinit var repository: String
    lateinit var awsRoleArn: String
    var awsRegion: String = "ap-southeast-2"
    lateinit var username: String

    constructor(init: DockerBuild.() -> Unit): this() {
        init()

        if (publish) {
            params {
                param("artifactory.username", this@DockerBuild.username)
                // Empty default so it can be overridden later
                password("artifactory.password", "")
            }
        }

        vcs {
            root(DslContext.settingsRoot)
            cleanCheckout = true
        }

        steps {
            dockerCommand {
                name = "Build Image"
                commandType = build {
                    source = path {
                        path = this@DockerBuild.dockerfile
                    }
                    contextDir = this@DockerBuild.buildContext
                    if (this@DockerBuild.publish) {
                        namesAndTags = "${this@DockerBuild.repository}/${this@DockerBuild.imageName}:${this@DockerBuild.tag}"
                    } else {
                        namesAndTags = "${this@DockerBuild.imageName}:${this@DockerBuild.tag}"
                    }
                    if (this@DockerBuild.additionalArgs.isNullOrEmpty() == false) {
                        commandArgs = this@DockerBuild.additionalArgs
                    }
                }
            }
            if (this@DockerBuild.publish) {
                assumeRole {
                    roleArn = this@DockerBuild.awsRoleArn
                    region = this@DockerBuild.awsRegion
                }
                script {
                    name = "Get Docker Credentials"
                    scriptContent = """
                        #!/bin/bash -e
                        
                        artifactory_password=${'$'}(aws ssm get-parameter --name "${TeamcityKotlinConstants.SSM_USERNAME_PREFIX}/%artifactory.username%" --with-decryption --region ${this@DockerBuild.awsRegion} | jq -r .Parameter.Value)
                        
                        echo "Setting environment variables..."
                        echo "##teamcity[setParameter name='artifactory.password' value='${'$'}{artifactory_password}']"
                    """.trimIndent()
                }
                dockerCommand {
                    name = "Docker Login"
                    commandType = other {
                        subCommand = "login"
                        commandArgs = "-u %artifactory.username% -p %artifactory.password% https://${this@DockerBuild.repository}"
                    }
                }
                dockerCommand {
                    name = "Docker Push"
                    commandType = push {
                        namesAndTags = "${this@DockerBuild.repository}/${this@DockerBuild.imageName}:${this@DockerBuild.tag}"
                        removeImageAfterPush = true
                    }
                }
            }
        }
    }
}