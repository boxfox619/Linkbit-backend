package com.boxfox.cross.service;

import com.boxfox.cross.common.data.DataSource;
import com.boxfox.vertx.service.AbstractService;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;


public abstract class JooqReactiveService extends AbstractService {

    protected <T> Single<T> createSingle(SingleJob job) {
        return Single.create(subscriber -> {
            DSLContext ctx = DSL.using(DataSource.getDataSource(), SQLDialect.POSTGRES);
            try {
                T result = (T) job.job(ctx);
                subscriber.onSuccess(result);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                subscriber.onError(throwable);
            } finally {
                ctx.close();
            }
        });
    }

    protected Completable createCompletable(CompletableJob job) {
        return Completable.create(subscriber -> {
            DSLContext ctx = DSL.using(DataSource.getDataSource(), SQLDialect.POSTGRES);
            try {
                job.job(ctx);
                subscriber.onComplete();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                subscriber.onError(throwable);
            } finally {
                ctx.close();
            }
        });
    }

    protected interface SingleJob {
       Object job(DSLContext ctx) throws Throwable;
    }

    protected interface CompletableJob {
        void job(DSLContext ctx) throws Throwable;
    }
}
