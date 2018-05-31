package com.boxfox.cross.common.vertx.router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.boxfox.cross.common.vertx.middleware.JWTAuthHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.reflections.Reflections;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

/* boxfox 2017.02.13*/

public class RouteRegister {
    private List<RouterContext> routerList;
    private Router router;

    public static RouteRegister routing(Vertx vertx) {
        return new RouteRegister(Router.router(vertx));
    }

    private RouteRegister(Router router) {
        this.router = router;
        this.routerList = new ArrayList();
    }

    public void route(Handler<RoutingContext> handler) {
        router.route().handler(handler);
    }

    public void route(String... packages) {
        Arrays.stream(packages).forEach(packageName -> {
            Reflections routerAnnotations = new Reflections(packageName, new TypeAnnotationsScanner(), new SubTypesScanner(), new MethodAnnotationsScanner());
            Set<Class<?>> annotatedClass = routerAnnotations.getTypesAnnotatedWith(RouteRegistration.class);
            Set<Method> annotatedMethod = routerAnnotations.getMethodsAnnotatedWith(RouteRegistration.class);

            annotatedClass.forEach(c -> {
                RouteRegistration annotation = c.getAnnotation(RouteRegistration.class);
                try {
                    Object routingInstance = c.newInstance();
                    Handler handler = (Handler<RoutingContext>) routingInstance;
                    for (HttpMethod method : annotation.method()){
                        if(annotation.auth()){
                            router.route(method, annotation.uri()).handler(JWTAuthHandler.create());
                        }
                        router.route(method, annotation.uri()).handler(handler);
                    }
                    routerList.add(new RouterContext(annotation, routingInstance));
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            annotatedMethod.forEach(m -> {
                RouteRegistration annotation = m.getAnnotation(RouteRegistration.class);
                try {
                    Object instance = searchCreatedInstance(m.getDeclaringClass());
                    if (instance == null)
                        instance = m.getDeclaringClass().newInstance();
                    Handler handler = createMethodHandler(instance, m);
                    for (HttpMethod method : annotation.method()){
                        if(annotation.auth()){
                            router.route(method, annotation.uri()).handler(JWTAuthHandler.create());
                        }
                        router.route(method, annotation.uri()).handler(handler);
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private Handler<RoutingContext> createMethodHandler(Object instance, Method m) {
        return ctx -> {
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
        };
    }

    private Object castingParameter(String str, Class<?> paramType) {
        Object paramData = str;
        if (paramType.equals(Integer.class)) {
            paramData = Integer.valueOf(str);
        } else if (paramType.equals(Boolean.class)) {
            paramData = Boolean.valueOf(str);
        } else if (paramType.equals(Double.class)) {
            paramData = Double.valueOf(str);
        } else if (paramType.equals(Float.class)) {
            paramData = Float.valueOf(str);
        } else if (paramType.equals(JsonObject.class)) {
            paramData = new JsonObject(str);
        } else if (paramType.equals(JsonArray.class)) {
            paramData = new JsonArray(str);
        }
        return paramData;
    }

    private Object getParameterFromBody(RoutingContext ctx, String paramName, Class<?> paramType) {
        Object paramData = null;
        String data = ctx.request().getFormAttribute(paramName);
        if(ctx.request().method() == HttpMethod.POST&& data!=null) {
            if (paramType.equals(String.class)) {
                paramData = data;
            } else if (paramType.equals(Integer.class)) {
                paramData = Integer.valueOf(data);
            } else if (paramType.equals(Boolean.class)) {
                paramData = Boolean.valueOf(data);
            } else if (paramType.equals(Double.class)) {
                paramData = Double.valueOf(data);
            } else if (paramType.equals(Float.class)) {
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

    private Object searchCreatedInstance(Class<?> clazz) {
        for (RouterContext ctx : this.routerList) {
            if (ctx.instanceOf(clazz))
                return ctx.getInstance();
        }
        return null;
    }

    public Router getRouter() {
        return router;
    }
}