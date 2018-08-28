package com.boxfox.cross.common.vertx.router;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class RouterMapDoc {

  public static JsonArray createAPIDoc(String packageName) {
    JsonArray apiArr = new JsonArray();
    Reflections routerAnnotations = new Reflections(packageName, new TypeAnnotationsScanner(), new SubTypesScanner(), new MethodAnnotationsScanner());
    Set<Class<?>> annotatedClass = routerAnnotations.getTypesAnnotatedWith(RouteRegistration.class);
    Set<Method> annotatedMethod = routerAnnotations
            .getMethodsAnnotatedWith(RouteRegistration.class);
    annotatedClass.forEach(c -> {
      RouteRegistration annotation = c.getAnnotation(RouteRegistration.class);
      JsonObject apiObj = new JsonObject();
      apiObj.put("url", annotation.uri());
      apiObj.put("method", annotation.method()[0]);
      apiArr.add(apiObj);
    });
    annotatedMethod.forEach(m -> {
      RouteRegistration annotation = m.getAnnotation(RouteRegistration.class);
      JsonObject apiObj = new JsonObject();
      apiObj.put("url", annotation.uri());
      apiObj.put("method", annotation.method()[0]);
      JsonArray params = new JsonArray();
      Arrays.stream(m.getParameters()).forEach(param -> {
        Class<?> paramClass = param.getType();
        if (!paramClass.equals(RoutingContext.class)) {
          String paramName = param.getName();
          params.add(paramName);
        }
      });
      apiObj.put("params", params);
      apiArr.add(apiObj);
    });
    return apiArr;
  }

}
