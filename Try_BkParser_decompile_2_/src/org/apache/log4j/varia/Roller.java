/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j.varia;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class Roller {
    static Logger cat = Logger.getLogger(class$org$apache$log4j$varia$Roller == null ? (class$org$apache$log4j$varia$Roller = Roller.class$("org.apache.log4j.varia.Roller")) : class$org$apache$log4j$varia$Roller);
    static String host;
    static int port;
    static /* synthetic */ Class class$org$apache$log4j$varia$Roller;

    Roller() {
    }

    public static void main(String[] argv) {
        BasicConfigurator.configure();
        if (argv.length == 2) {
            Roller.init(argv[0], argv[1]);
        } else {
            Roller.usage("Wrong number of arguments.");
        }
        Roller.roll();
    }

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + (class$org$apache$log4j$varia$Roller == null ? (class$org$apache$log4j$varia$Roller = Roller.class$("org.apache.log4j.varia.Roller")) : class$org$apache$log4j$varia$Roller).getName() + "host_name port_number");
        System.exit(1);
    }

    static void init(String hostArg, String portArg) {
        host = hostArg;
        try {
            port = Integer.parseInt(portArg);
        }
        catch (NumberFormatException e) {
            Roller.usage("Second argument " + portArg + " is not a valid integer.");
        }
    }

    static void roll() {
        try {
            Socket socket = new Socket(host, port);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            dos.writeUTF("RollOver");
            String rc = dis.readUTF();
            if ("OK".equals(rc)) {
                cat.info("Roll over signal acknowledged by remote appender.");
            } else {
                cat.warn("Unexpected return code " + rc + " from remote entity.");
                System.exit(2);
            }
        }
        catch (IOException e) {
            cat.error("Could not send roll signal on host " + host + " port " + port + " .", e);
            System.exit(2);
        }
        System.exit(0);
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

