package com.boxfox.cross.service;

import com.boxfox.cross.common.RoutingException;
import com.boxfox.cross.common.data.DataSource;
import com.boxfox.vertx.service.AbstractService;
import com.boxfox.vertx.service.AsyncService;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;


public abstract class JooqReactiveService extends AbstractService {

    protected <T> Single<T> createSingle(SingleJob job) {
        return Single.create(subscriber -> {
            AsyncService.getInstance().doAsync("service-worker-executor", future -> {
                DSLContext ctx = DSL.using(DataSource.getDataSource(), SQLDialect.POSTGRES);
                try {
                    subscriber.onSuccess((T)job.job(ctx));
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } finally {
                    ctx.close();
                }
            });
        });
    }

    protected Completable createCompletable(CompletableJob job) {
        return Completable.create(subscriber -> {
            AsyncService.getInstance().doAsync("service-worker-executor", future -> {
                DSLContext ctx = DSL.using(DataSource.getDataSource(), SQLDialect.POSTGRES);
                try {
                    job.job(ctx);
                    subscriber.onComplete();
                } catch (RoutingException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } finally {
                    ctx.close();
                }
                future.complete();
            });
        });
    }

    protected interface SingleJob {
        Object job(DSLContext ctx) throws RoutingException;
    }

    protected interface CompletableJob {
        void job(DSLContext ctx) throws RoutingException;
    }
}
