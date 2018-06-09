package com.boxfox.cross.common.vertx.service;

import com.boxfox.cross.common.data.DataSource;
import io.vertx.core.*;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public abstract class AbstractService {

    private Vertx vertx;

    public AbstractService(){}
    public AbstractService(Vertx vertx){this.vertx = vertx;}

    public void init(){}

    protected void useContext(ContextJob job){
        DSLContext ctx = DSL.using(DataSource.getDataSource(), SQLDialect.POSTGRES);
        job.execute(ctx);
        ctx.close();
    }

    public interface ContextJob{
        void execute(DSLContext ctx);
    }

    protected WorkerExecutor getExecutor(){
        return vertx.createSharedWorkerExecutor("service-worker-pool");
    }

    protected <T> void doAsync(Handler<Future<T>> hander, Handler<AsyncResult<T>> resultHandler){
        getExecutor().executeBlocking(hander, resultHandler);
    }

    protected Vertx getVertx(){
        return this.vertx;
    }
}
