import jetbrains.buildServer.configs.kotlin.v2018_2.*

class ExtensionProject(val extension: String) : Project({
    name = extension
    id(extension.toId())

    val buildExt = BuildExtension(extension)
    val releaseExt = ReleaseExtension(extension)

    sequence {
        build(buildExt)
        build(releaseExt)
    }
})