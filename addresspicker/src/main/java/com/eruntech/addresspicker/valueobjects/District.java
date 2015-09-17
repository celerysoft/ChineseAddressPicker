package com.eruntech.addresspicker.valueobjects;

/**
 * 时间：2015-09-08
 * @author 作者：Qin Yuanyi
 * 功能：区域信息模型，包含区域名称和邮编
 */
public class District {
    private String name;
    @Deprecated
    private String zipcode;
    private int index;

    public District() {
        super();
    }

    public District(String name, int index) {
        super();
        this.name = name;
        this.index = index;
    }

    @Deprecated
    public District(String name, String zipcode) {
        this.name = name;
        this.zipcode = zipcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "District [name=" + name + ", index=" + index + "]";
    }
}
