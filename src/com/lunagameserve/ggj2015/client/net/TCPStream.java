package com.lunagameserve.ggj2015.client.net;

import com.lunagameserve.ggj2015.bombServer.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by six on 1/24/15.
 */
public class TCPStream {

    private final Socket socket;
    private AtomicBoolean listening = new AtomicBoolean(false);
    private Thread listenThread;

    private Queue<String> outgoingStrings = new ConcurrentLinkedDeque<>();
    private Queue<String> incomingStrings = new ConcurrentLinkedDeque<>();


    public TCPStream(byte[] ip, int port) {
        Socket trySocket = null;
        try {
            trySocket = new Socket(InetAddress.getByAddress(ip), port);
        } catch (IOException e) {
            Log.error("Cannot connect to IP: " + e.getMessage());
        }
        socket = trySocket;
    }

    public void start() {
        listening.set(true);
        listenThread = new Thread(this::listen);
        listenThread.start();
    }

    public void stop() {
        listening.set(false);
    }

    public void writeLine(String line) {
        outgoingStrings.add(line);
    }

    public String readLine() {
        if (incomingStrings.size() > 0) {
            return incomingStrings.remove();
        } else {
            return null;
        }
    }

    private void listen() {
        try {
            StringBuilder inputBuilder;
            while (listening.get()) {
                inputBuilder = new StringBuilder();
                while (socket.getInputStream().available() > 0) {
                    inputBuilder.append((char) socket.getInputStream().read());
                }
                Thread.sleep(100);

                if (outgoingStrings.size() > 0) {
                    socket.getOutputStream().write(outgoingStrings.remove().getBytes());
                }
            }
            socket.close();
        } catch (IOException e) {
            Log.error("TCP Thread IO Error: " + e.getMessage());
            System.exit(6);
        } catch (InterruptedException e) {
            Log.error("TCP Thread interrupted: " + e.getMessage());
            System.exit(7);
        }
    }
}
