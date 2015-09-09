package com.eruntech.chineseaddresspicker.valueobjects;

import java.util.List;

/**
 * 时间：2015-09-08
 * @author 作者：Qin Yuanyi
 * 功能：省份信息模型，包含省份名称，省份城市列表
 */
public class Province {
    private String name;
    private List<City> cityList;

    public Province() {
        super();
    }

    public Province(String name, List<City> cityList) {
        super();
        this.name = name;
        this.cityList = cityList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<City> getCityList() {
        return cityList;
    }

    public void setCityList(List<City> cityList) {
        this.cityList = cityList;
    }

    @Override
    public String toString() {
        return "Province [name=" + name + ", cityList=" + cityList + "]";
    }
}
