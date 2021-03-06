package com.mastfrog.acteur.tutorial.v4;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.annotations.HttpCall;
import static com.mastfrog.acteur.headers.Method.PUT;
import com.mastfrog.acteur.preconditions.Methods;
import com.mastfrog.acteur.preconditions.PathRegex;
import com.mastfrog.acteur.preconditions.RequiredUrlParameters;
import static com.mastfrog.acteur.tutorial.v4.SignerUpper.SIGN_UP_PATTERN;
import com.mastfrog.acteur.util.PasswordHasher;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * New user sign-up.
 *
 * @author Tim Boudreau
 */
@HttpCall
@Methods(PUT)
@PathRegex(SIGN_UP_PATTERN)
@RequiredUrlParameters("displayName")
final class SignerUpper extends Acteur {

    static final String SIGN_UP_PATTERN = "^users/(.*?)/signup$";

    @Inject
    SignerUpper(HttpEvent evt, @Named("users") DBCollection users, PasswordHasher hasher) throws Exception {
        String password = evt.jsonContent(String.class);
        if (password.length() < 8) {
            badRequest("Password must be at least 8 characters");
            return;
        }
        String userName = evt.path().getElement(1).toString();
        String displayName = evt.urlParameter("displayName");

        BasicDBObject query = new BasicDBObject("name", userName);
        DBObject result = users.findOne(query);
        if (result != null) {
            reply(HttpResponseStatus.CONFLICT, "A user named "
                    + userName + " exists\n");
        } else {
            query.put("password", hasher.encryptPassword(password));
            query.put("displayName", displayName);
            query.put("lastModified", System.currentTimeMillis());
            users.save(query);
            ok("Congratulations, "+ userName + ", you are  " 
                    + query.get("_id") + "\n");
        }
    }
}
