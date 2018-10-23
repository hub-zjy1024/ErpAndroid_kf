package com.b1b.js.erpandroid_kf.yundan.sf.sfutils;

import android.util.Base64;
import android.util.Log;

import com.b1b.js.erpandroid_kf.yundan.sf.entity.Cargo;
import com.b1b.js.erpandroid_kf.yundan.sf.entity.SFSender;
import com.b1b.js.erpandroid_kf.yundan.utils.Md5;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Created by 张建宇 on 2016/12/20.
 */

public class SFWsUtils {

    public static String ORDER_SERVICE = "OrderService";
    public static String ORDER_CANCLE_SERVICE = "OrderConfirmService";
    public static final String NAMESPACE = "http://service.expressservice.integration" +
            ".sf.com/";
//    public static final String ROOT_URL = "http://bspoisp.sit.sf-express.com:11080/bsp-oisp/ws/sfexpressService?wsdl";
//    public static String head = "BSPdevelop";
//    public static String verifyCode = "j8DzkIFgmlomPt0aLuwU";
    //正式接口
        public static final String ROOT_URL = "http://bsp-oisp.sf-express.com/bsp-oisp/ws/sfexpressService?wsdl";
        public static String verifyCode = "FCQsryp3UXNgLfMEPEkcRDT3BRVgYGx5";
        public static String head = "bjyd";
    public static final int TIME_OUT = 15 * 1000;
    //SetClientSFInfo  name="id" type="xs:string" name="Province" type="xs:string"
    //   name="City" type="xs:string"name="County" type="xs:string"
    //    UpdateHeTongFileInfo(int pid, string filepath);
    //    string GetHeTongFileInfo(int pid);

//    public static String head = "bjyd";

    /**
     * 获取Url
     * 不能随意拼接，得自己根据wsdl文档
     *
     * @param serviceName 以svc结尾的service名称
     * @return
     */
    private static String getTransportSEtUrl(String serviceName) {
        //        return ROOT_URL + serviceName + "?singleWsdl";
        return ROOT_URL + serviceName;
    }

    /**
     * 不能随意拼接，得自己根据wsdl文档
     *
     * @param serviceName
     * @param methodName
     * @return
     */
    private static String getSoapAcction(String serviceName, String methodName) {
        Log.e("zjy", "SFWsUtils.java->getSoapAcction(): ==" + NAMESPACE + "I" +
                serviceName.substring(0, serviceName.indexOf(".")) + "/" + methodName);
        return NAMESPACE + "I" + serviceName.substring(0, serviceName.indexOf(".")) +
                "/" + methodName;
    }

