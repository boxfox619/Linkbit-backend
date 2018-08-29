package com.boxfox.core.router;


import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.boxfox.cross.common.vertx.service.ServiceInjector;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.Test;
import org.mockito.stubbing.Answer;

public class AuthRouterTest {

  @Test
  public void singinTest(){
    String token = "";
    AuthRouter authRouter = new AuthRouter();
    Vertx vertx = mock(Vertx.class);
    ServiceInjector serviceInjector = new ServiceInjector(vertx);
    try {
      serviceInjector.injectService(authRouter);
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    RoutingContext ctx = mock(RoutingContext.class);
    HttpServerResponse response = mock(HttpServerResponse.class);
    when(response.setStatusCode(anyInt())).then((Answer) invocation -> {
      System.out.println(invocation.getArguments());
      return null;
    });
    when(ctx.response()).thenReturn(response);
    authRouter.signin(ctx, token);
  }
}
