package com.mastfrog.acteur.tutorial.v4;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.ActeurFactory;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.Page;
import com.mastfrog.acteur.auth.AuthenticateBasicActeur;
import com.mastfrog.acteur.util.Method;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.bson.types.ObjectId;
import org.joda.time.DateTimeUtils;

/**
 *
 * @author Tim Boudreau
 */
final class CreateItemPage extends Page {
    static final String ITEM_PATTERN = "^users/(.*?)/items";
    @Inject
    CreateItemPage(ActeurFactory af) {
        add (af.matchPath(ITEM_PATTERN));
        add (af.matchMethods(Method.PUT));
        add (af.banParameters("_id", "creator", "lastModified", "created"));
        add (af.requireParameters("title"));
        add (AuthenticateBasicActeur.class);
        add (AddItemActeur.class);
    }
    private static final class AddItemActeur extends Acteur {
        @Inject
        AddItemActeur(BasicDBObject item, @Named("todo") DBCollection collection, User user, HttpEvent evt) {
            String owner = evt.getPath().getElement(1).toString();
            if (!owner.equals(user.name)) {
                // For the future
                setState(new RespondWith(HttpResponseStatus.FORBIDDEN, user.name 
                        + " cannot add items belonging to " + owner));
            }
            long now = DateTimeUtils.currentTimeMillis();
            item.put("creator", new ObjectId(user.id));
            item.put("lastModified", now);
            item.put("created", now);
            item.put("done", false);
            collection.save(item);
            setState(new RespondWith(HttpResponseStatus.CREATED, item.toMap()));
        }
    }
}
