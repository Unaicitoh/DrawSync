package com.ugs.drawsync.server;

import java.io.Serial;
import java.io.Serializable;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private ActionType type;
    private String text;

    public Message(ActionType type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public static Message buildMessage(ActionType type, String text) {
        return new Message(type, text);
    }
}
