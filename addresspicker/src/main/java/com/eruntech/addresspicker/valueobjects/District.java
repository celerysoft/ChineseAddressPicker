package com.eruntech.addresspicker.valueobjects;

/**
 * 时间：2015-09-08
 * @author 作者：Qin Yuanyi
 * 功能：区域信息模型，包含区域名称和邮编
 */
public class District {
    private String name;
    private String zipcode;

    public District() {
        super();
    }

    public District(String name, String zipcode) {
        super();
        this.name = name;
        this.zipcode = zipcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    @Override
    public String toString() {
        return "District [name=" + name + ", zipcode=" + zipcode + "]";
    }
}
