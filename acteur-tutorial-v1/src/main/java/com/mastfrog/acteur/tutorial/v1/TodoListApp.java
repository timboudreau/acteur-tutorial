package com.mastfrog.acteur.tutorial.v1;

import com.mastfrog.acteur.server.ServerBuilder;
import com.mastfrog.acteur.util.ServerControl;
import java.io.IOException;

public class TodoListApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8134;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        ServerControl control = new ServerBuilder()
                .enableHelp()
                .build()
                .start(port);
        control.await();
    }
}
