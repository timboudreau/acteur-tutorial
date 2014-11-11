package com.mastfrog.acteur.tutorial.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.annotations.HttpCall;
import static com.mastfrog.acteur.headers.Method.GET;
import static com.mastfrog.acteur.headers.Method.HEAD;
import com.mastfrog.acteur.preconditions.Authenticated;
import com.mastfrog.acteur.preconditions.Methods;
import com.mastfrog.acteur.preconditions.Path;
import io.netty.handler.codec.http.HttpResponseStatus;

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
    WhoAmIPage(User user, ObjectMapper mapper) throws JsonProcessingException {
        setState(new RespondWith(HttpResponseStatus.OK, mapper.writeValueAsString(user)));
    }
}
