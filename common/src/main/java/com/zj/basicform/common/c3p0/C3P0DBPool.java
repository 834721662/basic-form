package com.zj.basicform.common.c3p0;

import com.mchange.v2.c3p0.DataSources;
import com.zj.basicform.common.property.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author zj
 * @since 2019/2/9
 */
public class C3P0DBPool  implements DBPool, Serializable {
    private static Logger logger = LoggerFactory.getLogger(C3P0DBPool.class);
    private DataSource dataSource;
    public C3P0DBPool(String jdbcUrl, PropertiesLoader propertiesLoader) {
        try {
            DataSource unpooledDS = DataSources.unpooledDataSource(jdbcUrl);
            Properties properties = propertiesLoader.load();
            this.dataSource = DataSources.pooledDataSource(unpooledDS, properties);
        } catch (IOException e) {
            throw new RuntimeException("failed load c3p0 config.", e);
        } catch (SQLException e) {
            throw new RuntimeException("init c3p0 pool failed.", e);
        }
    }

    public C3P0DBPool(String jdbcUrl, Properties properties) {
        try {
            DataSource unpooledDS = DataSources.unpooledDataSource(jdbcUrl);
            this.dataSource = DataSources.pooledDataSource(unpooledDS, properties);
        } catch (SQLException e) {
            throw new RuntimeException("init c3p0 pool failed.", e);
        }
    }

    public C3P0DBPool(String jdbcUrl, String user, String password, PropertiesLoader propertiesLoader) {
        try {
            DataSource unpooledDS = DataSources.unpooledDataSource(jdbcUrl, user, password);
            Properties properties = propertiesLoader.load();
            this.dataSource = DataSources.pooledDataSource(unpooledDS, properties);
        } catch (IOException e) {
            throw new RuntimeException("failed load c3p0 config.", e);
        } catch (SQLException e) {
            throw new RuntimeException("init c3p0 pool failed.", e);
        }
    }

    public C3P0DBPool(String jdbcUrl, String user, String password, Properties properties) {
        try {
            DataSource unpooledDS = DataSources.unpooledDataSource(jdbcUrl, user, password);
            this.dataSource = DataSources.pooledDataSource(unpooledDS, properties);
        } catch (SQLException e) {
            throw new RuntimeException("init c3p0 pool failed.", e);
        }
    }

    @Override
    public Connection getResource() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void returnResource(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            DataSources.destroy(dataSource);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
