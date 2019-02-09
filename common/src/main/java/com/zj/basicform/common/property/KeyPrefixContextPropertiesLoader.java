package com.zj.basicform.common.property;

import com.zj.basicform.common.context.Context;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author zj
 * @since 2019/2/8
 */
public class KeyPrefixContextPropertiesLoader implements PropertiesLoader {
    private String keyPrefix;
    private Context context;

    public KeyPrefixContextPropertiesLoader(String keyPrefix, Context context) {
        this.keyPrefix = keyPrefix;
        this.context = context;
    }

    @Override
    public Properties load() throws IOException {
        Properties p = new Properties();
        for (Map.Entry<String, String> entry : context.toMap().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(keyPrefix)) {
                key = key.substring(keyPrefix.length());
                p.put(key, entry.getValue());
            }
        }
        return p;
    }
}
