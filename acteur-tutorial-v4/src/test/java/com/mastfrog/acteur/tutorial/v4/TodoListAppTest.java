package com.mastfrog.acteur.tutorial.v4;

import com.mastfrog.acteur.annotations.GenericApplicationModule;
import com.mastfrog.acteur.mongo.MongoHarness;
import com.mastfrog.acteur.tutorial.v4.TodoListAppTest.GAM;
import com.mastfrog.giulius.tests.GuiceRunner;
import com.mastfrog.giulius.tests.anno.IfBinaryAvailable;
import com.mastfrog.giulius.tests.anno.TestWith;
import com.mastfrog.mime.MimeType;
import com.mastfrog.netty.http.client.StateType;
import com.mastfrog.netty.http.test.harness.TestHarness;
import com.mastfrog.netty.http.test.harness.TestHarnessModule;
import com.mastfrog.settings.Settings;
import com.mongodb.DBCursor;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import java.util.Arrays;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Tim Boudreau
 */
@RunWith(GuiceRunner.class)
@TestWith({TodoListApp.class, MongoHarness.Module.class, TestHarnessModule.class, GAM.class})
@IfBinaryAvailable("mongod")
public class TodoListAppTest {

    static class GAM extends GenericApplicationModule {

        public GAM(Settings settings) {
            super(settings);
        }

        @Override
        protected void configure() {
            super.configure();
            scope.bindTypes(binder(), User.class, DBCursor.class);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test(TestHarness harness, MongoHarness mongo) throws Throwable {
        if (mongo.failed()) {
            System.out.println("Mongodb could not start - not installed?");
            return;
        }
        String username = "joe" + Long.toString(System.currentTimeMillis(), 36);
        harness.put("users", username, "signup")
                .addQueryPair("displayName", "Joe Blow")
                .setBody("password", MimeType.PLAIN_TEXT_UTF_8)
                .log()
                .go()
                .await()
                .assertStateSeen(StateType.FullContentReceived)
                .assertStatus(OK);

        Map<String, Object> item = (Map<String, Object>) harness
                .put("users", username, "items")
                .addQueryPair("title", "Do stuff")
                .basicAuthentication(username, "password")
                .go()
                .assertStateSeen(StateType.FullContentReceived)
                .assertStatus(CREATED)
                .content(Map.class);

        Map[] items = harness
                .get("users", username, "items")
                .basicAuthentication(username, "password")
                .addQueryPair("creator", (String) item.get("creator"))
                .go()
                .assertStatus(OK).content(Map[].class);

        System.out.println("GOT BACK " + Arrays.asList(items));

        assertTrue(Arrays.asList(items).contains(item));
        assertEquals(1, items.length);
    }

}
