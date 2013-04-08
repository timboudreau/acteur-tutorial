package com.mastfrog.acteur.tutorial.v1;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mastfrog.acteur.Event;
import com.mongodb.BasicDBObject;
import java.util.Map;
import org.bson.types.ObjectId;

/**
 *
 * @author Tim Boudreau
 */
final class ListItemsQuery implements Provider<BasicDBObject> {
    private final Provider<Event> eventProvider;
    @Inject
    public ListItemsQuery(Provider<Event> eventProvider) {
        this.eventProvider = eventProvider;
    }
    @Override
    public BasicDBObject get() {
        Event event = eventProvider.get();
        BasicDBObject result = new BasicDBObject();
        for (Map.Entry<String, String> e : event.getParametersAsMap().entrySet()) {
            switch(e.getKey()) {
                case "_id" :
                case "creator" :
                    result.put(e.getKey(), new ObjectId(e.getValue()));
                    break;
                case "created" :
                case "lastModified" :
                    result.put(e.getKey(), Long.parseLong(e.getValue()));
                    break;
                case "done" :
                    result.put(e.getKey(), Boolean.parseBoolean(e.getValue()));
                    break;
                default :
                    result.put(e.getKey(), e.getValue());
            }
        }
        result.put("type", "todo");
        return result;
    }
}
