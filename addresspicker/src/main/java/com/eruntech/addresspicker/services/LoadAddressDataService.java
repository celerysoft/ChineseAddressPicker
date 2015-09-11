package com.eruntech.addresspicker.services;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.eruntech.addresspicker.interfaces.OnAddressDataServiceListener;
import com.eruntech.addresspicker.valueobjects.City;
import com.eruntech.addresspicker.valueobjects.District;
import com.eruntech.addresspicker.valueobjects.Province;
import com.eruntech.addresspicker.widgets.ChineseAddressPicker;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse the local Chinese address data in assets.
 * @author Qin Yuanyi
 * 时间：2015-09-09
 * 功能：解析储存在assets的中国地址库数据
 */
public class LoadAddressDataService {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private ChineseAddressPicker mChineseAddressPicker;
    private OnAddressDataServiceListener mOnAddressDataServiceListener;

    /** namespace **/
    private final String ns = null;

    /**
     * 储存所有的解析对象
     */
    //private List<Province> mProvinceList = new ArrayList<Province>();
    private List<Province> mProvinceList;
    public List<Province> getProvinceList() {
        return mProvinceList;
    }

    public LoadAddressDataService(ChineseAddressPicker chineseAddressPicker) {
        mChineseAddressPicker = chineseAddressPicker;
        if (chineseAddressPicker instanceof OnAddressDataServiceListener) {
            mOnAddressDataServiceListener = (OnAddressDataServiceListener) chineseAddressPicker;
        } else {
                Log.w(LOG_TAG, "LoadAddressDataService构造函数传进的参数必须实现OnTubeViewServiceListener接口");
        }
    }

    public void startToParseData() {
        new GetAddressDataAsyncTask().execute("address_data.xml");
    }

    private void parseXmlData(String xmlPath) throws XmlPullParserException, IOException {

        AssetManager asset = mChineseAddressPicker.getContext().getAssets();
        InputStream xmlStream = asset.open(xmlPath);

        XmlPullParser parser = Xml.newPullParser();
        //parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(xmlStream, "UTF-8");
        parser.next();

        try {
            mProvinceList = readRoot(parser);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            xmlStream.close();
        }

    }

    private List<Province> readRoot(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "root");

        List<Province> provinceList = new ArrayList<Province>();

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equalsIgnoreCase("province")) {
                provinceList.add(readProvince(parser));
            } else {
                Log.e(LOG_TAG, "root标签的下级标签应为province，请检查地址xml文件的正确性");
            }
        }

        return provinceList;
    }

    /**
     * 读取root标签，遍历root标签下的province标签，构建省份数据
     * @param parser 解析xml的parser，确保parser的位置位于root标签的startTag
     * @return 包含所有省份信息的List
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Province readProvince(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "province");

        Province province = null;
        String provinceName = parser.getAttributeValue(ns, "name");
        List<City> cityList = new ArrayList<City>();

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equalsIgnoreCase("city")) {
                cityList.add(readCity(parser));
            } else {
                Log.e(LOG_TAG, "province标签的下级标签应为city，请检查地址xml文件的正确性");
            }
        }

        province = new Province(provinceName, cityList);

        return province;
    }

    /**
     * 读取provice标签，遍历provice标签下的city标签，构建城市数据
     * @param parser 解析xml的parser，确保parser的位置位于provice标签的startTag
     * @return 包含所有管孔信息的List
     * @throws Exception
     */
    private City readCity(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "city");

        City city = null;
        String cityName = parser.getAttributeValue(ns, "name");;
        List<District> districtList = new ArrayList<District>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equalsIgnoreCase("district")) {
                districtList.add(readDistrict(parser));
            } else {
                Log.e(LOG_TAG, "city标签的下级标签应为district，请检查地址xml文件的正确性");
            }

        }

        city = new City(cityName, districtList);

        return city;
    }

    /**
     * 读取city标签，遍历city标签下的district标签，构建区域数据
     * @param parser 解析xml的parser，确保parser的位置位于record标签的startTag
     * @return 区域数据对象
     * @throws XmlPullParserException
     * @throws IOException
     */
    private District readDistrict(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "district");

        District district;
        String name = null;
        String zipCode = null;

        if (parser.getName().equalsIgnoreCase("district")) {
            // 为了让区域名称String指向的内存地址不同，建立IdentityHashMap时防止“其他”区域的邮编一致的bug
            name = new String(parser.getAttributeValue(ns, "name"));
            zipCode = parser.getAttributeValue(ns, "zipcode");
            parser.nextTag();
        } else {
            Log.w(LOG_TAG, "city下出现非district标签");
        }

        district = new District(name, zipCode);

        return district;
    }

    /** 用于解析地址xml文件的异步线程类 **/
    private class GetAddressDataAsyncTask extends AsyncTask<String, Object, Object> {
        /**
         * 解析地址xml文件
         */
        @Override
        protected String doInBackground(String... params) {
            try {
                parseXmlData(params[0]);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        /**
         * 通知mOnAddressDataServiceListener已经获取地址数据
         */
        @Override
        protected void onPostExecute(Object result) {
            if (mProvinceList != null) {
                if (mProvinceList.size() > 0) {
                    mOnAddressDataServiceListener.onAddressDataGot(mProvinceList);
                }
            }

        }

    }
}
