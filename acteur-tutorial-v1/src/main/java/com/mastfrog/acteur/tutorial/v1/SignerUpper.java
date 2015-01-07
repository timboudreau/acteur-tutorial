package com.mastfrog.acteur.tutorial.v1;

import com.google.inject.Inject;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.annotations.HttpCall;
import static com.mastfrog.acteur.headers.Method.PUT;
import com.mastfrog.acteur.preconditions.InjectRequestBodyAs;
import com.mastfrog.acteur.preconditions.Methods;
import com.mastfrog.acteur.preconditions.PathRegex;
import com.mastfrog.acteur.preconditions.RequiredUrlParameters;
import static com.mastfrog.acteur.tutorial.v1.SignerUpper.SIGN_UP_PATTERN;
import com.mastfrog.url.Path;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;

/**
 * New user sign-up.
 *
 * @author Tim Boudreau
 */
@HttpCall
@PathRegex(SIGN_UP_PATTERN)
@RequiredUrlParameters("displayName")
@Methods(PUT)
@InjectRequestBodyAs(String.class)
final class SignerUpper extends Acteur {

    static final String SIGN_UP_PATTERN = "^users/(.*?)/signup$";

    @Inject
    SignerUpper(String password, Path path) throws IOException {
        if (password.length() < 8) {
            setState(new RespondWith(HttpResponseStatus.BAD_REQUEST, "Password must be at least 8 characters"));
            return;
        }
        String userName = path.getElement(1).toString();
        setState(new RespondWith(HttpResponseStatus.OK, "Congratulations, "
                + userName + ", your password is " + password + "\n"));
    }
}
