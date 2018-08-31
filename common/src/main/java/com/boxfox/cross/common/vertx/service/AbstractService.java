package com.boxfox.cross.common.vertx.service;

import com.boxfox.cross.common.data.DataSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public abstract class AbstractService {

    private Vertx vertx;

    public AbstractService() {
    }

    public AbstractService(Vertx vertx) {
        this();
        this.vertx = vertx;
    }

    public void init(){
        if(AsyncService.getInstance() == null){
            AsyncService.create(this.vertx);
        }
    }

    protected void useContext(ContextJob job){
        DSLContext ctx = DSL.using(DataSource.getDataSource(), SQLDialect.POSTGRES);
        job.execute(ctx);
        ctx.close();
    }

    public interface ContextJob{
        void execute(DSLContext ctx);
    }

    protected Vertx getVertx(){
        return this.vertx;
    }

    protected <T> void doAsync( Handler<Future<T>> handler, Handler<AsyncResult<T>> resultHandler){
        AsyncService.getInstance().doAsync("service-worker-executor", handler, resultHandler);
    }
}
