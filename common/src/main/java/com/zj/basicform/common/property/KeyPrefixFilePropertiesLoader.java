package com.zj.basicform.common.property;

import java.util.Map;
import java.util.Properties;

/**
 * 前缀读取配置参数信息
 *
 * @author zj
 * @since 2019/2/8
 */
public class KeyPrefixFilePropertiesLoader extends FilePropertiesLoader {
    private String keyPrefix;

    /**
     * @param filePath 文件路径
     * @param keyPrefix 配置信息
     */
    public KeyPrefixFilePropertiesLoader(String filePath, String keyPrefix) {
        super(filePath);
        this.filePath = filePath;
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
