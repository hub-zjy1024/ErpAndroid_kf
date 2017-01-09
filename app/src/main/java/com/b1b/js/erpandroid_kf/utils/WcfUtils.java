package com.b1b.js.erpandroid_kf.utils;

import android.util.Log;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by 张建宇 on 2016/12/20.
 */

public class WcfUtils {


    public static final String NAMESPACE = "http://tempuri.org/";
    public static final String ROOT_URL = "http://172.16.6.160:8006/";
    //服务名，带后缀名的
    public static final String MartService = "MartService.svc";
    public static final String Login = "Login.svc";
    public static final String MyBasicServer = "MyBasicServer.svc";
    public static final String ForeignStockServer = "ForeignStockServer.svc";
    public static final String PMServer = "PMServer.svc";
    public static final String IC360Server = "IC360Server.svc";
    public static final String ChuKuServer = "ChuKuServer.svc";
    /**
     * 扫描二维码的返回请求码
     */
    public static final int QR_REQUESTCODE = 100;
    /**
     * 设备No
     */
    public static String DeviceNo = "";
    /**
     * 交互码
     */
    public static String WebServiceCheckWord = "sdr454fgtre6e655t5rt4";
    /**
     * 设备ID
     */
    public static String DeviceID = "ZTE-T U880";

    /**
     * 获取Url
     *
     * @param serviceName 以svc结尾的service名称
     * @return
     */
    private static String getTransportSEtUrl(String serviceName) {
        return ROOT_URL + serviceName + "?singleWsdl";
    }

    private static String getSoapAcction(String serviceName, String methodName) {
        Log.e("zjy", "WcfUtils.java->getSoapAcction(): ==" + NAMESPACE + "I" + serviceName.substring(0, serviceName.indexOf(".")) + "/" + methodName);
        return NAMESPACE + "I" + serviceName.substring(0, serviceName.indexOf(".")) + "/" + methodName;
    }

    /**
     * 获取SoapObject请求对象
     *
     * @param properties 方法的参数，如果没有，可以传入null
     * @param method     方法的名称
     * @return
     */
    public static SoapObject getRequest(LinkedHashMap<String, Object> properties, String method) {
        SoapObject request = new SoapObject(WcfUtils.NAMESPACE, method);
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
     * @param request
     * @param EnvolopeVesion {@link org.ksoap2.SoapEnvelope}
     * @param soapAction
     * @param resultUrl      HttpTransportSE中的url
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static Object getObjResponse(SoapObject request, int EnvolopeVesion, String soapAction, String resultUrl) throws IOException, XmlPullParserException {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(EnvolopeVesion);
        envelope.dotNet = true;
//       envelope.bodyOut = request;
        envelope.setOutputSoapObject(request);
        //创建HttpTransportSE对象
        HttpTransportSE ht = new HttpTransportSE(resultUrl);
        //有些不需要传入soapAction，根据wsdl文档
        ht.call(soapAction, envelope);
        Object sob = envelope.getResponse();
        return sob;
    }

    /**
     * @param request
     * @param EnvolopeVesion {@link org.ksoap2.SoapEnvelope}
     * @param serviceName    以svc结尾的service名称
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static Object getObjResponse(SoapObject request, int EnvolopeVesion, String serviceName) throws IOException, XmlPullParserException {
        //创建HttpTransportSE对象
        HttpTransportSE ht = new HttpTransportSE(getTransportSEtUrl(serviceName));
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(EnvolopeVesion);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        String soapAction = getSoapAcction(serviceName, request.getName());
        //有些不需要传入soapAction，根据wsdl文档
        ht.call(soapAction, envelope);
        Object sob = envelope.bodyIn;
//      Object sob = envelope.getResponse();
        return sob;
    }

    public static SoapObject getSoapObjResponse(SoapObject request, int EnvolopeVesion, String serviceName) throws IOException, XmlPullParserException {
        SoapObject sob = (SoapObject) getObjResponse(request, EnvolopeVesion, serviceName);
        return sob;
    }

    /**
     * @param request
     * @param EnvolopeVesion {@link org.ksoap2.SoapEnvelope}
     * @param serviceName    以svc结尾的service名称
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static SoapPrimitive getSoapPrimitiveResponse(SoapObject request, int EnvolopeVesion, String serviceName) throws IOException, XmlPullParserException {
        //创建HttpTransportSE对象
        HttpTransportSE ht = new HttpTransportSE(getTransportSEtUrl(serviceName));
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(EnvolopeVesion);
        envelope.dotNet = true;
        envelope.bodyOut = request;
//        envelope.setOutputSoapObject(request);
        String soapAction = getSoapAcction(serviceName, request.getName());
        //有些不需要传入soapAction，根据wsdl文档
        ht.call(soapAction, envelope);
//      Object sob = envelope.bodyIn;
        SoapPrimitive sob = (SoapPrimitive) envelope.getResponse();
        return sob;
    }

    public static void getUserInfo(String uid) {
        LinkedHashMap map = new LinkedHashMap<String, Object>();
        map.put("uid", uid);
        SoapObject request = WcfUtils.getRequest(map, "getUserInfo");
    }

    public static String getStringTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        return sdf.format(new Date());
    }

}
