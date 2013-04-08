package com.mastfrog.acteur.tutorial.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import com.google.inject.Inject;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.ActeurFactory;
import com.mastfrog.acteur.Page;
import com.mastfrog.acteur.auth.AuthenticateBasicActeur;
import com.mastfrog.acteur.util.Method;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 *
 * @author Tim Boudreau
 */
public class WhoAmIPage extends Page {

    private static final String WHO_AM_I_PATTERN = "^who";

    @Inject
    WhoAmIPage(ActeurFactory af) {
        add(af.matchMethods(Method.GET, Method.HEAD));
        add(af.matchPath(WHO_AM_I_PATTERN));
        add(AuthenticateBasicActeur.class);
        add(WhoAmIActeur.class);
        getReponseHeaders().setContentType(MediaType.JSON_UTF_8);
    }

    private static final class WhoAmIActeur extends Acteur {
        @Inject
        WhoAmIActeur(User user, ObjectMapper mapper) throws JsonProcessingException {
            setState(new RespondWith(HttpResponseStatus.OK, mapper.writeValueAsString(user)));
        }
    }
}
