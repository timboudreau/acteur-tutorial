package com.mastfrog.acteur.tutorial.v4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Tim Boudreau
 */
final class User {

    public final String id;
    public final String name;
    public final String displayName;

    @JsonCreator
    User(@JsonProperty("id") String id, @JsonProperty("name") String name,
            @JsonProperty(value = "displayName", required = false) String displayName) {

        this.id = id;
        this.name = name;
        this.displayName = displayName;
    }
}
