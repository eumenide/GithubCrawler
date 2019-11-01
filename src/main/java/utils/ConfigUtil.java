package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author: eumes
 * @date: 2019/10/21
 **/
public class ConfigUtil {

    private static Map<String,String> configures=new HashMap<String,String>();
    static{
        loadProperties();
    }

    private ConfigUtil(){}

    /**
     * 加载配置文件资源
     */
    private static void loadProperties(){
        String configure="src/main/resources/config.properties";
        try {
            InputStream inputStream=Thread.currentThread().getContextClassLoader().getResourceAsStream(configure);
            Properties propertyFiles=new Properties();
            propertyFiles.load(inputStream);
            for(Object key:propertyFiles.keySet()){
                configures.put(key.toString(), propertyFiles.getProperty(key.toString()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 根据配置文件的key，返回对应的配置值
     * @param configKey
     * @return
     */
    public static String getConfig(String configKey){
        return configures.get(configKey);
    }
    /**
     * 描述：根据配置文件的key，返回对应的配置值，如果configKey不存在,或者值为空的字符串，则返回默认值defaultValue
     * @param configKey
     * @param defaultValue
     * @return
     */
    public static String getConfig(String configKey,String defaultValue){
        String v=configures.get(configKey);
        if(v==null || "".equals(v.trim())){
            return defaultValue;
        }
        return v;
    }
    /**
     * 描述：属性文件中是否包含键configKey，包含返回true
     * @param configKey
     * @return
     */
    public static boolean containsKey(String configKey){
        return configures.containsKey(configKey);
    }

    /**
     * 描述：根据配置文件的key，返回对应的配置值，如果configKey不存在,或者值为空的字符串，则返回默认值defaultValue
     * @param configKey
     * @param defaultValue
     * @param clazz  需要返回的数据类型：支持Integer.class，Double.class，Boolean.class，Float.class，Long.class，String.class
     * @return
     */
    public static <E> E getConfig(String configKey,E defaultValue,Class<E> clazz){
        E ret = defaultValue;
        String v=configures.get(configKey);
        if(v==null || "".equals(v.trim())){
            return ret;
        }
        if(clazz.equals(Integer.class)){
            try{ret =clazz.cast(Integer.valueOf(v));}catch(Exception e){}
        }else if(clazz.equals(Double.class)){
            try{ret =clazz.cast(Double.valueOf(v));}catch(Exception e){}
        }else if(clazz.equals(Boolean.class)){
            try{ret =clazz.cast("1".equals(v)?true:Boolean.valueOf(v));}catch(Exception e){}
        }else if(clazz.equals(Float.class)){
            try{ret =clazz.cast(Float.valueOf(v));}catch(Exception e){}
        }else if(clazz.equals(Long.class)){
            try{ret =clazz.cast(Long.valueOf(v));}catch(Exception e){}
        }else if(clazz.equals(String.class)){
            try{ret =clazz.cast(String.valueOf(v));}catch(Exception e){}
        }
        return ret;
    }
}
