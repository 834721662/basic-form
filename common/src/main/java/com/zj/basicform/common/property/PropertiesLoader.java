package com.zj.basicform.common.property;

import java.io.IOException;
import java.util.Properties;

/**
 * @author zj
 * @since 2019/2/6
 */
public interface PropertiesLoader {

    Properties load() throws IOException;

}
