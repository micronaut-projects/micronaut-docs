package io.micronaut.docs

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CompileStatic
class DownloadDocResourcesTask extends DefaultTask {

    private static final RESOURCES_FOLDER = 'src/main/docs/resources'

    @OutputDirectory
    File resourceFolder

    @OutputDirectories
    List<File> resourceFolders

    String githubRaw = "https://raw.githubusercontent.com/${MicronautDocs.GITHUB_ORG}/${MicronautDocs.GITHUB_REPO}/${MicronautDocs.GIT_BRANCH}/${RESOURCES_FOLDER}"

    @TaskAction
    void downloadResources() {
        for (File resource : resourceFolders) {
            if (!resource.exists()) {
                resource.mkdirs()
            }
        }
        [
                css  : MicronautDocsResources.CSS,
                img  : MicronautDocsResources.IMG,
                js   : MicronautDocsResources.JS,
                style: MicronautDocsResources.STYLE,
        ].each { String k, List<String> v ->
            v.each { name ->
                project.getLogger().info("downloading ${v}")
                String path = "${resourceFolder.absolutePath}/${k}/" + name
                File f = new File(path)
                f.createNewFile()
                f.text = new URL("${githubRaw}/${k}/${name}").text
            }
        }
    }
}
