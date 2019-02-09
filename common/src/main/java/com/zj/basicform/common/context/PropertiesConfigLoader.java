package com.zj.basicform.common.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author zj
 * @since 2019/2/8
 */
public class PropertiesConfigLoader implements ComponentConfigLoader {

    private static Logger logger = LoggerFactory.getLogger(PropertiesConfigLoader.class);

    @Override
    public Context load(String name) throws IOException {
        BufferedReader br = null;
        Context context = new Context();
        try {
            br = new BufferedReader(new FileReader(name));
            Properties p = new Properties();
            p.load(br);
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                context.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error("file " + name + " load failed.", e);
                }
            }
        }
        return context;
    }

    @Override
    public Context loadClasspathContext(String name) throws IOException {
        InputStream inputStream = null;
        Properties p = new Properties();
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream(name);
            p.load(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        Context context = new Context();
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            context.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return context;
    }

    @Override
    public Context load(Context context) {
        throw new RuntimeException("not support now.");
    }
}
