package com.boxfox.linkbit.common.data;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

public class PostgresConfig {

    public static Configuration create(){
        Configuration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.POSTGRES);
        configuration.set(DataSource.getDataSource());
        return configuration;
    }

}
