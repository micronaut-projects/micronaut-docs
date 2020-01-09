package io.micronaut.docs

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Destroys
import org.gradle.api.tasks.TaskAction

@CompileStatic
class CleanDocResourcesTask extends DefaultTask {

    @Destroys
    List<File> resourceFolders

    @TaskAction
    void cleanResources() {
        for(File f : resourceFolders) {
            project.getLogger().info("deleting dir ${f.name}")
            if (f.exists()) {
                f.deleteDir()
            }
        }
    }
}
