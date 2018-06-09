package com.boxfox.cross.common.vertx.router;

import com.boxfox.cross.common.data.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public abstract class RouterService {

    public void init(){}

    protected void useContext(ContextJob job){
        DSLContext ctx = DSL.using(DataSource.getDataSource(), SQLDialect.POSTGRES);
        job.execute(ctx);
        ctx.close();
    }

    public interface ContextJob{
        void execute(DSLContext ctx);
    }
}
