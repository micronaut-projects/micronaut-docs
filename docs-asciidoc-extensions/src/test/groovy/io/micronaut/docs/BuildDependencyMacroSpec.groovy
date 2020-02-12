package io.micronaut.docs

import spock.lang.Specification

class BuildDependencyMacroSpec extends Specification {

    void "implementation is used instead of compile for gradle"() {
        when:
        String content = BuildDependencyMacro.contentForTargetAndAttributes("micronaut-function-aws-alexa", [:])

        then:
        content.contains('data-lang="gradle-groovy">implementation')
        content.contains('data-lang="gradle-kotlin">implementation')
    }
}
