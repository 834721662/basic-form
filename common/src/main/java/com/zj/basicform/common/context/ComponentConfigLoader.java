package com.zj.basicform.common.context;

import java.io.IOException;

/**
 * @author zj
 * @since 2019/2/8
 */
public interface ComponentConfigLoader {

    Context load(String name) throws IOException;

    Context loadClasspathContext(String name) throws IOException;

    Context load(Context context);
}
