package com.mastfrog.acteur.tutorial.v4;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.Acteur.RespondWith;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.annotations.HttpCall;
import static com.mastfrog.acteur.headers.Method.PUT;
import com.mastfrog.acteur.preconditions.Authenticated;
import com.mastfrog.acteur.preconditions.BannedUrlParameters;
import com.mastfrog.acteur.preconditions.Methods;
import com.mastfrog.acteur.preconditions.PathRegex;
import com.mastfrog.acteur.preconditions.RequiredUrlParameters;
import static com.mastfrog.acteur.tutorial.v4.CreateItemPage.ITEM_PATTERN;
import com.mastfrog.util.time.TimeUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.time.ZonedDateTime;
import org.bson.types.ObjectId;

/**
 *
 * @author Tim Boudreau
 */
@HttpCall
@Authenticated
@Methods(PUT)
@PathRegex(ITEM_PATTERN)
@BannedUrlParameters({"_id", "creator", "lastModified", "created"})
@RequiredUrlParameters("title")
final class CreateItemPage extends Acteur {

    static final String ITEM_PATTERN = "^users/(.*?)/items";

    @Inject
    CreateItemPage(BasicDBObject item, @Named("todo") DBCollection collection, User user, HttpEvent evt) {
        String owner = evt.path().getElement(1).toString();
        if (!owner.equals(user.name)) {
            // For the future
            setState(new RespondWith(HttpResponseStatus.FORBIDDEN, user.name
                    + " cannot add items belonging to " + owner));
        }
        long now = System.currentTimeMillis();
        item.put("creator", new ObjectId(user.id));
        item.put("lastModified", now);
        item.put("created", now);
        item.put("done", false);
        collection.save(item);
        setState(new RespondWith(HttpResponseStatus.CREATED, item.toMap()));
    }
}
