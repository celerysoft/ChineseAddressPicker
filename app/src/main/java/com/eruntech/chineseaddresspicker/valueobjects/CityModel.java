package com.eruntech.chineseaddresspicker.valueobjects;

import java.util.List;

/**
 * 时间：2015-09-08
 * @author 作者：Qin Yuanyi
 * 功能：城市信息模型，包含城市名称，城市区域列表
 */
public class CityModel {
    private String name;
    private List<DistrictModel> districtList;

    public CityModel() {
        super();
    }

    public CityModel(String name, List<DistrictModel> districtList) {
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

    public List<DistrictModel> getDistrictList() {
        return districtList;
    }

    public void setDistrictList(List<DistrictModel> districtList) {
        this.districtList = districtList;
    }

    @Override
    public String toString() {
        return "CityModel [name=" + name + ", districtList=" + districtList
                + "]";
    }
}
