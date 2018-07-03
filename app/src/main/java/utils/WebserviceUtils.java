package utils;

import android.util.Log;

import com.b1b.js.erpandroid_kf.MyApp;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 Created by 张建宇 on 2016/12/20. */

public class WebserviceUtils {
    public static final String NAMESPACE = "http://tempuri.org/";
    public static final String ROOT_URL = "http://172.16.6.160:8006/";
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
    public static final String SF_Server = SF_SERVER;
    private static final int VERSION_10 = SoapEnvelope.VER10;
    private static final int VERSION_11 = SoapEnvelope.VER11;
    private static final int VERSION_12 = SoapEnvelope.VER12;
    public static final int DEF_TIMEOUT = 30 * 1000;
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
     获取SoapObject请求对象
     @param properties 方法的参数，有序，建议集合使用LinkedHashMap，如果没有，可以传入null
     @param method     方法的名称
     @return
     */
    public static SoapObject getRequest(LinkedHashMap<String, Object> properties, String method) {
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

    /**
     @param request
     @param envolopeVesion {@link org.ksoap2.SoapEnvelope}
     @param soapAction
     @param resultUrl      HttpTransportSE中的url
     @return
     @throws IOException
     @throws XmlPullParserException
     */
    private static SoapSerializationEnvelope getEnvelope(SoapObject request, int envolopeVesion, String soapAction, String
            resultUrl,int timeout) throws IOException, XmlPullParserException {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(envolopeVesion);
        envelope.dotNet = true;
        //       envelope.bodyOut = request;
        envelope.setOutputSoapObject(request);
        //创建HttpTransportSE对象
        HttpTransportSE ht = new HttpTransportSE(resultUrl, timeout);
        //有些不需要传入soapAction，根据wsdl文档
        ht.call(soapAction, envelope);
        return envelope;
    }

    /**
     @param request        返回Envelope对象
     @param envolopeVesion {@link org.ksoap2.SoapEnvelope}
     @param serviceName
     @return
     @throws IOException
     @throws XmlPullParserException
     */
    private static SoapSerializationEnvelope getEnvelope(SoapObject request, int envolopeVesion, String serviceName, int
            timeout) throws IOException, XmlPullParserException {
        String url = getTransportSEtUrl(serviceName);
        String action = getSoapAcction(serviceName, request.getName());
        return getEnvelope(request, envolopeVesion, action, url, timeout);
    }


    /**
     @param request
     @param envolopeVesion {@link org.ksoap2.SoapEnvelope}
     @param serviceName    以svc结尾的service名称
     @return
     @throws IOException
     @throws XmlPullParserException
     */
    private static SoapPrimitive getSoapPrimitiveResponse(SoapObject request, int envolopeVesion, String serviceName) throws
            IOException, XmlPullParserException {
        SoapSerializationEnvelope envelope = getEnvelope(request, envolopeVesion, serviceName, 30 * 1000);
        Object response = envelope.getResponse();
        SoapPrimitive sob = null;
        if (response == null) {
            MyApp.myLogger.writeError("==========response==null:" + request.toString());
            return new SoapPrimitive("", "", "");
        } else {
            if (response instanceof SoapFault) {
                Exception soapFault = (SoapFault) response;
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(bao);
                soapFault.printStackTrace(writer);
                writer.flush();
                String error = "";
                try {
                    error = new String(bao.toByteArray(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                writer.close();
                MyApp.myLogger.writeError("==========Call ERROR: req:" + request.toString());
                MyApp.myLogger.writeError("==========Call ERROR:detail:" + error);
                throw new SoapException(soapFault);
            } else if (response instanceof SoapObject) {
                SoapObject obj=(SoapObject) response;
                Log.e("zjy", "WebserviceUtils->getSoapPrimitiveResponse(): Obj==" + obj);
                MyApp.myLogger.writeError("==========Call ERROR:detail:" + request.toString());
                if (("anyType{}").equals(obj.toString())) {
                    return new SoapPrimitive("", "", "");
                }
                int propertyCount = obj.getPropertyCount();
                for(int i=0;i<propertyCount;i++) {
                    Object property = obj.getProperty(i);
                    Log.e("zjy", "WebserviceUtils->getSoapPrimitiveResponse(): toString==" + property.toString());
                }
                return new SoapPrimitive("", "", "");
            }
        }
        sob= (SoapPrimitive) response;
        return sob;
    }

    /**
     @param request
     @param serviceName 以svc结尾的service名称
     @return
     @throws IOException
     @throws XmlPullParserException
     */
    public static SoapPrimitive getSoapPrimitiveResponse(SoapObject request, String serviceName) throws
            IOException, XmlPullParserException {
        return getSoapPrimitiveResponse(request, VERSION_11, serviceName);
    }
    public static String getWcfResult(LinkedHashMap<String, Object> properties, String method,
                                      String serviceName) throws IOException,
            XmlPullParserException {
//        return getSoapPrimitiveResponse(properties, method, serviceName).toString();
        return getWcfResult2(properties, method, serviceName);
    }
    /**
     @param serviceName 以svc结尾的service名称
     @return
     @throws IOException
     @throws XmlPullParserException
     */
    public static SoapPrimitive getSoapPrimitiveResponse(LinkedHashMap<String, Object> properties, String methodName, String
            serviceName) throws
            IOException, XmlPullParserException {
        SoapObject request = getRequest(properties, methodName);
        return getSoapPrimitiveResponse(request, serviceName);
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
        int timeout = 30 * 1000;
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(envolopeVesion);
        //.net开发的ws服务必须设置为true
        envelope.dotNet = true;
        //       envelope.bodyOut = request;
        envelope.setOutputSoapObject(request);
        //创建HttpTransportSE对象
        HttpTransportSE ht = new HttpTransportSE(getTransportSEtUrl(serviceName), timeout);
        //有些不需要传入soapAction，根据wsdl文档
        if (envolopeVesion == VERSION_12) {
            ht.call(null, envelope);
        }else{
            ht.call(getSoapAcction(serviceName, request.getName()), envelope);
        }
        Object sob = envelope.getResponse();
        if (sob == null) {
            Log.e("zjy", "WebserviceUtils->getWcfResult(): soapObj==null");
            MyApp.myLogger.writeBug("Soap response Object null" + request.toString());
            return "response obj null";
        }
        if (sob instanceof SoapFault) {
            MyApp.myLogger.writeBug("Soap response Object null" + request.toString());
            throw new IOException("error requeset", (SoapFault) sob);
        }else if (sob instanceof SoapObject) {
            Log.e("zjy", "WebserviceUtils->getWcfResult(): soapObj==");
            MyApp.myLogger.writeBug("Soap response is SoapObject");
        } else if (sob instanceof SoapPrimitive) {
        } else {
            MyApp.myLogger.writeBug("Soap response is Unknow");
        }
        return sob.toString();
    }

    public static String getWcfResult2(LinkedHashMap<String, Object> properties, String method,
                                      String serviceName) throws IOException,
            XmlPullParserException {
        SoapObject request = getRequest(properties, method);
        return getWcfResult(request, VERSION_11, serviceName);
    }
}
