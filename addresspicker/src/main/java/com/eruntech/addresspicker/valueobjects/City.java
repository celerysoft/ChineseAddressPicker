package com.eruntech.addresspicker.valueobjects;

import java.util.List;

/**
 * 时间：2015-09-08
 * @author 作者：Qin Yuanyi
 * 功能：城市信息模型，包含城市名称，城市区域列表
 */
public class City {
    private String name;
    private List<District> districtList;

    public City() {
        super();
    }

    public City(String name, List<District> districtList) {
        super();
        this.name = name;
        this.districtList = districtList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<District> getDistrictList() {
        return districtList;
    }

    public void setDistrictList(List<District> districtList) {
        this.districtList = districtList;
    }

    @Override
    public String toString() {
        return "City [name=" + name + ", districtList=" + districtList
                + "]";
    }
}
