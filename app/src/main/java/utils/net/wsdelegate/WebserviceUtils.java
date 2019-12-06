package utils.net.wsdelegate;

import android.util.Log;

import com.b1b.js.erpandroid_kf.BuildConfig;
import com.b1b.js.erpandroid_kf.MyApp;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 Created by 张建宇 on 2016/12/20. */

public class WebserviceUtils {
    private static final String NAMESPACE = "http://tempuri.org/";
    public static String ROOT_URL = "http://172.16.6.160:8006/";
    public static String LOCAL_URL = "http://192.168.10.117:8089/";
    public static final String COMMON_URL = "http://210.51.190.36:7500/";
    //服务名，带后缀名的
    public static final String MartService = "MartService.svc";
    public static final String MartStock = "MartStock.svc";
    public static final String Login = "Login.svc";
    public static final String MyBasicServer = "MyBasicServer.svc";
    public static final String ForeignStockServer = "ForeignStockServer.svc";
    public static final String PMServer = "PMServer.svc";
    public static final String IC360Server = "IC360Server.svc";
    public static final String ChuKuServer = "ChuKuServer.svc";
    public static final String SF_SERVER = "SF_Server.svc";
    public static final String RKServer = "RKServer.svc";
    public static final String SF_Server = SF_SERVER;
    private static final int VERSION_10 = SoapEnvelope.VER10;
    private static final int VERSION_11 = SoapEnvelope.VER11;
    private static final int VERSION_12 = SoapEnvelope.VER12;
    public static final int DEF_TIMEOUT = 30 * 1000;

    public static final boolean debug = false;
    /**
     扫描二维码的返回请求码
     */
    public static final int QR_REQUESTCODE = 100;
    /**
     设备No
     */
    public static String DeviceNo = "";
    /**
     交互码
     */
    public static String WebServiceCheckWord = "sdr454fgtre6e655t5rt4";
    /**
     设备ID
     */
    public static String DeviceID = "ZTE-T U880";

    public static class SoapException extends IOException {
        public SoapException() {
        }

        public SoapException(String detailMessage) {
            super(detailMessage);
        }

        public SoapException(String message, Throwable cause) {
            super(message, cause);
        }

        public SoapException(Throwable cause) {
            super(cause);
        }
    }

    /**
     获取Url
     不能随意拼接，得自己根据wsdl文档
     @param serviceName 以svc结尾的service名称
     @return
     */
    private static String getTransportSEtUrl(String serviceName) {
        //        return ROOT_URL + serviceName + "?singleWsdl";
        return ROOT_URL + serviceName;
    }

    /**
     不能随意拼接，得自己根据wsdl文档
     @param serviceName
     @param methodName
     @return
     */
    private static String getSoapAcction(String serviceName, String methodName) {
        return NAMESPACE + "I" + serviceName.substring(0, serviceName.indexOf(".")) + "/" + methodName;
    }

    /**
     * 获取SoapObject请求对象
     * @param properties 方法的参数，有序，建议集合使用LinkedHashMap，如果没有，可以传入null
     * @param method     方法的名称
     * @return
     */
    private static SoapObject getRequest(LinkedHashMap<String, Object> properties, String method) {
        SoapObject request = new SoapObject(WebserviceUtils.NAMESPACE, method);
        if (properties != null) {
            // 设定参数
            Set<String> set = properties.keySet();
            for (String string : set) {
                request.addProperty(string, properties.get(string));
            }
        }
        return request;
    }



    public static String getWcfResult(LinkedHashMap<String, Object> properties, String method,
                                      String serviceName) throws IOException,
            XmlPullParserException {
        SoapObject request = getRequest(properties, method);
        return getWcfResult(request, VERSION_11, serviceName);
    }

    private static String  getCommWsResult (String namespace, String method, String soapAction, String transUrl,
                                                         LinkedHashMap<String, Object> properties, int envolopeVersion, int
                                                                 timeout) throws IOException, XmlPullParserException {
        SoapObject request = new SoapObject(namespace, method);
        //设置方法参数，无参数直接传入null值
        if (properties != null) {
            Iterator<String> iterator = properties.keySet().iterator();
            while (iterator.hasNext()) {
                String s = iterator.next();
                String value = (String) properties.get(s);
                request.addProperty(s, value);
            }
        }

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(envolopeVersion);
        envelope.setOutputSoapObject(request);
        envelope.dotNet = true;
        HttpTransportSE se = new HttpTransportSE(transUrl, timeout);
        if (envolopeVersion == VERSION_11 && soapAction != null) {
            se.call(soapAction, envelope);
        } else if (envolopeVersion == VERSION_12) {
            se.call(null, envelope);
        } else {
            throw new IOException("请选择正确的envolopeVersion,11或者12");
        }
        Object obj = envelope.getResponse();
        if (obj instanceof SoapFault) {
            throw new IOException("response error", (SoapFault) obj);
        } else if (obj instanceof SoapObject) {

        }
        return obj.toString();
    }

