package com.eruntech.addresspicker.valueobjects;

import java.util.List;

/**
 * 时间：2015-09-08
 * @author 作者：Qin Yuanyi
 * 功能：城市信息模型，包含城市名称，城市序号，城市区域列表
 */
public class City {
    private String name;
    private int index;
    private List<District> districtList;

    public City() {
        super();
    }

    public City(String name, int index, List<District> districtList) {
        super();
        this.name = name;
        this.index = index;
        this.districtList = districtList;
    }

    @Deprecated
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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
