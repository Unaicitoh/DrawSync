package com.ugs.drawsync.server;

import com.ugs.drawsync.gui.GUIBoard;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerManager {
    private final int port;
    private ServerSocket server;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public ServerManager(int port) {
        this.port = port;
    }

    public void startServer(GUIBoard ui) {
        new Thread(() -> {
            try {
                server = new ServerSocket(port);
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    ClientHandler client = new ClientHandler(socket, ui, this);
                    clients.add(client);
                    client.start();
                }
            } catch (IOException e) {
                System.err.println("Closing server");
            }
        }).start();
    }

    public static void broadcastMessage(Message text) {
        for (int i = 0; i < clients.size(); i++) {
            try {
                clients.get(i).sendMessage(text);
            } catch (IOException e) {
                clients.remove(clients.get(i));
            }
        }
    }

    public ServerSocket getConnection() {
        return server;
    }
}
