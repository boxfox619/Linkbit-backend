package com.boxfox.cross.common.vertx.router;

import java.util.*;

import com.boxfox.cross.common.vertx.middleware.AuthHandler;
import com.boxfox.cross.common.vertx.middleware.BaseHandler;
import com.boxfox.cross.common.vertx.service.ServiceInjector;
import io.vertx.core.Vertx;
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
    private ServiceInjector serviceInjector;
    private List<RouterContext> routerList;
    private Router router;
    private Vertx vertx;

    public static RouteRegister routing(Vertx vertx) {
        RouteRegister register = new RouteRegister(vertx, Router.router(vertx));
        return register;
    }

    private RouteRegister(Vertx vertx, Router router) {
        this.vertx = vertx;
        this.router = router;
        this.routerList = new ArrayList();
        this.serviceInjector = new ServiceInjector(vertx);
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
                Handler handler = BaseHandler.create(instance, m);
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

    private Object createRouterInstance(Class<?> clazz){
        Object instance = searchCreatedRouter(clazz);
        if(instance==null) {
            try {
                instance = clazz.newInstance();
                this.serviceInjector.injectService(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private Object searchCreatedRouter(Class<?> clazz) {
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