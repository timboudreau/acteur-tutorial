package com.mastfrog.acteur.tutorial.v1;

import com.google.inject.AbstractModule;
import com.mastfrog.acteur.Application;
import com.mastfrog.acteur.ImplicitBindings;
import com.mastfrog.acteur.auth.Authenticator;
import com.mastfrog.acteur.mongo.MongoModule;
import com.mastfrog.acteur.server.ServerModule;
import java.io.IOException;

@ImplicitBindings(User.class)
public class TodoListApp extends Application {

    TodoListApp() {
        add(SignUpPage.class);
        add(WhoAmIPage.class);
        add(Application.helpPageType());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8134;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        ServerModule<TodoListApp> module = new ServerModule<>(TodoListApp.class);
        module.add(new MongoModule("todo")
                .bindCollection("users", "todoUsers")
                .bindCollection("todo", "todo"));

        module.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Authenticator.class).to(AuthenticatorImpl.class);
            }
        });

        module.start(port).await();
    }
}
