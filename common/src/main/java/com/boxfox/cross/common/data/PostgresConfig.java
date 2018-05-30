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
        try {
            configuration.set(DataSource.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return configuration;
    }

    public static DSLContext createContext(){
        DSLContext create = null;
        try {
            create = DSL.using(DataSource.getConnection(), SQLDialect.POSTGRES);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return create;
    }
}
