package com.mastfrog.acteur.tutorial.v1;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.Event;
import com.mastfrog.acteur.util.PasswordHasher;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;

/**
 * New user sign-up.
 *
 * @author Tim Boudreau
 */
final class SignerUpper extends Acteur {

    @Inject
    SignerUpper(Event evt, @Named("users") DBCollection users, PasswordHasher hasher) throws IOException {
        String password = evt.getContentAsJSON(String.class);
        if (password.length() < 8) {
            setState(new RespondWith(HttpResponseStatus.BAD_REQUEST,
                    "Password must be at least 8 characters"));
            return;
        }
        String userName = evt.getPath().getElement(1).toString();
        String displayName = evt.getParameter("displayName");

        BasicDBObject query = new BasicDBObject("name", userName);
        DBObject result = users.findOne(query);
        if (result != null) {
            setState(new RespondWith(HttpResponseStatus.CONFLICT, "A user named " 
                    + userName + " exists\n"));
        } else {
            query.put("password", hasher.encryptPassword(password));
            query.put("displayName", displayName);
            users.save(query);
            setState(new RespondWith(HttpResponseStatus.OK, "Congratulations, "
                    + userName + ", you are  " + query.get("_id") + "\n"));
        }
    }
}
