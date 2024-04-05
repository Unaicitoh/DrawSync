package com.ugs.drawsync.server;

import com.ugs.drawsync.gui.GUIBoard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket client;
    private final GUIBoard ui;
    private ObjectOutputStream out;
    private ObjectInputStream in;
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
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());
            System.out.println("Connecting client " + client.getRemoteSocketAddress());
            if (manager == null) {
                out.writeObject(Message.buildMessage(ActionType.SERVER_MESSAGE, "Welcome to the room " + username));
                out.writeObject(Message.buildMessage(ActionType.USERS_REQUEST, username));
            }
            Message message;
            while (!client.isClosed() && (message = (Message) in.readObject()) != null) {
                if (manager != null) {
                    serverSideHandler(message);
                } else clientSideHandler(message);
            }

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void clientSideHandler(Message message) throws IOException {
        String text = message.getText();
        switch (message.getType()) {
            case CLIENT_MESSAGE -> {
                if (!text.substring(0, text.indexOf(":")).equals(username)) {
                    insertChat(text);
                }
            }
            case SERVER_MESSAGE -> insertChat(text);
            case USERS_REQUEST -> syncUsers(text);
            case CLIENT_CLOSING -> removeUser(text);
            case SERVER_CLOSING -> {
                insertChat(text);
                syncUsers("[ROOM CLOSED] Online Users:\n - " + username);
                closeConnection();
            }
        }
    }

    private void serverSideHandler(Message message) {
        String text = message.getText();
        switch (message.getType()) {
            case CLIENT_MESSAGE, SERVER_MESSAGE -> {
                ServerManager.broadcastMessage(message);
                insertChat(text);
            }
            case USERS_REQUEST -> {
                ui.getUsers().append("\n - " + text);
                message.setText(ui.getUsers().getText());
                ServerManager.broadcastMessage(message);
            }
            case CLIENT_CLOSING -> {
                ServerManager.broadcastMessage(message);
                removeUser(text);
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + message.getType());
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

    private void insertChat(String formattedMessage) {
        ui.getChat().append("\n " + formattedMessage);
    }

    public void sendMessage(Message message) throws IOException {
        System.out.println("Sending message" + message);
        out.writeObject(message);
    }

    private void closeConnection() throws IOException {
        if (out != null) {
            out.close();
        }
        if (in != null) {
            in.close();
        }
        if (client != null) {
            client.close();
        }
    }

    public Socket getConnection() {
        return client;
    }
}
