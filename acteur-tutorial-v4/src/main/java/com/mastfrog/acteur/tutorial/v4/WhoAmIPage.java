package com.mastfrog.acteur.tutorial.v4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.annotations.HttpCall;
import static com.mastfrog.acteur.headers.Method.GET;
import static com.mastfrog.acteur.headers.Method.HEAD;
import com.mastfrog.acteur.preconditions.Authenticated;
import com.mastfrog.acteur.preconditions.Methods;
import com.mastfrog.acteur.preconditions.Path;

/**
 *
 * @author Tim Boudreau
 */
@HttpCall
@Path("who")
@Methods({GET, HEAD})
@Authenticated
public class WhoAmIPage extends Acteur {

    @Inject
    WhoAmIPage(User user) throws JsonProcessingException {
        ok(user);
    }
}
