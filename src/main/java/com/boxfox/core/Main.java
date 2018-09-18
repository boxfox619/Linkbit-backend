package com.boxfox.core;

import io.vertx.core.Vertx;

public class Main {
    private static Vertx vertx;

    public static void main(String[] args) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
