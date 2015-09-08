package com.eruntech.chineseaddresspicker.valueobjects;

import java.util.List;

/**
 * 时间：2015-09-08
 * @author 作者：Qin Yuanyi
 * 功能：省份信息模型，包含省份名称，省份城市列表
 */
public class ProvinceModel {
    private String name;
    private List<CityModel> cityList;

    public ProvinceModel() {
        super();
    }

    public ProvinceModel(String name, List<CityModel> cityList) {
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

    public List<CityModel> getCityList() {
        return cityList;
    }

    public void setCityList(List<CityModel> cityList) {
        this.cityList = cityList;
    }

    @Override
    public String toString() {
        return "ProvinceModel [name=" + name + ", cityList=" + cityList + "]";
    }
}