    /**
     * 获取SoapObject请求对象
     *
     * @param properties 方法的参数，有序，建议集合使用LinkedHashMap，如果没有，可以传入null
     * @param method     方法的名称
     * @return
     */
    public static SoapObject getRequest(LinkedHashMap<String, Object> properties,
                                        String method) {
        SoapObject request = new SoapObject(SFWsUtils.NAMESPACE, method);
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
     * @param envolopeVesion {@link org.ksoap2.SoapEnvelope}
     * @param soapAction
     * @param resultUrl      HttpTransportSE中的url
     * @param isNetServer    后台是否通过.net写的
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static SoapSerializationEnvelope getEnvelope(SoapObject request, int
            envolopeVesion, String soapAction, String resultUrl, boolean isNetServer)
            throws IOException, XmlPullParserException {

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope
                (envolopeVesion);
        envelope.dotNet = isNetServer;
        envelope.setOutputSoapObject(request);
        //创建HttpTransportSE对象
        HttpTransportSE ht = new HttpTransportSE(resultUrl, TIME_OUT);
        //有些不需要传入soapAction，根据wsdl文档
        ht.call(soapAction, envelope);
        return envelope;
    }
    /**
     * @param request
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static SoapSerializationEnvelope getEnvelope(SoapObject request)
            throws IOException, XmlPullParserException {
        return getEnvelope(request, SoapEnvelope.VER11, null, ROOT_URL, false);
    }

    /**
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static String getNewOrder(String xml)
            throws IOException, XmlPullParserException {
        SoapObject object = new SoapObject(NAMESPACE,
                "sfexpressService");
        byte[] byteCode = Base64.encode((Md5.getMD5Bytes(xml + SFWsUtils
                .verifyCode)), Base64.NO_WRAP);
        String verifyCode = new String(byteCode, "UTF-8");
        object.addProperty("arg0", xml);
        object.addProperty("arg1", verifyCode);
        SoapSerializationEnvelope envelope =getEnvelope(object);
        SoapPrimitive soapPrimitive = (SoapPrimitive) envelope.getResponse();
        return soapPrimitive.toString();
    }

    public static SoapSerializationEnvelope getEnvelope(LinkedHashMap<String, Object> properties,
                                                        String method)
            throws IOException, XmlPullParserException {
        SoapObject request = getRequest(properties, method);
        return getEnvelope(request, SoapEnvelope.VER11, null, ROOT_URL, false);
    }

    public static String createOrderXml(String serviceName, SFSender info, List<Cargo>
            cargos,
                                        List<ExtraService> services) {

        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
            Document xmlDoc = xmlBuilder.newDocument();
            Element root = xmlDoc.createElement("Request");
            root.setAttribute("service", serviceName);
            root.setAttribute("lang", "zh-CN");
            Element head = xmlDoc.createElement("Head");
            head.setTextContent(SFWsUtils.head);
            root.appendChild(head);
            Element body = xmlDoc.createElement("Body");
            Element order = xmlDoc.createElement("Order");
            order.setAttribute("orderid", info.orderID);
            order.setAttribute("is_gen_bill_no", "1");
            //生成运单，以下3个必填
            order.setAttribute("j_company", info.j_company);
            order.setAttribute("j_contact", info.j_name);
            order.setAttribute("j_tel", info.j_tel);
            //可选寄件人的电话
            order.setAttribute("j_mobile", info.j_cellphone);
            //跨境必填，寄件方国家/城市代码
            order.setAttribute("j_shippercode", info.j_code);
            //
            if (info.custid != null) {
                order.setAttribute("custid", info.custid);
            }
            order.setAttribute("j_province", info.j_province);
            order.setAttribute("j_city", info.j_city);
            order.setAttribute("j_county", info.j_district);
            //电子运单必填
            order.setAttribute("j_address", info.j_address);
            //跨境必填，寄件方邮编
            order.setAttribute("j_post_code", info.j_postcode);
            order.setAttribute("express_type", info.expressType);
            order.setAttribute("pay_method", info.payType);
            order.setAttribute("parcel_quantity", info.bagCounts);
            //            order.setAttribute("cargo_length", "33");
            //            order.setAttribute("cargo_width", "33");
            //            order.setAttribute("cargo_height", "33");
            //            order.setAttribute("remark", "33");
            //以下3个必填
            order.setAttribute("d_company", info.d_company);
            order.setAttribute("d_contact", info.d_name);
            order.setAttribute("d_tel", info.d_tel);

            //可选
            order.setAttribute("d_mobile", info.d_tel);
            //必须详情
            order.setAttribute("d_address", info.d_address);
            //跨境必填，到方国家城市代码
            order.setAttribute("d_deliverycode", info.d_postcode);
            if (cargos != null) {
                for (int i = 0; i < cargos.size(); i++) {
                    Element cargoEle = xmlDoc.createElement("Cargo");
                    Cargo cargo = cargos.get(i);
                    cargoEle.setAttribute("name", cargo.getName());
                    cargoEle.setAttribute("count", cargo.getCount());
                    //                    cargoEle.setAttribute("unit", cargo.getUnit());
                    //                    cargoEle.setAttribute("weight", cargo
                    // .getWeight());
                    //                    cargoEle.setAttribute("amount", cargo
                    // .getAmount());
                    //                    cargoEle.setAttribute("currency", cargo
                    // .getCurrency());
                    //                    cargoEle.setAttribute("source_area", cargo
                    // .getOriginCountry());
                    //                    cargoEle.setAttribute("product_record_no",
                    // cargo.getProductRecordNo
                    //                            ());
                    //                    cargoEle.setAttribute("good_prepard_no",
                    // cargo.getGoodHaiguanRecord
                    //                            ());
                    order.appendChild(cargoEle);
                }
            }

            if (services != null) {
                for (int i = 0; i < services.size(); i++) {
                    Element addedService = xmlDoc.createElement("AddedService");
                    ExtraService tempService = services.get(i);
                    addedService.setAttribute("name", tempService.name);
                    addedService.setAttribute("value", tempService.value);
                    if (tempService.value1 != null) {
                        addedService.setAttribute("value1", tempService.value1);
                    }
                    order.appendChild(addedService);
                }
            }
            body.appendChild(order);
            root.appendChild(body);
            xmlDoc.appendChild(root);
            //定义了用于处理转换指令，以及执行从源到结果的转换的
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("encoding", "UTF-8");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
            Log.e("zjy", "sfWs->sendRequest(): xmlResult==" + writer.toString
                    ());
            return writer.toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
