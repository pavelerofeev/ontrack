plugins {
    groovy
}

dependencies {
    compile(project(":ontrack-common"))
    compile(project(":ontrack-json"))
    compile(project(":ontrack-job"))
    compile("com.google.guava:guava")
    compile("org.apache.commons:commons-text")
    compile("org.springframework:spring-context")
    compile("org.springframework.security:spring-security-core")
    compile("javax.validation:validation-api")
    compile("org.slf4j:slf4j-api")
    compile("org.springframework.boot:spring-boot-starter-actuator")

    testCompile("org.codehaus.groovy:groovy")
    testCompile(project(":ontrack-test-utils"))
}