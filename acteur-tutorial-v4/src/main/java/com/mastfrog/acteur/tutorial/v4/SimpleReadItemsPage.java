package com.mastfrog.acteur.tutorial.v4;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.ActeurFactory;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.Page;
import com.mastfrog.acteur.auth.AuthenticateBasicActeur;
import com.mastfrog.acteur.headers.Method;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 *
 * @author Tim Boudreau
 */
public class SimpleReadItemsPage extends Page {
    @Inject
    SimpleReadItemsPage(ActeurFactory af) {
        add(af.matchPath(CreateItemPage.ITEM_PATTERN));
        add(af.matchMethods(Method.GET));
        add(AuthenticateBasicActeur.class);
        add(FindItemsActeur.class);
    }

    private static final class FindItemsActeur extends Acteur {

        @Inject
        FindItemsActeur(@Named("todo") DBCollection collection, HttpEvent evt, User user, BasicDBObject query) {
            String owner = evt.getPath().getElement(1).toString();
            if (!owner.equals(user.name)) {
                // For the future
                setState(new RespondWith(HttpResponseStatus.FORBIDDEN, user.name
                        + " cannot add items belonging to " + owner));
            }
            DBCursor cursor = collection.find(query).snapshot();
            setState(new RespondWith(HttpResponseStatus.OK, cursor.toArray()));
        }
    }
}
