package com.auction.client;

import javafx.application.Application;

/** Launcher - alternative entry point that launches the MainApp JavaFX application. */
public final class Launcher {

  private Launcher() {
    // Hide utility class constructor
  }

  public static void main(String[] args) {
    Application.launch(MainApp.class, args);
  }
}

