package com.mastfrog.acteur.tutorial.v4;

import com.mastfrog.acteur.Application;
import com.mastfrog.acteur.ImplicitBindings;
import com.mastfrog.acteur.auth.Authenticator;
import com.mastfrog.acteur.mongo.MongoModule;
import com.mastfrog.acteur.server.ServerModule;
import com.mastfrog.jackson.JacksonModule;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import java.io.IOException;

@ImplicitBindings({User.class, DBCursor.class})
public class TodoListApp extends Application {

    TodoListApp() {
        add(SignUpPage.class);
        add(WhoAmIPage.class);
        add(CreateItemPage.class);
//        add(ReadItemsPage.class);
        add(SimpleReadItemsPage.class);
        add(Application.helpPageType());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8134;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        ServerModule<TodoListApp> module = new TodoListModule();
        module.add(new MongoModule("todo")
                .bindCollection("users", "todoUsers")
                .bindCollection("todo", "todo"));
        module.start(port).await();
    }

    static class TodoListModule extends ServerModule<TodoListApp> {

        TodoListModule() {
            super(TodoListApp.class);
        }

        @Override
        protected void configure() {
            super.configure();
            bind(Authenticator.class).to(AuthenticatorImpl.class);
            bind(BasicDBObject.class).toProvider(ListItemsQuery.class);
            install(new JacksonModule());
        }
    }
}
