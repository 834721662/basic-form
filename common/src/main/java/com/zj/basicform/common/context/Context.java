package com.zj.basicform.common.context;

import org.apache.commons.lang.StringUtils;
import java.io.Serializable;
import java.util.*;

/**
 * @author zj
 * @since 2019/2/8
 */
public class Context implements Serializable {

    private Map<String, String> parameters;

    public Context() {
        parameters = Collections.synchronizedMap(new HashMap<String, String>());
    }

    public void put(String key, String value) {
        parameters.put(key, value);
    }

    public void put(Map<String, String> map) {
        parameters.putAll(map);
    }


    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String getParameter(String key, String defaultValue) {
        String value = parameters.get(key);
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }

    public boolean getBoolean(String key) {
        String value = getParameter(key);
        return StringUtils.isBlank(value) ? false : Boolean.valueOf(value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getParameter(key);
        return StringUtils.isBlank(value) ? defaultValue : Boolean.valueOf(value);
    }

    public int getInt(String key, int defaultValue) {
        String value = getParameter(key);
        return StringUtils.isBlank(value) ? defaultValue : Integer.parseInt(value);
    }

    public int getInt(String key) {
        return Integer.parseInt(getParameter(key));
    }

    public long getLong(String key) {
        return Long.parseLong(getParameter(key));
    }

    public long getLong(String key, long defaultValue) {
        String value = getParameter(key);
        return StringUtils.isBlank(value) ? defaultValue : Long.parseLong(value);
    }

    public List<String> getList(String key, String delimiter) {
        String value = getParameter(key);
        if (StringUtils.isNotBlank(value)) {
            String[] split = value.split(delimiter);
            return Arrays.asList(split);
        }
        return new ArrayList<String>();
    }

    public boolean containKey(String key) {
        return parameters.containsKey(key);
    }

    public Map<String, String> toMap() {
        return parameters;
    }

    public void clear() {
        this.parameters.clear();
    }
    @Override
    public String toString(){
        return parameters.toString();
    }

}
