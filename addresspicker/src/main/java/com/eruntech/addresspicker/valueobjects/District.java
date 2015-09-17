package com.eruntech.addresspicker.valueobjects;

import com.eruntech.addresspicker.interfaces.Sortable;

/**
 * 时间：2015-09-08
 * @author 作者：Qin Yuanyi
 * 功能：区域信息模型，包含区域名称和序号
 */
public class District implements Sortable {
    private String name;
    private int index;

    public District() {
        super();
    }

    public District(String name, int index) {
        super();
        this.name = name;
        this.index = index;
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
