package com.b1b.js.erpandroid_kf.utils;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author js
 */
public class MyConnection {

    /**
     * @param namespace  命名空间
     * @param methodName 方法名
     * @param map        封装有参数的Map集合
     * @param serviceUrl 提供服务的URL
     */
    public static void getConn(String namespace, String methodName, Map<String, Object> map, String serviceUrl) {
        SoapObject request = new SoapObject(namespace, methodName);
        Set<String> keys = map.keySet();
        Iterator<String> its = keys.iterator();
        while (its.hasNext()) {
            String key = its.next();
            request.addProperty(key, map.get(key));
        }

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;

    }
}
