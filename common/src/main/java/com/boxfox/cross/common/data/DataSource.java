package com.boxfox.cross.common.data;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
    private BasicDataSource ds;

    private DataSource(String host, int port, String dbName, String id, String pwd) {
        ds = new BasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUsername(id);
        ds.setPassword(pwd);
        ds.setUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName);
    }

    private static class DataSourceInstance {
        private static String host = Config.getDefaultInstance().getString("dbHost", "localhost");
        private static String user = Config.getDefaultInstance().getString("dbUser", "postgres");
        private static String password = Config.getDefaultInstance().getString("dbPassword", "dbpw1234");
        private static int port = Config.getDefaultInstance().getInt("dbPort", 5432);
        private static String dbName = Config.getDefaultInstance().getString("dbName", "crossdb");
        private static DataSource instance = new DataSource(host, port, dbName, user, password);
    }

    protected static Connection getConnection() throws SQLException {
        return DataSourceInstance.instance.ds.getConnection();
    }
}