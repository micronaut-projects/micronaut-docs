import groovy.io.FileType

List<String> assets = ['css','js','img','fonts']
File folder = new File('/Users/sdelamo/github/micronaut-projects/micronaut-docs')
folder.eachFile(FileType.DIRECTORIES)  { semanticVersionFolder ->
    if (semanticVersionFolder.name.count('.') >= 2) {
        semanticVersionFolder.eachFile(FileType.DIRECTORIES) { releaseFolder ->
            if (assets.contains(releaseFolder.name)) {
                if (!releaseFolder.deleteDir()) {
                    println "Could not release folder ${releaseFolder.name}"
                }
            }
        }
    }
}
