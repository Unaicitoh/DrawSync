package com.ugs.drawsync.server;

import com.ugs.drawsync.gui.GUIBoard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    public static final String USERS_REQUEST = "[SYNC_USERS_REQUEST=";
    public static final String SERVER_MESSAGE = "[SERVER_MESSAGE=";
    public static final String CLIENT_MESSAGE = "[CLIENT_MESSAGE=";
    public static final String SERVER_CLOSING = "[SERVER_CLOSING=";
    public static final String CLIENT_CLOSING = "[CLIENT_CLOSING=";
    private final Socket client;
    private final GUIBoard ui;
    private PrintWriter out;
    private ServerManager manager;
    private String username;

    public ClientHandler(Socket client, GUIBoard ui, ServerManager manager) {
        this.client = client;
        this.ui = ui;
        this.manager = manager;
    }

    public ClientHandler(Socket client, GUIBoard ui, String text) {
        this.client = client;
        this.ui = ui;
        this.username = text;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            if (manager == null) {
                out.println(SERVER_MESSAGE + "Welcome to the room " + username);
                out.println(USERS_REQUEST + username);
            }
            System.out.println("Connecting client " + client.getRemoteSocketAddress());
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("receiving message" + message);
                String formattedMessage = message.substring(message.indexOf("=") + 1);
                if (manager != null) {
                    serverSideHandler(message, formattedMessage);
                } else clientSideHandler(message, formattedMessage);
            }
        } catch (IOException e) {
            throw new IllegalThreadStateException();
        }
    }

    private void clientSideHandler(String message, String formattedMessage) {
        if (message.startsWith(CLIENT_MESSAGE) && !message.substring(message.indexOf("=") + 1, message.indexOf(":")).equals(username)) {
            System.out.println("Broadcast message" + message);
            insertChat(formattedMessage);
        } else if (message.startsWith(USERS_REQUEST)) {
            String users = formattedMessage.replace("|+*.", "\n");
            syncUsers(users);
        } else if (message.startsWith(SERVER_MESSAGE)) {
            System.out.println("Server sending message");
            insertChat(formattedMessage);
        } else if (message.startsWith(SERVER_CLOSING)) {
            insertChat(formattedMessage);
            syncUsers("[ROOM CLOSED] Online Users:\n - " + username);
        } else if (message.startsWith(CLIENT_CLOSING)) {
            removeUser(formattedMessage);
        }
    }

    private void serverSideHandler(String message, String formattedMessage) {
        if (message.startsWith(CLIENT_MESSAGE)) {
            System.out.println("Server broadcasting message");
            broadcastMessage(message);
            insertChat(formattedMessage);
        } else if (message.startsWith(SERVER_MESSAGE)) {
            broadcastMessage(message);
            insertChat(formattedMessage);
        } else if (message.startsWith(USERS_REQUEST)) {
            ui.getUsers().append("\n - " + formattedMessage);
            String users = ui.getUsers().getText().replace("\n", "|+*.");
            broadcastMessage(USERS_REQUEST + users);
        } else if (message.startsWith(CLIENT_CLOSING)) {
            broadcastMessage(message);
            removeUser(formattedMessage);
        }
    }

    private void removeUser(String formattedMessage) {
        insertChat(formattedMessage + " left the room.");
        int firstChar = ui.getUsers().getText().indexOf("\n - " + formattedMessage);
        String firstUsers = ui.getUsers().getText().substring(0, firstChar);
        String lastUsers = ui.getUsers().getText().substring(firstChar + 4 + formattedMessage.length());
        syncUsers(firstUsers.concat(lastUsers));
    }

    private void syncUsers(String users) {
        ui.getUsers().setText(users);
    }

    private static void broadcastMessage(String message) {
        ServerManager.broadcastMessage(message);
    }

    private void insertChat(String formattedMessage) {
        ui.getChat().append("\n " + formattedMessage);
    }

    public void sendMessage(String message) {
        System.out.println("Sending message" + message);
        out.println(message);
    }

    public Socket getClient() {
        return client;
    }
}
