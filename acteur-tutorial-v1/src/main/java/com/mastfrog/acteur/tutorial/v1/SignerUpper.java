package com.mastfrog.acteur.tutorial.v1;

import com.google.inject.Inject;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.HttpEvent;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;

/**
 * New user sign-up.
 *
 * @author Tim Boudreau
 */
final class SignerUpper extends Acteur {

    @Inject
    SignerUpper(HttpEvent evt) throws IOException {
        String password = evt.getContentAsJSON(String.class);
        if (password.length() < 8) {
            setState(new RespondWith(HttpResponseStatus.BAD_REQUEST, "Password must be at least 8 characters"));
            return;
        }
        String userName = evt.getPath().getElement(1).toString();
        setState(new RespondWith(HttpResponseStatus.OK, "Congratulations, " 
                + userName + ", your password is " + password + "\n"));
    }
}
