package com.zj.basicform.common.property;

import java.util.Properties;

/**
 * @author zj
 * @since 2019/2/8
 */
public class DefaultFilePropertiesLoader extends FilePropertiesLoader {

    public DefaultFilePropertiesLoader(String filePath) {
        super(filePath);
    }

    @Override
    Properties load(Properties properties) {
        return properties;
    }
}
