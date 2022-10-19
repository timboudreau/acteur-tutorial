package com.mastfrog.acteur.tutorial.v4;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.auth.Authenticator;
import com.mastfrog.acteur.header.entities.BasicCredentials;
import com.mastfrog.acteur.util.PasswordHasher;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.io.IOException;

/**
 * Handles authentication in concert with AuthenticateBasicActeur
 *
 * @author Tim Boudreau
 */
final class AuthenticatorImpl implements Authenticator {

    private final DBCollection users;
    private final PasswordHasher hasher;

    @Inject
    AuthenticatorImpl(@Named(value = "users") DBCollection users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    @Override
    public Object[] authenticate(String realm, BasicCredentials credentials) throws IOException {
        BasicDBObject query = new BasicDBObject("name", credentials.username);
        DBObject userRecord = users.findOne(query);
        if (userRecord != null) {
            String password = (String) userRecord.get("password");
            if (hasher.checkPassword(credentials.password, password)) {
                User user = new User(userRecord.get("_id") + "",
                        (String) userRecord.get("name"),
                        (String) userRecord.get("displayName"));
                System.out.println("RETURN USER " + userRecord);
                return new Object[]{user};
            }
        } else {
            // Security - ensure someone can't probe for what user ids are
            // valid by seeing that requests for non-existent users take less
            //time
            hasher.checkPassword("abcedefg", credentials.password);
        }
        return null;
    }
}
