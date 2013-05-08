package com.mastfrog.acteur.tutorial.v4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.ActeurFactory;
import com.mastfrog.acteur.Event;
import com.mastfrog.acteur.Page;
import com.mastfrog.acteur.auth.AuthenticateBasicActeur;
import com.mastfrog.acteur.util.Method;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

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
        add(WriteItemsActeur.class);
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

    static final class WriteItemsActeur extends Acteur implements ChannelFutureListener {

        private final DBCursor cursor;
        private final Event evt;
        private volatile boolean first = true;
        private final ObjectMapper mapper;

        @Inject
        WriteItemsActeur(DBCursor cursor, Event evt, ObjectMapper mapper) {
            this.mapper = mapper;
            this.cursor = cursor;
            this.evt = evt;
            setState(new RespondWith(HttpResponseStatus.OK));
            setResponseBodyWriter(this);
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (first) {
                future = future.channel().write(Unpooled.wrappedBuffer(new byte[] { (byte) '[' }));
            }
            if (cursor.hasNext()) {
                DBObject item = cursor.next();
                future = future.channel().write(Unpooled.copiedBuffer(
                        mapper.writeValueAsString(item.toMap()) 
                        + '\n', CharsetUtil.UTF_8));
                future.addListener(this);
            } else {
                future = future.channel().write(Unpooled.wrappedBuffer(new byte[] { (byte) ']' }));
                if (!evt.isKeepAlive()) {
                    future.addListener(CLOSE);
                }
            }
        }
    }
}
