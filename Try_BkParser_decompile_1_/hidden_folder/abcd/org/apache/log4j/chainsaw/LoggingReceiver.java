/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j.chainsaw;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.apache.log4j.Logger;
import org.apache.log4j.chainsaw.EventDetails;
import org.apache.log4j.chainsaw.MyTableModel;
import org.apache.log4j.spi.LoggingEvent;

class LoggingReceiver
extends Thread {
    private static final Logger LOG = Logger.getLogger(class$org$apache$log4j$chainsaw$LoggingReceiver == null ? (class$org$apache$log4j$chainsaw$LoggingReceiver = LoggingReceiver.class$("org.apache.log4j.chainsaw.LoggingReceiver")) : class$org$apache$log4j$chainsaw$LoggingReceiver);
    private MyTableModel mModel;
    private ServerSocket mSvrSock;
    static /* synthetic */ Class class$org$apache$log4j$chainsaw$LoggingReceiver;

    LoggingReceiver(MyTableModel aModel, int aPort) throws IOException {
        this.setDaemon(true);
        this.mModel = aModel;
        this.mSvrSock = new ServerSocket(aPort);
    }

    public void run() {
        LOG.info("Thread started");
        try {
            do {
                LOG.debug("Waiting for a connection");
                Socket client = this.mSvrSock.accept();
                LOG.debug("Got a connection from " + client.getInetAddress().getHostName());
                Thread t = new Thread(new Slurper(client));
                t.setDaemon(true);
                t.start();
            } while (true);
        }
        catch (IOException e) {
            LOG.error("Error in accepting connections, stopping.", e);
            return;
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

    private class Slurper
    implements Runnable {
        private final Socket mClient;

        Slurper(Socket aClient) {
            this.mClient = aClient;
        }

        public void run() {
            LOG.debug("Starting to get data");
            try {
                ObjectInputStream ois = new ObjectInputStream(this.mClient.getInputStream());
                do {
                    LoggingEvent event = (LoggingEvent)ois.readObject();
                    LoggingReceiver.this.mModel.addEvent(new EventDetails(event));
                } while (true);
            }
            catch (EOFException e) {
                LOG.info("Reached EOF, closing connection");
            }
            catch (SocketException e) {
                LOG.info("Caught SocketException, closing connection");
            }
            catch (IOException e) {
                LOG.warn("Got IOException, closing connection", e);
            }
            catch (ClassNotFoundException e) {
                LOG.warn("Got ClassNotFoundException, closing connection", e);
            }
            try {
                this.mClient.close();
            }
            catch (IOException e) {
                LOG.warn("Error closing connection", e);
            }
        }
    }

}

