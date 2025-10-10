package io.micronaut.docs

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec
import io.micronaut.annotation.processing.test.JavaParser
import org.intellij.lang.annotations.Language

class AsciiDocConfigurationSpec extends AbstractTypeElementSpec {

    @Override
    protected JavaParser newJavaParser() {
        return new JavaParser() {
        }
    }

    protected String createAsciiDoc(@Language("java") String cls) {
        return super.buildAndReadResourceAsString("META-INF/config-properties.adoc", cls)
    }

    def setup() {
    }

    void "test configuration metadata and records"() {
        when:
        //language=AsciiDoc
        String doc = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;

/**
*  My Configuration description.
*
 * @param name The name of the config
 * @param age The age of the config
*/
@ConfigurationProperties("test")
record MyProperties(String name, int age, NestedConfig nested) {
    @ConfigurationProperties("nested")
    record NestedConfig(int num) {}
}

''')

        then:
        doc == '''
++++
<a id="test.MyProperties" href="#test.MyProperties">&#128279;</a>
++++
.Configuration Properties for api:test.MyProperties[]
|===
|Property |Type |Description |Default value

| `+test.name+`
|java.lang.String
|The name of the config
|


| `+test.age+`
|int
|The age of the config
|


|===
<<<
++++
<a id="test.MyProperties$NestedConfig" href="#test.MyProperties$NestedConfig">&#128279;</a>
++++
.Configuration Properties for api:test.MyProperties$NestedConfig[]
|===
|Property |Type |Description |Default value

| `+test.nested.num+`
|int
|
|


|===
<<<'''
    }

    void "test configuration metadata and interfaces"() {
        when:
        String doc = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;

/**
*  My Configuration description.
*
*/
@ConfigurationProperties("test")
interface MyProperties {
    /**
    * @return The name
    */
    String getName();

    /**
     * The age
     */
    int getAge();
}

''')

