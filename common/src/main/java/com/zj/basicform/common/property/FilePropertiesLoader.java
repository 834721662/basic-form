package com.zj.basicform.common.property;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * 从文件当中读取配置信息
 * @author zj
 * @since 2019/2/8
 */
public abstract class FilePropertiesLoader implements PropertiesLoader {
    String filePath;

    public FilePropertiesLoader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Properties load() throws IOException {
        Properties p = new Properties();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            p.load(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return load(p);
    }

    abstract Properties load(Properties properties);
}
