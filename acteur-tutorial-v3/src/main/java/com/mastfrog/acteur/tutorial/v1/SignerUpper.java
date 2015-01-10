package com.mastfrog.acteur.tutorial.v1;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.annotations.HttpCall;
import static com.mastfrog.acteur.headers.Method.PUT;
import com.mastfrog.acteur.preconditions.InjectRequestBodyAs;
import com.mastfrog.acteur.preconditions.Methods;
import com.mastfrog.acteur.preconditions.PathRegex;
import com.mastfrog.acteur.preconditions.RequiredUrlParameters;
import static com.mastfrog.acteur.tutorial.v1.SignerUpper.SIGN_UP_PATTERN;
import com.mastfrog.acteur.util.PasswordHasher;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;

/**
 * New user sign-up.
 *
 * @author Tim Boudreau
 */
@HttpCall(scopeTypes = {User.class, DBCursor.class})
@Methods(PUT)
@PathRegex(SIGN_UP_PATTERN)
@RequiredUrlParameters("displayName")
@InjectRequestBodyAs(String.class)
final class SignerUpper extends Acteur {

    static final String SIGN_UP_PATTERN = "^users/(.*?)/signup$";

    @Inject
    SignerUpper(HttpEvent evt, String password, @Named("users") DBCollection users, PasswordHasher hasher) throws IOException {
        if (password.length() < 8) {
            badRequest("Password must be at least 8 characters");
            return;
        }
        String userName = evt.getPath().getElement(1).toString();
        String displayName = evt.getParameter("displayName");

        BasicDBObject query = new BasicDBObject("name", userName);
        DBObject result = users.findOne(query);
        if (result != null) {
            reply(HttpResponseStatus.CONFLICT, "A user named "
                    + userName + " exists\n");
        } else {
            query.put("password", hasher.encryptPassword(password));
            query.put("displayName", displayName);
            users.save(query);
            ok("Congratulations, "
                    + userName + ", you are  " + query.get("_id") + "\n");
        }
    }
}
