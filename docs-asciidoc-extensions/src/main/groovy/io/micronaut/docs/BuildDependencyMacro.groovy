package io.micronaut.docs

import org.asciidoctor.ast.AbstractBlock
import org.asciidoctor.extension.InlineMacroProcessor

/**
 * Inline macro which can be invoked in asciidoc with:
 *
 * dependency:micronaut-spring[version="1.0.1", groupId="io.micronaut"]
 *
 * For
 *
 * Gradle
 * compile 'io.micronaut:micronaut-spring:1.0.1'
 *
 * Maven
 * <dependency>
 *     <groupId>io.micronaut</groupId>
 *     <artifactId>micronaut-spring</artifactId>
 *     <version>1.0.1</version>
 * </dependency>
 *
 * invoke it with:
 *
 * dependency:micronaut-spring[version="1.0.1", groupId="io.micronaut", verbose="true"]
 *
 * for:
 *
 * Gradle
 * compile group: 'io.micronaut', name: 'micronaut-spring', version: '1.0.1'
 *
 * Maven
 * <dependency>
 *     <groupId>io.micronaut</groupId>
 *     <artifactId>micronaut-spring</artifactId>
 *     <version>1.0.1</version>
 * </dependency>
 *
 * or simply:
 *
 * Gradle
 * compile 'io.micronaut:micronaut-spring'
 *
 * Maven
 * <dependency>
 * <groupId>io.micronaut</groupId>
 * <artifactId>micronaut-spring</artifactId>
 * </dependency>
 *
 * By default compile scope is used
 *
 * You can use:
 *
 * dependency:micronaut-spring[scope="testCompile"]
 *
 * or specify a different scope for gradle or maven
 *
 * dependency:micronaut-spring[gradleScope="implementation"]
 *
 */
class BuildDependencyMacro extends InlineMacroProcessor implements ValueAtAttributes {
    static final String MICRONAUT_GROUPID = "io.micronaut."
    static final String DEPENDENCY_PREFIX = 'micronaut-'
    static final String GROUPID = 'io.micronaut'
    static final String MULTILANGUAGECSSCLASS = 'multi-language-sample'
    static final String BUILD_GRADLE = 'gradle'
    static final String BUILD_MAVEN = 'maven'
    static final String BUILD_GRADLE_KOTLIN = 'gradle-kotlin'
    public static final String SCOPE_COMPILE = 'compile'

    BuildDependencyMacro(String macroName, Map<String, Object> config) {
        super(macroName, config)
    }

    @Override
    protected Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        String groupId
        String artifactId
        String version

        if (target.contains(":")) {
            def tokens = target.split(":")
            groupId = tokens[0] ?: GROUPID
            artifactId = tokens[1]
            if (tokens.length == 3) {
                version = tokens[2]
            } else {
                version = valueAtAttributes('version', attributes)    
            }
        } else {
            groupId = valueAtAttributes('groupId', attributes) ?: GROUPID
            artifactId = target.startsWith(DEPENDENCY_PREFIX) ? target : groupId.startsWith(MICRONAUT_GROUPID) ? "${DEPENDENCY_PREFIX}${target}" : target
            version = valueAtAttributes('version', attributes)
        }

