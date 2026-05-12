package server.command;

import org.java_websocket.WebSocket;

import com.google.gson.JsonObject;

public interface Command {
    void execute(WebSocket conn, JsonObject jsonData);
}