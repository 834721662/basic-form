package com.zj.basicform.common.context;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * 从文件配置当中读取
 * @author zj
 * @since 2019/2/8
 */
public class PropertiesConfigDirLoader implements ComponentConfigLoader {

    private PropertiesConfigLoader loader = new PropertiesConfigLoader();

    @Override
    public Context load(String dir) throws IOException {
        File file = new File(dir);
        Context context = new Context();
        if (file.isDirectory()) {
            String[] files = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".properties");
                }
            });
            if (files != null) {
                for (String f : files) {
                    context.put(loader.load(dir + "/" + f).toMap());
                }
            }
        }
        return context;
    }

    @Override
    public Context loadClasspathContext(String name) throws IOException {
        throw new RuntimeException("not support now.");
    }

    @Override
    public Context load(Context context) {
        throw new RuntimeException("not support now.");
    }
}
