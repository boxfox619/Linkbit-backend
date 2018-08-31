package com.boxfox.cross.common.vertx.middleware;

import com.boxfox.cross.common.vertx.router.Param;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseHandlerImpl implements BaseHandler {
    private Object instance;
    private Method m;

    public BaseHandlerImpl(Object instance, Method m) {
        this.instance = instance;
        this.m = m;
    }

    private Object castingParameter(String str, Class<?> paramType) {
        Object paramData = null;
        if (str != null) {
            if (paramType.equals(Integer.class) || paramType.equals(int.class)) {
                paramData = Integer.valueOf(str);
            } else if (paramType.equals(Boolean.class) || paramType.equals(boolean.class)) {
                paramData = Boolean.valueOf(str);
            } else if (paramType.equals(Double.class) || paramType.equals(double.class)) {
                paramData = Double.valueOf(str);
            } else if (paramType.equals(Float.class) || paramType.equals(float.class)) {
                paramData = Float.valueOf(str);
            } else if (paramType.equals(JsonObject.class)) {
                paramData = new JsonObject(str);
            } else if (paramType.equals(JsonArray.class)) {
                paramData = new JsonArray(str);
            } else {
                paramData = str;
            }
        }
        return paramData;
    }

    private Object getParameterFromBody(RoutingContext ctx, String paramName, Class<?> paramType) {
        Object paramData = null;
        String data = ctx.request().getFormAttribute(paramName);
        if (ctx.request().method() == HttpMethod.POST && data != null) {
            if (paramType.equals(String.class)) {
                paramData = data;
            } else if (paramType.equals(Integer.class) || paramType.equals(int.class)) {
                paramData = Integer.valueOf(data);
            } else if (paramType.equals(Boolean.class) || paramType.equals(boolean.class)) {
                paramData = Boolean.valueOf(data);
            } else if (paramType.equals(Double.class) || paramType.equals(double.class)) {
                paramData = Double.valueOf(data);
            } else if (paramType.equals(Float.class) || paramType.equals(float.class)) {
                paramData = Float.valueOf(data);
            } else if (paramData.equals(JsonObject.class)) {
                paramData = new JsonObject(data);
            } else if (paramData.equals(JsonArray.class)) {
                paramData = new JsonArray(data);
            } else if (paramData.equals(byte[].class)) {
                paramData = Byte.valueOf(data);
            }
        }
        return paramData;
    }

    @Override
    public void handle(RoutingContext ctx) {
        List<Object> argments = new ArrayList<>();
        Arrays.stream(m.getParameters()).forEach(param -> {
            Class<?> paramClass = param.getType();
            if (paramClass.equals(RoutingContext.class)) {
                argments.add(ctx);
            } else {
                String paramName = param.getName();
                Object paramData = null;
                if (param.getAnnotation(Param.class) != null) {
                    paramData = getParameterFromBody(ctx, paramName, paramClass);
                    if (paramData == null)
                        paramData = castingParameter(ctx.pathParam(paramName), paramClass);
                    if (paramData == null && ctx.queryParam(paramName).size() > 0)
                        paramData = castingParameter(ctx.queryParam(paramName).get(0), paramClass);
                }
                argments.add(paramData);
            }
        });
        try {
            m.invoke(instance, argments.toArray());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
        }
    }
}
