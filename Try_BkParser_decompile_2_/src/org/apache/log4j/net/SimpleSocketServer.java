/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j.net;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.net.SocketNode;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;

public class SimpleSocketServer {
    static Logger cat = Logger.getLogger(class$org$apache$log4j$net$SimpleSocketServer == null ? (class$org$apache$log4j$net$SimpleSocketServer = SimpleSocketServer.class$("org.apache.log4j.net.SimpleSocketServer")) : class$org$apache$log4j$net$SimpleSocketServer);
    static int port;
    static /* synthetic */ Class class$org$apache$log4j$net$SimpleSocketServer;

    public static void main(String[] argv) {
        if (argv.length == 2) {
            SimpleSocketServer.init(argv[0], argv[1]);
        } else {
            SimpleSocketServer.usage("Wrong number of arguments.");
        }
        try {
            cat.info("Listening on port " + port);
            ServerSocket serverSocket = new ServerSocket(port);
            do {
                cat.info("Waiting to accept a new client.");
                Socket socket = serverSocket.accept();
                cat.info("Connected to client at " + socket.getInetAddress());
                cat.info("Starting new socket node.");
                new Thread((Runnable)new SocketNode(socket, LogManager.getLoggerRepository()), "SimpleSocketServer-" + port).start();
            } while (true);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + (class$org$apache$log4j$net$SimpleSocketServer == null ? (class$org$apache$log4j$net$SimpleSocketServer = SimpleSocketServer.class$("org.apache.log4j.net.SimpleSocketServer")) : class$org$apache$log4j$net$SimpleSocketServer).getName() + " port configFile");
        System.exit(1);
    }

    static void init(String portStr, String configFile) {
        try {
            port = Integer.parseInt(portStr);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            SimpleSocketServer.usage("Could not interpret port number [" + portStr + "].");
        }
        if (configFile.endsWith(".xml")) {
            DOMConfigurator.configure(configFile);
        } else {
            PropertyConfigurator.configure(configFile);
        }
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        }
        catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }
}

