package com.mastfrog.acteur.tutorial.v4;

import com.google.inject.Inject;
import com.mastfrog.acteur.ActeurFactory;
import com.mastfrog.acteur.Page;
import com.mastfrog.acteur.util.Method;

/**
 *
 * @author Tim Boudreau
 */
public class SignUpPage extends Page {

    private static final String SIGN_UP_PATTERN = "^users/(.*?)/signup$";

    @Inject
    SignUpPage(ActeurFactory af) {
        add(af.matchMethods(Method.PUT));
        add(af.matchPath(SIGN_UP_PATTERN));
        add(af.requireParameters("displayName"));
        add(SignerUpper.class);
    }
}
