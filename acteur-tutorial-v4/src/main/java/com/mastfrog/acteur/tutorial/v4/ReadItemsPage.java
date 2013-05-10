package com.mastfrog.acteur.tutorial.v4;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.ActeurFactory;
import com.mastfrog.acteur.Event;
import com.mastfrog.acteur.Page;
import com.mastfrog.acteur.auth.AuthenticateBasicActeur;
import com.mastfrog.acteur.mongo.CursorWriterActeur;
import com.mastfrog.acteur.util.Method;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 *
 * @author Tim Boudreau
 */
public class ReadItemsPage extends Page {

    @Inject
    ReadItemsPage(ActeurFactory af) {
        add(af.matchPath(CreateItemPage.ITEM_PATTERN));
        add(af.matchMethods(Method.GET));
        add(AuthenticateBasicActeur.class);
        add(FindItemsActeur.class);
        // CursorWriterActeur is a generic class in acteur-mongdb which iterates
        // a cursor, writing out each object as JSON
        add(CursorWriterActeur.class);
    }

    private static final class FindItemsActeur extends Acteur {

        @Inject
        FindItemsActeur(@Named("todo") DBCollection collection, Event evt, User user, BasicDBObject query) {
            String owner = evt.getPath().getElement(1).toString();
            if (!owner.equals(user.name)) {
                // For the future
                setState(new RespondWith(HttpResponseStatus.FORBIDDEN, user.name
                        + " cannot add items belonging to " + owner));
            }
            DBCursor cursor = collection.find(query);
            if (!cursor.hasNext()) {
                // No items, bail here
                setState(new RespondWith(HttpResponseStatus.OK, "[]\n"));
            } else {
                setState(new ConsumedLockedState(cursor));
            }
        }
    }
}
