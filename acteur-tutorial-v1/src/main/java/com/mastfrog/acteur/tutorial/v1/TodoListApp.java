package com.mastfrog.acteur.tutorial.v1;

import com.mastfrog.acteur.Application;
import com.mastfrog.acteur.server.ServerModule;
import java.io.IOException;

public class TodoListApp extends Application {
    
    TodoListApp() {
        add(SignUpPage.class);
        add(Application.helpPageType());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8134;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        ServerModule<TodoListApp> module = new ServerModule<>(TodoListApp.class);
        module.start(port);
    }
}
