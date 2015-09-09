package com.eruntech.chineseaddresspicker.services;

import com.eruntech.chineseaddresspicker.valueobjects.City;
import com.eruntech.chineseaddresspicker.valueobjects.District;
import com.eruntech.chineseaddresspicker.valueobjects.Province;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 时间：2015-09-08
 * @author 作者：Qin Yuanyi
 * 功能：解析
 */
public class XmlParserHandler extends DefaultHandler {
    /**
     * 存储所有的解析对象
     */
    private List<Province> provinceList = new ArrayList<Province>();

    public XmlParserHandler() {

    }

    public List<Province> getDataList() {
        return provinceList;
    }

    @Override
    public void startDocument() throws SAXException {
        // 当读到第一个开始标签的时候，会触发这个方法
    }

    Province provinceModel = new Province();
    City cityModel = new City();
    District districtModel = new District();

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // 当遇到开始标记的时候，调用这个方法
        if (qName.equals("province")) {
            provinceModel = new Province();
            provinceModel.setName(attributes.getValue(0));
            provinceModel.setCityList(new ArrayList<City>());
        } else if (qName.equals("city")) {
            cityModel = new City();
            cityModel.setName(attributes.getValue(0));
            cityModel.setDistrictList(new ArrayList<District>());
        } else if (qName.equals("district")) {
            districtModel = new District();
            districtModel.setName(attributes.getValue(0));
            districtModel.setZipcode(attributes.getValue(1));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // 遇到结束标记的时候，会调用这个方法
        if (qName.equals("district")) {
            cityModel.getDistrictList().add(districtModel);
        } else if (qName.equals("city")) {
            provinceModel.getCityList().add(cityModel);
        } else if (qName.equals("province")) {
            provinceList.add(provinceModel);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }
}
