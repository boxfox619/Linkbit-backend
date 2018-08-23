package com.boxfox.cross.common.vertx.router;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AbstractRouter {
    protected Gson gson;

    public AbstractRouter(){
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }
}
