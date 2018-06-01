package com.boxfox.cross.common.data;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import java.sql.SQLException;

public class PostgresConfig {

    public static Configuration create(){
        Configuration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.POSTGRES);
        configuration.set(DataSource.getDataSource());
        return configuration;
    }

    public static DSLContext createContext(){
        DSLContext create = DSL.using(DataSource.getDataSource(), SQLDialect.POSTGRES);
        return create;
    }
}
