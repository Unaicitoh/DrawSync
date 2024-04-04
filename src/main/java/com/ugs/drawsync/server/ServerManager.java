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
                System.out.println("Starting server ");
                while (true) {
                    Socket socket = server.accept();
                    ClientHandler client = new ClientHandler(socket, ui, this);
                    clients.add(client);
                    client.start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void broadcastMessage(String text) {
        clients.forEach(c -> c.sendMessage(text));
    }
}
