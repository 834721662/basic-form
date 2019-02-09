package com.zj.basicform.common.c3p0;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author zj
 * @since 2019/2/9
 */
public interface DBPool {

    Connection getResource() throws SQLException;

    void returnResource(Connection connection);

    void close();

}
