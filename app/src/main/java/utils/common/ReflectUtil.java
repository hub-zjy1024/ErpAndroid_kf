package utils.common;

import java.lang.reflect.Method;

/**
 * Created by 张建宇 on 2019/7/24.
 */
public class ReflectUtil {

    public static Object reflectInvoke(Object obj, String method, Class[] types, Object[] params) throws Exception {
        try {
            Class  aClass = obj.getClass();
            //            ()Lsun/util/locale/provider/LocaleProviderAdapter
            Method getOpticalInsets = aClass.getDeclaredMethod(method, types);
            getOpticalInsets.setAccessible(true);
            Object mobj = getOpticalInsets.invoke(obj, params);
            return mobj;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("反射异常," + e);
        }
    }
}
