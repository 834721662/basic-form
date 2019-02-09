package com.zj.basicform.common.property;

import java.util.Map;
import java.util.Properties;

/**
 * @author zj
 * @since 2019/2/8
 */
public class KeyPrefixClasspathFilePropertiesLoader extends ClasspathFilePropertiesLoader {
    private String keyPrefix;

    public KeyPrefixClasspathFilePropertiesLoader(String keyPrefix, String path) {
        super();
        this.path = path;
        this.keyPrefix = keyPrefix;
    }

    @Override
    Properties load(Properties properties) {
        Properties p = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith(keyPrefix)) {
                key = key.substring(keyPrefix.length());
                p.put(key, entry.getValue());
            }
        }
        return p;
    }

}
