package com.mastfrog.acteur.tutorial.v1;

import com.google.inject.AbstractModule;
import com.mastfrog.acteur.auth.Authenticator;
import com.mastfrog.acteur.mongo.MongoModule;
import com.mastfrog.acteur.server.ServerBuilder;
import java.io.IOException;

public class TodoListApp extends AbstractModule {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8134;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new ServerBuilder()
                .enableHelp()
                .add(new TodoListApp())
                .build().start(port).await();
    }

    @Override
    protected void configure() {
        install(new MongoModule("todo")
                .bindCollection("users", "todoUsers")
                .bindCollection("todo", "todo"));
        bind(Authenticator.class).to(AuthenticatorImpl.class);
    }
}
