package com.boxfox.linkbit.util;

import com.boxfox.linkbit.common.data.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class JooqUtil {

    public static void useContext(ContextJob job){
        DSLContext ctx = DSL.using(DataSource.getDataSource(), SQLDialect.POSTGRES);
        job.execute(ctx);
        ctx.close();
    }

    public interface ContextJob {
        void execute(DSLContext ctx);
    }

}