    /**
     * @param request
     * @param envolopeVesion {@link org.ksoap2.SoapEnvelope}
     * @param serviceName    以svc结尾的service名称
     * @return 返回请求结果
     */
    private static String getWcfResult(SoapObject request, int envolopeVesion, String
            serviceName) throws
            IOException, XmlPullParserException {
        int timeout = DEF_TIMEOUT;
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(envolopeVesion);
        //.net开发的ws服务必须设置为true
        envelope.dotNet = true;
        //       envelope.bodyOut = request;
        envelope.setOutputSoapObject(request);
        //创建HttpTransportSE对象
        HttpTransportSE ht = new HttpTransportSE(getTransportSEtUrl(serviceName), timeout);
        //有些不需要传入soapAction，根据wsdl文档
        return getResNew(ht, envolopeVesion, envelope, serviceName, request);
    }

    public static String getLocalWcf(LinkedHashMap<String,Object> properties,String method, String
            serviceName) throws
            IOException, XmlPullParserException {
//        LinkedHashMap<String, Object> properties, String method,
//                String serviceName
        int envolopeVesion = VERSION_11;
        SoapObject request = getRequest(properties, method);
        int timeout = DEF_TIMEOUT;
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(envolopeVesion);
        //.net开发的ws服务必须设置为true
        envelope.dotNet = true;
        //       envelope.bodyOut = request;
        envelope.setOutputSoapObject(request);
        //创建HttpTransportSE对象"
        String url = LOCAL_URL + serviceName;
        HttpTransportSE ht = new HttpTransportSE(url, timeout);
        //有些不需要传入soapAction，根据wsdl文档
        return getResNew(ht, envolopeVesion, envelope, serviceName, request);
    }

    private static String getResNew(HttpTransportSE ht, int envolopeVesion, SoapSerializationEnvelope
            envelope, String serviceName, SoapObject request)
            throws IOException {
        String ret = "";
        try {
            if (envolopeVesion == VERSION_12) {
                ht.call(null, envelope);
            } else {
                ht.call(getSoapAcction(serviceName, request.getName()), envelope);
            }
            Object sob = envelope.getResponse();
            String debugMsg = String.format("wcf,server=%s,param=%s", serviceName, request.toString());
            if (sob == null) {
                MyApp.myLogger.writeBug("Soap response Object null," + debugMsg);
                throw new IOException("返回空");
            }
            int debugLenLimit = 1000;
            if (sob instanceof SoapObject) {
                MyApp.myLogger.writeBug("Soap response is SoapObject");
                if (debug) {
                    int len = sob.toString().length();
                    String outStr = sob.toString();
                    if (len > debugLenLimit) {
                        outStr = outStr.substring(0, debugLenLimit) + "(...)";
                    }
                    System.out.println(debugMsg);
                }
            } else if (sob instanceof SoapPrimitive) {
                if (debug) {
                    int len = sob.toString().length();
                    String outStr = sob.toString();
                    if (len > debugLenLimit) {
                        outStr = outStr.substring(0, debugLenLimit) + "(...)";
                    }
                    System.out.println(debugMsg);
                }
            } else {
                MyApp.myLogger.writeBug("Soap response is Unknow," + debugMsg + ",res=" + sob.toString());
                throw new IOException("接口调用失败，Soap response Unknow");
            }
            ret = sob.toString();
            if (BuildConfig.DEBUG) {
                Log.e("zjy", "wcf,cla=" + sob.getClass() + "->getResNew():"
                        +
                        "->" + serviceName +
                        "->" + request.getName() +
                        " ,ret==" + ret);
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            throw new IOException("接口解析失败");
        } catch (SocketException | SocketTimeoutException e) {
            e.printStackTrace();
            throw new IOException("连接服务器失败");
        } catch (SoapFault e) {
            e.printStackTrace();
            throw new IOException("接口调用失败，soapfault=," + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("接口调用失败，其他异常," + e.getMessage());
        }
        return ret;
    }

}
