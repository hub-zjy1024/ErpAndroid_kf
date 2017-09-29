package printer.entity;

/**
 * Created by 张建宇 on 2017/6/23.
 */

public class SFSender {
    public String orderID;
    public String j_company;
    public String j_name;
    public String j_code;
    public String j_tel;
    public String j_cellphone;
    public String j_username;
    public String j_country;
    public String j_province;
    public String j_city;
    public String j_district;
    public String j_address;
    public String j_postcode;
    public boolean needCode;
    public String d_company;
    public String d_name;
    public String d_code;
    public String d_tel;
    public String d_cellphone;
    public String d_username;
    public String d_country;
    public String d_province;
    public String d_city;
    public String d_district;
    public String d_address;
    public String d_postcode;
    public String custid;
    public String expressType;
    public String payType;
    public String bagCounts;



    public SFSender() {
        this.j_username = "";
        this.orderID = "";
        this.j_company = "";
        this.j_name = "";
        this.j_code = "";
        this.j_tel = "";
        this.j_cellphone = "";
        this.j_country = "";
        this.j_province = "";
        this.j_city = "";
        this.j_district = "";
        this.j_address = "";
        this.j_postcode = "";
        this.d_company = "";
        this.d_name = "";
        this.d_code = "";
        this.d_tel = "";
        this.d_cellphone = "";
        this.d_username = "";
        this.d_country = "";
        this.d_province = "";
        this.d_city = "";
        this.d_district = "";
        this.d_address = "";
        this.d_postcode = "";
        this.expressType = "1";
        this.payType = "1";
    }

    @Override
    public String toString() {
        return "SFSender{" +
                "orderID='" + orderID + '\'' +
                ", j_company='" + j_company + '\'' +
                ", j_name='" + j_name + '\'' +
                ", j_code='" + j_code + '\'' +
                ", j_tel='" + j_tel + '\'' +
                ", j_cellphone='" + j_cellphone + '\'' +
                ", j_username='" + j_username + '\'' +
                ", j_country='" + j_country + '\'' +
                ", j_province='" + j_province + '\'' +
                ", j_city='" + j_city + '\'' +
                ", j_district='" + j_district + '\'' +
                ", j_address='" + j_address + '\'' +
                ", j_postcode='" + j_postcode + '\'' +
                ", needCode=" + needCode +
                ", d_company='" + d_company + '\'' +
                ", d_name='" + d_name + '\'' +
                ", d_code='" + d_code + '\'' +
                ", d_tel='" + d_tel + '\'' +
                ", d_cellphone='" + d_cellphone + '\'' +
                ", d_username='" + d_username + '\'' +
                ", d_country='" + d_country + '\'' +
                ", d_province='" + d_province + '\'' +
                ", d_city='" + d_city + '\'' +
                ", d_district='" + d_district + '\'' +
                ", d_address='" + d_address + '\'' +
                ", d_postcode='" + d_postcode + '\'' +
                ", custid='" + custid + '\'' +
                ", expressType='" + expressType + '\'' +
                ", payType='" + payType + '\'' +
                ", bagCounts='" + bagCounts + '\'' +
                '}';
    }
}
