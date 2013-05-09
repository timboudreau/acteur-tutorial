package com.mastfrog.acteur.tutorial.v4;

import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import com.mastfrog.giulius.tests.GuiceRunner;
import com.mastfrog.giulius.tests.TestWith;
import com.mastfrog.netty.http.client.StateType;
import com.mastfrog.netty.http.test.harness.TestHarness;
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
@TestWith({TodoListApp.TodoListModule.class})
public class TodoListAppTest {

    @Test
    @SuppressWarnings("unchecked")
    public void test(TestHarness harness) throws Throwable {
        if (true) return; //need a local mongodb
        String username = "joe" + Long.toString(System.currentTimeMillis(), 36);
        harness.put("users", username, "signup")
                .addQueryPair("displayName", "Joe Blow")
                .setBody("password", PLAIN_TEXT_UTF_8)
                .go()
                .assertStateSeen(StateType.Closed)
                .assertStatus(OK);
        
        Map<String,Object> item = (Map<String,Object>) harness
                .put("users", username, "items")
                .addQueryPair("title", "Do stuff")
                .basicAuthentication(username, "password")
                .go()
                .assertStateSeen(StateType.Closed)
                .assertStatus(CREATED)
                .content(Map.class);
        
        Map[] items = (Map[]) harness
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
