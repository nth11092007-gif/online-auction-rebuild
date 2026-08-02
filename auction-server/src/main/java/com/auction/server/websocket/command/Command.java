package com.auction.server.websocket.command;

import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;

/** Command - interface for executing WebSocket command handlers. */
public interface Command {
  void execute(WebSocket conn, JsonObject jsonData);
}
