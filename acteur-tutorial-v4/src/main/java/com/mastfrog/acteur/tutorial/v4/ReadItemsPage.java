package com.mastfrog.acteur.tutorial.v4;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.annotations.Concluders;
import com.mastfrog.acteur.annotations.HttpCall;
import com.mastfrog.acteur.mongo.CursorWriterActeur;
import static com.mastfrog.acteur.headers.Method.GET;
import com.mastfrog.acteur.preconditions.Authenticated;
import com.mastfrog.acteur.preconditions.Methods;
import com.mastfrog.acteur.preconditions.PathRegex;
import static com.mastfrog.acteur.tutorial.v4.CreateItemPage.ITEM_PATTERN;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 *
 * @author Tim Boudreau
 */
@HttpCall
@Authenticated
@PathRegex(ITEM_PATTERN)
@Methods(GET)
@Concluders(CursorWriterActeur.class)
public class ReadItemsPage extends Acteur {

    @Inject
    ReadItemsPage(@Named("todo") DBCollection collection, HttpEvent evt, User user, BasicDBObject query) {
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