        boolean verbose = valueAtAttributes('verbose', attributes) as boolean
        String classifier = valueAtAttributes('classifier', attributes)
        String gradleScope = valueAtAttributes('gradleScope', attributes) ?: toGradleScope(attributes) ?: SCOPE_COMPILE
        String mavenScope = valueAtAttributes('mavenScope', attributes) ?: toMavenScope(attributes) ?: SCOPE_COMPILE
        String content = gradleDependency(BUILD_GRADLE, groupId, artifactId, version, classifier, gradleScope, MULTILANGUAGECSSCLASS, verbose)
        content += mavenDependency(BUILD_MAVEN, groupId, artifactId, version, classifier, mavenScope, MULTILANGUAGECSSCLASS)
        content += gradleKotlinDependency(BUILD_GRADLE_KOTLIN, groupId, artifactId, version, classifier, mavenScope, MULTILANGUAGECSSCLASS)
        createBlock(parent, "pass", [content], attributes, config).convert()
    }

    private String toMavenScope(Map<String, Object> attributes) {
        String s = valueAtAttributes('scope', attributes)
        switch (s) {
            case 'api':
            case 'implementation':
                return 'compile'
            case 'testCompile':
            case 'testRuntime':
            case 'testRuntimeOnly':
            case 'testImplementation':
                return 'test'
            case 'compileOnly': return 'provided'
            case 'runtimeOnly': return 'runtime'
            default: return s
        }
    }

    private String toGradleScope(Map<String, Object> attributes) {
        String s = valueAtAttributes('scope', attributes)
        switch (s) {
            case 'test':
                return 'testCompile'
            break
            case 'provided':
                return 'developmentOnly'
            default: return s
        }
    }



    String gradleDependency(String build,
                              String groupId,
                              String artifactId,
                              String version,
                              String classifier,
                              String scope,
                              String multilanguageCssClass,
                              boolean verbose) {
String html = """\
        <div class=\"listingblock ${multilanguageCssClass}\">
<div class=\"content\">
<pre class=\"highlightjs highlight\"><code class=\"language-groovy hljs" data-lang="${build}">"""
        if (verbose) {
            html += "${scope} <span class=\"hljs-string\">group:</span> <span class=\"hljs-string\">'${groupId}'</span>, <span class=\"hljs-string\">name:</span> <span class=\"hljs-string\">'${artifactId}'</span>"
            if (version) {
                html +=", <span class=\"hljs-string\">version:</span> <span class=\"hljs-string\">'${version}'</span>"
            }
            if (classifier) {
                html +=", <span class=\"hljs-string\">classifier:</span> <span class=\"hljs-string\">'${classifier}'</span>"
            }
        } else {
            html += "${scope} <span class=\"hljs-string\">'${groupId}:${artifactId}"
            if (version) {
                html += ":${version}"
            }
            if (classifier) {
                html += ":${classifier}"
            }
            html += "'</span>"
        }
        html += """</code></pre>
</div>
</div>
"""
        html
    }

    String gradleKotlinDependency(String build,
                              String groupId,
                              String artifactId,
                              String version,
                              String classifier,
                              String scope,
                              String multilanguageCssClass) {
        String html = """\
        <div class=\"listingblock ${multilanguageCssClass}\">
<div class=\"content\">
<pre class=\"highlightjs highlight\"><code class=\"language-kotlin hljs" data-lang="${build}">"""

        html += "${scope}(<span class=\"hljs-string\">\"${groupId}:${artifactId}"
        if (version) {
            html += ":${version}"
        }
        if (classifier) {
            html += ":${classifier}"
        }
        html += "\")</span>"

        html += """</code></pre>
</div>
</div>
"""
        html
    }

    String mavenDependency(String build,
                              String groupId,
                              String artifactId,
                              String version,
                              String classifier,
                              String scope,
                              String multilanguageCssClass
                             ) {
        String html
        if (scope == 'annotationProcessor') {
            html = """\
<div class=\"listingblock ${multilanguageCssClass}\">
<div class=\"content\">
<pre class=\"highlightjs highlight\"><code class=\"language-xml hljs\" data-lang=\"${build}\">&lt;annotationProcessorPaths&gt;
    &lt;path&gt;
        &lt;groupId&gt;${groupId}&lt;/groupId&gt;
        &lt;artifactId&gt;${artifactId}&lt;/artifactId&gt;"""
            if (version) {
                html += "\n        &lt;version&gt;${version}&lt;/version&gt;"
            }
            if (classifier) {
                html += "\n        &lt;classifier&gt;${classifier}&lt;/classifier&gt;"
            }
            html += """
    &lt;/path&gt;
&lt;/annotationProcessorPaths&gt;</code></pre>
</div>
</div>
"""
        } else {

            html = """\
<div class=\"listingblock ${multilanguageCssClass}\">
<div class=\"content\">
<pre class=\"highlightjs highlight\"><code class=\"language-xml hljs\" data-lang=\"${build}\">&lt;dependency&gt;
    &lt;groupId&gt;${groupId}&lt;/groupId&gt;
    &lt;artifactId&gt;${artifactId}&lt;/artifactId&gt;"""
            if (version) {
                html += "\n    &lt;version&gt;${version}&lt;/version&gt;"
            }
            if (scope != SCOPE_COMPILE) {
                html += "\n    &lt;scope&gt;${scope}&lt;/scope&gt;"
            }
            if (classifier) {
                html += "\n    &lt;classifier&gt;${classifier}&lt;/classifier&gt;"
            }

            html += """
&lt;/dependency&gt;</code></pre>
</div>
</div>
"""
        }
        return html
    }
}

