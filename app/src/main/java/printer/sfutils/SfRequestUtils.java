package printer.sfutils;

import android.util.Base64;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

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
 * Created by 张建宇 on 2017/6/22.
 */

public class SfRequestUtils {
    public static String verifyCode = "j8DzkIFgmlomPt0aLuwU";
    public static String strUrl = "http://bspoisp.sit.sf-express" +
            ".com:11080/bsp-oisp/sfexpressService";
    public static String ORDER_SERVICE = "OrderService";
    public static String ORDER_CONFIRM_SERVICE = "OrderConfirmService";
    public static String ORDER_SEARCH_SERVICE = "OrderSearchService";

    public static String ORDER_FILTER_SERVICE = "OrderFilterService";
    public static String ORDER_FILTER_PUSH_SERVICE = "OrderFilterPushService";

    public void sendRequest(String service, String xml) throws IOException {
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15 * 1000);
        conn.setDoOutput(true);
        OutputStream out = conn.getOutputStream();
        String params = "";
        params += "xml=";
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
            Document xmlDoc = xmlBuilder.newDocument();
            Element root = xmlDoc.createElement("Request");
            root.setAttribute("service", service);
            root.setAttribute("lang", "zh-CN");
            Element head = xmlDoc.createElement("Head");
            head.setTextContent("BSPdevelop");
            root.appendChild(head);
            Element body = xmlDoc.createElement("Body");
            Element order = xmlDoc.createElement("Order");
            order.setAttribute("orderid", "");
            order.setAttribute("j_company", "");
            order.setAttribute("j_contact", "");
            order.setAttribute("j_tel", "");
            order.setAttribute("j_mobile", "");
            order.setAttribute("j_province", "");
            order.setAttribute("j_city", "");
            order.setAttribute("j_county", "");
            order.setAttribute("j_address", "");
            order.setAttribute("d_company", "");
            order.setAttribute("d_contact", "");
            order.setAttribute("d_tel", "");
            order.setAttribute("d_mobile", "");
            order.setAttribute("d_address", "");
            order.setAttribute("express_type", "");
            order.setAttribute("pay_method", "");
            order.setAttribute("parcel_quantity", "");
            order.setAttribute("cargo_length", "");
            order.setAttribute("cargo_width", "");
            order.setAttribute("cargo_height", "");
            order.setAttribute("remark", "");
            Element cargo = xmlDoc.createElement("Cargo");
            cargo.setAttribute("name", "");
            cargo.setAttribute("count", "");
            cargo.setAttribute("unit", "");
            cargo.setAttribute("orderid", "");
            cargo.setAttribute("weight", "");
            cargo.setAttribute("amount", "");
            cargo.setAttribute("currency", "");
            cargo.setAttribute("source_area", "");
            order.appendChild(cargo);
            body.appendChild(order);
            root.appendChild(body);
            xmlDoc.appendChild(root);
            //定义了用于处理转换指令，以及执行从源到结果的转换的
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("encoding", "UTF-8");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
            Log.e("zjy", "SfRequestUtils->sendRequest(): xmlResult==" + writer.toString
                    ());
//            transformer.transform(new DOMSource(xmlDoc), new StreamResult(new File("newxml.xml")));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        String tempXml = "<Request service=\"" + service + "\" lang=\"zh-CN\">" +
                "<Head>BSPdevelop</Head>" +
                "<Body>" +
                "<Order " +
                "orderid=\"TE20150104\" " +
                "j_company=\"罗湖火车站\" " +
                "j_contact=\"小雷\" " +
                "j_tel=\"13810744\" " +
                "j_mobile=\"13111744\" " +
                "j_province=\"广东省\" " +
                "j_city=\"深圳\" " +
                "j_county=\"福田区\" " +
                "j_address=\"罗湖火车站东区调度室\" " +
                "d_company=\"顺丰速运\" " +
                "d_contact=\"小邱\" " +
                "d_tel=\"15819050\" " +
                "d_mobile=\"15539050\" " +
                "d_address=\"北京市海淀区中关村\" " +
                "express_type=\"1\" " +
                "pay_method=\"1\" " +
                "parcel_quantity=\"1\" " +
                "cargo_length=\"33\" " +
                "cargo_width=\"33\" " +
                "cargo_height=\"33\" " +
                "remark=\"\">" +
                "<Cargo " +
                "name=\"LV1\" " +
                "count=\"3\" " +
                "unit=\"a\" " +
                "weight=\"\" " +
                "amount=\"\" " +
                "currency=\"\" " +
                "source_area=\"\"></Cargo>" +
                "<Cargo " +
                "name=\"LV2\" " +
                "count=\"3\" " +
                "unit=\"a\" " +
                "weight=\"\" " +
                "amount=\"\" " +
                "currency=\"\" " +
                "source_area=\"\"></Cargo>" +
                "</Order>" +
                "<Extra " +
                "e1=\"abc\" e2=\"abc\"/>" +
                "</Body>" +
                 "</Request>";
        params += tempXml;
        byte[] verifycode = Base64.encode(Md5.getMD5Bytes(tempXml + verifyCode), Base64
                .NO_WRAP);
        String code = new String(verifycode,"UTF-8");
        params += "&verifyCode=" + code;
        Log.e("zjy", "SfRequestUtils->sendRequest(): xml==" + tempXml);
        Log.e("zjy", "SfRequestUtils->sendRequest(): code==" + code);
        out.write(params.getBytes("UTF-8"));
        out.flush();
        out.close();
        InputStream response = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(response,
                "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String tem = "";
        while ((tem = reader.readLine()) != null) {
            builder.append(tem);
        }
        Log.e("zjy", "SfRequestUtils->sendRequest(): response==" + builder.toString());
    }
}
