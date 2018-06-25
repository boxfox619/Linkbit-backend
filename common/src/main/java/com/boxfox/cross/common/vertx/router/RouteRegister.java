package com.boxfox.cross.common.vertx.router;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import com.boxfox.cross.common.vertx.middleware.AuthHandler;
import com.boxfox.cross.common.vertx.service.AbstractService;
import com.boxfox.cross.common.vertx.service.Service;
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
    private Map<Class, AbstractService> serviceMap;
    private Router router;
    private Vertx vertx;

    public static RouteRegister routing(Vertx vertx) {
        RouteRegister register = new RouteRegister(Router.router(vertx));
        register.vertx = vertx;
        return register;
    }

    private RouteRegister(Router router) {
        this.router = router;
        this.routerList = new ArrayList();
        this.serviceMap = new HashMap<>();
    }

    public void route(Handler<RoutingContext> handler) {
        router.route().handler(handler);
    }

    public void route(String... packages) {
        Arrays.stream(packages).forEach(packageName -> {
            Reflections scanner = new Reflections(packageName, new TypeAnnotationsScanner(), new SubTypesScanner(), new MethodAnnotationsScanner());
            AuthHandler jwtAuthHandler = AuthHandler.create();
            scanner.getTypesAnnotatedWith(RouteRegistration.class).forEach(c -> {
                RouteRegistration annotation = c.getAnnotation(RouteRegistration.class);
                try {
                    Object routingInstance = c.newInstance();
                    Handler handler = (Handler<RoutingContext>) routingInstance;
                    for (HttpMethod method : annotation.method()){
                        if(annotation.auth()){
                            router.route(method, annotation.uri()).handler(jwtAuthHandler);
                        }
                        router.route(method, annotation.uri()).handler(handler);
                    }
                    routerList.add(new RouterContext(annotation, routingInstance));
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            scanner.getMethodsAnnotatedWith(RouteRegistration.class).forEach(m -> {
                RouteRegistration annotation = m.getAnnotation(RouteRegistration.class);
                Object instance = createRouterInstance(m.getDeclaringClass());
                Handler handler = createMethodHandler(instance, m);
                for (HttpMethod method : annotation.method()) {
                    if (annotation.auth()) {
                        router.route(method, annotation.uri()).handler(jwtAuthHandler);
                    }
                    router.route(method, annotation.uri()).handler(handler);
                }
                routerList.add(new RouterContext(annotation, instance));
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

    private Object createRouterInstance(Class<?> clazz){
        Object instance = searchCreatedInstance(clazz);
        if(instance==null) {
            try {
                instance = clazz.newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.getAnnotation(Service.class) != null) {
                        AbstractService service = serviceMap.get(field.getType());
                        if(service==null) {
                            service = (AbstractService) field.getType().newInstance();
                            try {
                                Field vertxField = service.getClass().getSuperclass().getDeclaredField("vertx");
                                if(vertxField!=null){
                                    vertxField.setAccessible(true);
                                    vertxField.set(service, this.vertx);
                                }
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            }
                            try {
                                service.getClass().getMethod("init").invoke(service);
                            } catch (NoSuchMethodException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            serviceMap.put(field.getClass(), service);
                        }
                        field.set(instance, service);
                    }
                }
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return instance;
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