        then:
            doc == '''
++++
<a id="test.MyProperties" href="#test.MyProperties">&#128279;</a>
++++
.Configuration Properties for api:test.MyProperties[]
|===
|Property |Type |Description |Default value

| `+test.name+`
|java.lang.String
|The name
|


| `+test.age+`
|int
|The age
|


|===
<<<'''
    }

    void "test default values and descriptions"() {
        when:
        String doc = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;import io.micronaut.core.bind.annotation.Bindable;

/**
*  My Configuration description.
*/
@ConfigurationProperties("test")
interface MyProperties {

    String DEFAULT_NAME = "Fred";

    /**
    * Get the name, default value {@value #DEFAULT_NAME}.
    * @return The name
    */
    @Bindable(defaultValue = DEFAULT_NAME)
    String getName();

    /**
     * The age
     */
    int getAge();
}

''')

        then:
            doc == '''
++++
<a id="test.MyProperties" href="#test.MyProperties">&#128279;</a>
++++
.Configuration Properties for api:test.MyProperties[]
|===
|Property |Type |Description |Default value

| `+test.name+`
|java.lang.String
|Get the name, default value {@value #DEFAULT_NAME}.
|Fred


| `+test.age+`
|int
|The age
|


|===
<<<'''
    }

    void "test setter descriptions"() {
        when:
        String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;
import io.micronaut.core.bind.annotation.Bindable;import io.micronaut.core.util.Toggleable;

interface BaseConfig extends Toggleable {

    /**
     * @return The name
     */
    String getName();
}

@ConfigurationProperties("test")
class Config implements BaseConfig {

    public static final String DEFAULT_NAME = "test";

    private String name = DEFAULT_NAME;

    @Bindable(defaultValue = DEFAULT_NAME)
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name (default {@value #DEFAULT_NAME}).
     * @param name the name to use
     */
    public void setName(String name) {
        this.name = name;
    }
}
''')

        then:
            docs == '''
++++
<a id="test.Config" href="#test.Config">&#128279;</a>
++++
.Configuration Properties for api:test.Config[]
|===
|Property |Type |Description |Default value

| `+test.name+`
|java.lang.String
|Sets the name (default {@value #DEFAULT_NAME}).
|test


|===
<<<'''
    }

    void "test configuration metadata and javabeans"() {
        when:
        String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;

/**
*  My Configuration description.
*
*/
@ConfigurationProperties("test")
class MyProperties {

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    /**
    * Sets the name.
    * @param name The name
    */
    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    /**
    *
    * @param age The age
    */
    public void setAge(int age) {
        this.age = age;
    }
}

''')

        then:
            docs == '''
++++
<a id="test.MyProperties" href="#test.MyProperties">&#128279;</a>
++++
.Configuration Properties for api:test.MyProperties[]
|===
|Property |Type |Description |Default value

| `+test.name+`
|java.lang.String
|Sets the name.
|


| `+test.age+`
|int
|The age
|


|===
<<<'''
    }

    void "test configuration builder on method"() {
        when:
            String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;

@ConfigurationProperties("test")
class MyProperties {

    private Test test;

    @ConfigurationBuilder(factoryMethod="build")
    void setTest(Test test) {
        this.test = test;
    }

    Test getTest() {
        return this.test;
    }

}

class Test {
    private String foo;
    private Test() {}
    public void setFoo(String s) {
        this.foo = s;
    }
    public String getFoo() {
        return foo;
    }

    static Test build() {
        return new Test();
    }
}
''')

        then:
            docs == '''
++++
<a id="test.Test" href="#test.Test">&#128279;</a>
++++
.Configuration Properties for api:test.Test[]
|===
|Property |Type |Description |Default value

| `+test.foo+`
|java.lang.String
|
|


|===
<<<'''
    }

    void "test configuration builder with includes"() {
        when:
            String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;

@ConfigurationProperties("test")
class MyProperties {

    @ConfigurationBuilder(factoryMethod="build", includes="foo")
    Test test;

}

class Test {
    private String foo;
    private String bar;
    private Test() {}
    public void setFoo(String s) {
        this.foo = s;
    }
    public String getFoo() {
        return foo;
    }
    public void setBar(String s) {
        this.bar = s;
    }
    public String getBar() {
        return bar;
    }

    static Test build() {
        return new Test();
    }
}
''')
        then:
            docs == '''
++++
<a id="test.Test" href="#test.Test">&#128279;</a>
++++
.Configuration Properties for api:test.Test[]
|===
|Property |Type |Description |Default value

| `+test.foo+`
|java.lang.String
|
|


|===
<<<'''
    }

    void "test configuration builder with factory method"() {
        when:
            String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;

@ConfigurationProperties("test")
class MyProperties {

    @ConfigurationBuilder(factoryMethod="build")
    Test test;

}

class Test {
    private String foo;
    private Test() {}
    public void setFoo(String s) {
        this.foo = s;
    }
    public String getFoo() {
        return foo;
    }

    static Test build() {
        return new Test();
    }
}
''')
        then:
            docs == '''
++++
<a id="test.Test" href="#test.Test">&#128279;</a>
++++
.Configuration Properties for api:test.Test[]
|===
|Property |Type |Description |Default value

| `+test.foo+`
|java.lang.String
|
|


|===
<<<'''
    }

    void "test with setters that return void"() {
        when:
            String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;
import java.lang.Deprecated;

@ConfigurationProperties("test")
class MyProperties {

    @ConfigurationBuilder
    Test test = new Test();

}

class Test {
    private String foo;
    private int bar;
    private Long baz;

    public void setFoo(String s) { this.foo = s;}
    public void setBar(int s) {this.bar = s;}
    @Deprecated
    public void setBaz(Long s) {this.baz = s;}

    public String getFoo() { return this.foo; }
    public int getBar() { return this.bar; }
    public Long getBaz() { return this.baz; }
}
''')
        then:
            docs == '''
++++
<a id="test.Test" href="#test.Test">&#128279;</a>
++++
.Configuration Properties for api:test.Test[]
|===
|Property |Type |Description |Default value

| `+test.foo+`
|java.lang.String
|
|


| `+test.bar+`
|int
|
|


|===
<<<'''
    }

    void "test different inject types for config properties"() {
        when:
            String doc = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;
import org.neo4j.driver.*;

@ConfigurationProperties("neo4j.test")
class Neo4jProperties {
    protected java.net.URI uri;

    @ConfigurationBuilder(
        prefixes="with",
        allowZeroArgs=true
    )
    Config.ConfigBuilder options = Config.builder();


}
''')
        then:
            doc == '''
++++
<a id="test.Neo4jProperties" href="#test.Neo4jProperties">&#128279;</a>
++++
.Configuration Properties for api:test.Neo4jProperties[]
|===
|Property |Type |Description |Default value

| `+neo4j.test.uri+`
|java.net.URI
|
|


|===
<<<
++++
<a id="org.neo4j.driver.Config$ConfigBuilder" href="#org.neo4j.driver.Config$ConfigBuilder">&#128279;</a>
++++
.Configuration Properties for api:org.neo4j.driver.Config$ConfigBuilder[]
|===
|Property |Type |Description |Default value

| `+neo4j.test.logging+`
|org.neo4j.driver.Logging
|
|


| `+neo4j.test.leaked-sessions-logging+`
|boolean
|
|


| `+neo4j.test.connection-liveness-check-timeout+`
|java.time.Duration
|
|


| `+neo4j.test.max-connection-lifetime+`
|java.time.Duration
|
|


| `+neo4j.test.max-connection-pool-size+`
|int
|
|


| `+neo4j.test.connection-acquisition-timeout+`
|java.time.Duration
|
|


| `+neo4j.test.encryption+`
|boolean
|
|


| `+neo4j.test.trust-strategy+`
|org.neo4j.driver.Config$TrustStrategy
|
|


| `+neo4j.test.routing-table-purge-delay+`
|java.time.Duration
|
|


| `+neo4j.test.fetch-size+`
|long
|
|


| `+neo4j.test.connection-timeout+`
|java.time.Duration
|
|


| `+neo4j.test.max-transaction-retry-time+`
|java.time.Duration
|
|


| `+neo4j.test.resolver+`
|org.neo4j.driver.net.ServerAddressResolver
|
|


| `+neo4j.test.driver-metrics+`
|boolean
|
|


| `+neo4j.test.metrics-adapter+`
|org.neo4j.driver.MetricsAdapter
|
|


| `+neo4j.test.event-loop-threads+`
|int
|
|


| `+neo4j.test.user-agent+`
|java.lang.String
|
|


| `+neo4j.test.notification-config+`
|org.neo4j.driver.NotificationConfig
|
|


| `+neo4j.test.telemetry-disabled+`
|boolean
|
|


|===
<<<'''
    }

    void "test specifying a configuration prefix"() {
        when:
            String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;
import org.neo4j.driver.*;

@ConfigurationProperties("neo4j.test")
class Neo4jProperties {
    protected java.net.URI uri;

    @ConfigurationBuilder(
        prefixes="with",
        allowZeroArgs=true,
        configurationPrefix="options"
    )
    Config.ConfigBuilder options = Config.builder();

}
''')
        then:
            docs == '''
++++
<a id="test.Neo4jProperties" href="#test.Neo4jProperties">&#128279;</a>
++++
.Configuration Properties for api:test.Neo4jProperties[]
|===
|Property |Type |Description |Default value

| `+neo4j.test.uri+`
|java.net.URI
|
|


|===
<<<
++++
<a id="org.neo4j.driver.Config$ConfigBuilder" href="#org.neo4j.driver.Config$ConfigBuilder">&#128279;</a>
++++
.Configuration Properties for api:org.neo4j.driver.Config$ConfigBuilder[]
|===
|Property |Type |Description |Default value

| `+neo4j.test.options.logging+`
|org.neo4j.driver.Logging
|
|


| `+neo4j.test.options.leaked-sessions-logging+`
|boolean
|
|


| `+neo4j.test.options.connection-liveness-check-timeout+`
|java.time.Duration
|
|


| `+neo4j.test.options.max-connection-lifetime+`
|java.time.Duration
|
|


| `+neo4j.test.options.max-connection-pool-size+`
|int
|
|


| `+neo4j.test.options.connection-acquisition-timeout+`
|java.time.Duration
|
|


| `+neo4j.test.options.encryption+`
|boolean
|
|


| `+neo4j.test.options.trust-strategy+`
|org.neo4j.driver.Config$TrustStrategy
|
|


| `+neo4j.test.options.routing-table-purge-delay+`
|java.time.Duration
|
|


| `+neo4j.test.options.fetch-size+`
|long
|
|


| `+neo4j.test.options.connection-timeout+`
|java.time.Duration
|
|


| `+neo4j.test.options.max-transaction-retry-time+`
|java.time.Duration
|
|


| `+neo4j.test.options.resolver+`
|org.neo4j.driver.net.ServerAddressResolver
|
|


| `+neo4j.test.options.driver-metrics+`
|boolean
|
|


| `+neo4j.test.options.metrics-adapter+`
|org.neo4j.driver.MetricsAdapter
|
|


| `+neo4j.test.options.event-loop-threads+`
|int
|
|


| `+neo4j.test.options.user-agent+`
|java.lang.String
|
|


| `+neo4j.test.options.notification-config+`
|org.neo4j.driver.NotificationConfig
|
|


| `+neo4j.test.options.telemetry-disabled+`
|boolean
|
|


|===
<<<'''
    }

    void "test inner"() {
        when:
            String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("foo.bar")
class MyConfigInner {

    private List<InnerVal> innerVals;

    public List<InnerVal> getInnerVals() {
        return innerVals;
    }

    public void setInnerVals(List<InnerVal> innerVals) {
        this.innerVals = innerVals;
    }

    public static class InnerVal {

        private Integer expireUnsignedSeconds;

        public Integer getExpireUnsignedSeconds() {
            return expireUnsignedSeconds;
        }

        public void setExpireUnsignedSeconds(Integer expireUnsignedSeconds) {
            this.expireUnsignedSeconds = expireUnsignedSeconds;
        }
    }

}
''')
        then:
            docs == '''
++++
<a id="test.MyConfigInner" href="#test.MyConfigInner">&#128279;</a>
++++
.Configuration Properties for api:test.MyConfigInner[]
|===
|Property |Type |Description |Default value

| `+foo.bar.inner-vals+`
|java.util.List
|
|


|===
<<<'''
    }

    void "test inheritance"() {
        when:
            String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;
import java.time.Duration;

@ConfigurationProperties("foo.bar")
class MyConfig extends ParentConfig {
    String host;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @ConfigurationProperties("baz")
    static class ChildConfig {
        String stuff;

        public String getStuff() {
            return stuff;
        }

        public void setStuff(String stuff) {
            this.stuff = stuff;
        }
    }
}

@ConfigurationProperties("parent")
class ParentConfig {

}
''')
        then:
            docs == '''
++++
<a id="test.MyConfig" href="#test.MyConfig">&#128279;</a>
++++
.Configuration Properties for api:test.MyConfig[]
|===
|Property |Type |Description |Default value

| `+parent.foo.bar.host+`
|java.lang.String
|
|


|===
<<<
++++
<a id="test.MyConfig$ChildConfig" href="#test.MyConfig$ChildConfig">&#128279;</a>
++++
.Configuration Properties for api:test.MyConfig$ChildConfig[]
|===
|Property |Type |Description |Default value

| `+parent.foo.bar.baz.stuff+`
|java.lang.String
|
|


|===
<<<'''
    }

    void "test inheritance empty"() {
        when:
            String docs = createAsciiDoc('''
package test;

import io.micronaut.context.annotation.*;
import java.time.Duration;

@ConfigurationProperties("foo.bar")
class MyConfig {
    String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @ConfigurationProperties("abc")
    class Abc extends SuperAbc {
    }

    static class SuperAbc {
        String stuff;

        public String getStuff() {
            return stuff;
        }

        public void setStuff(String stuff) {
            this.stuff = stuff;
        }
    }
}

''')
        then:
            docs == '''
++++
<a id="test.MyConfig" href="#test.MyConfig">&#128279;</a>
++++
.Configuration Properties for api:test.MyConfig[]
|===
|Property |Type |Description |Default value

| `+foo.bar.host+`
|java.lang.String
|
|


|===
<<<
++++
<a id="test.MyConfig$Abc" href="#test.MyConfig$Abc">&#128279;</a>
++++
.Configuration Properties for api:test.MyConfig$Abc[]
|===
|Property |Type |Description |Default value

| `+foo.bar.abc.stuff+`
|java.lang.String
|
|


|===
<<<'''
    }

}
