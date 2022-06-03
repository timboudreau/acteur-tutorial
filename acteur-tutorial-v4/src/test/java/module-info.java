// Generated by com.dv.sourcetreetool.impl.App
open module com.mastfrog.acteur.tutorial.v4 {
    exports com.mastfrog.acteur.tutorial.v4;

    // derived from com.google.guava/guava-31.1-jre in com/google/guava/guava/31.1-jre/guava-31.1-jre.pom
    requires transitive com.google.common;

    // Transitive detected by source scan
    requires com.mastfrog.acteur.auth;

    // Transitive detected by source scan
    requires com.mastfrog.acteur.deprecated;

    // Sibling com.mastfrog/acteur-headers-3.0.0-dev
    requires com.mastfrog.acteur.headers;

    // Sibling com.mastfrog/acteur-mongo-3.0.0-dev
    requires com.mastfrog.acteur.mongo;

    // Sibling com.mastfrog/giulius-tests-3.0.0-dev
    requires com.mastfrog.giulius.tests;

    // Sibling com.mastfrog/jackson-3.0.0-dev
    requires com.mastfrog.jackson;

    // Sibling com.mastfrog/netty-http-test-harness-3.0.0-dev
    requires com.mastfrog.netty.http.test.harness;

    // Inferred from test-source-scan
    requires transitive junit;

    // derived from org.mongodb/mongo-java-driver-0.0.0-? in org/mongodb/mongo-java-driver/3.12.11/mongo-java-driver-3.12.11.pom
    requires transitive mongo.java.driver.3.12.11;

}
