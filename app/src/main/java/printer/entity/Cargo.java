package printer.entity;

/**
 * Created by 张建宇 on 2017/6/23.
 */

public class Cargo {
    /**
     * 必选，其他的为可选项，跨境才必须有的
     */
    private String name;
    private String unit;
    private String count;
    private String weight;
    private String amount;
    /**
     * 货币种类
     * CNY: 人民币
     *  HKD: 港币
     *  USD: 美元
     *  NTD: 新台币
     *  RUB: 卢布
     *  EUR: 欧元
     *  MOP: 澳门元
     *  SGD: 新元
     *  JPY: 日元
     *  KRW: 韩元
     *  MYR: 马币
     *  VND: 越南盾
     *  THB: 泰铢
     *  AUD: 澳大利亚元
     *  MNT: 图格里克
     * 跨境件报关需要填写。
     */
    private String currency;

    /**
     * source_area 原产地（国家）
     */
    private String originCountry;
    /**
     * 货物产品国检备案编号
     */
    private String productRecordNo;
    /**
     * 商品海关备案号
     */
    private String goodHaiguanRecord;

    public Cargo() {
        currency = "";
        originCountry = "";
        productRecordNo = "";
        goodHaiguanRecord = "";
        weight = "";
        amount = "";
        unit = "";
        count = "";
        name = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getProductRecordNo() {
        return productRecordNo;
    }

    public void setProductRecordNo(String productRecordNo) {
        this.productRecordNo = productRecordNo;
    }

    public String getGoodHaiguanRecord() {
        return goodHaiguanRecord;
    }

    public void setGoodHaiguanRecord(String goodHaiguanRecord) {
        this.goodHaiguanRecord = goodHaiguanRecord;
    }



}
