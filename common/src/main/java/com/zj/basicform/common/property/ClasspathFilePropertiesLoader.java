package com.zj.basicform.common.property;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author zj
 * @since 2019/2/8
 */
public abstract class ClasspathFilePropertiesLoader implements PropertiesLoader {
    protected String path;

    @Override
    public Properties load() throws IOException {
        Properties p = new Properties();
        try (InputStream inputStream = this.getClass().getResourceAsStream(path)) {
            p.load(inputStream);
        }
        return load(p);
    }

    abstract Properties load(Properties properties);
}
