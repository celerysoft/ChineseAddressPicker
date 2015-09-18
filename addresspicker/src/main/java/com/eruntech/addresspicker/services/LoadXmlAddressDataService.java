package com.eruntech.addresspicker.services;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.eruntech.addresspicker.interfaces.Sortable;
import com.eruntech.addresspicker.valueobjects.City;
import com.eruntech.addresspicker.valueobjects.District;
import com.eruntech.addresspicker.valueobjects.Province;
import com.eruntech.addresspicker.widgets.ChineseAddressPicker;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse the local Chinese address XML data in assets.
 * <P>作者：Qin Yuanyi
 * <P>时间：2015-09-09
 * <P>功能：解析储存在assets的中国地址库XML数据
 */
public class LoadXmlAddressDataService {

    /** Log tag **/
    private final String LOG_TAG = this.getClass().getSimpleName();

    /** 选择地址的时候没有选中具体地址时的字符串 **/
    private final String NO_ADDRESS_PICKED = "-----";

    /** 解析index节点时的结果转换为int型失败时，index的默认值 **/
    private final int INDEX_PARSE_WRONG = -1;

    /** ChineseAddressPicker控件的引用 **/
    private ChineseAddressPicker mChineseAddressPicker;

    /** namespace **/
    private final String ns = null;

    /** 储存所有的解析对象 **/
    private List<Province> mProvinceList;

    /** 所有省 **/
    private String[] mProvinceDatas;
    /** key - 省 value - 市 **/
    private Map<String, String[]> mCitisDatasMap = new HashMap<>();
    /** key - 市 values - 区 **/
    private Map<String, String[]> mDistrictDatasMap = new HashMap<>();

    private boolean mIsDataSortedByPronunciation;

    public LoadXmlAddressDataService(ChineseAddressPicker chineseAddressPicker) {
        mChineseAddressPicker = chineseAddressPicker;
    }

    /**
     * <P>修改时间：2015-09-18
     * <P>作者：Qin Yuanyi
     * <P>功能描述：发送异步请求开始解析储存在本地的中国地址数据库
     * @param isDataSortByPronunciation 解析好的数据是否按照读音排序
     */
    public void startToParseData(boolean isDataSortByPronunciation) {
        mIsDataSortedByPronunciation = isDataSortByPronunciation;
        new GetAddressDataAsyncTask().execute("address_data.xml");
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：解析位置assets下指定位置的本地xml文件
     * @param xmlPath xml文件的位置
     */
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

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：读取province标签，遍历root标签下的province标签，构建省份数据
     * @param parser 解析xml的parser，确保parser的位置位于province标签的startTag
     * @return 包含所有省份信息的List
     */
    private List<Province> readRoot(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "root");

        List<Province> provinceList = new ArrayList<>();

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
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：读取province标签，遍历root标签下的province标签，构建省份数据
     * @param parser 解析xml的parser，确保parser的位置位于province标签的startTag
     * @return 包含所有省份信息的List
     */
    private Province readProvince(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "province");

        Province province;
        String provinceName = parser.getAttributeValue(ns, "name");
        int index;
        try {
            index = Integer.parseInt(parser.getAttributeValue(ns, "index"));
        } catch (NumberFormatException e) {
            index = INDEX_PARSE_WRONG;
            Log.e(LOG_TAG, provinceName + "的序号转换成int型时出错，请检查数据是否正确");
        }
        List<City> cityList = new ArrayList<>();

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

        province = new Province(provinceName, index, cityList);

        return province;
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：读取city标签，遍历provice标签下的city标签，构建城市数据
     * @param parser 解析xml的parser，确保parser的位置位于city标签的startTag
     * @return 包含所有管孔信息的List
     */
    private City readCity(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "city");

        City city;
        String cityName = parser.getAttributeValue(ns, "name");
        int index;
        try {
            index = Integer.parseInt(parser.getAttributeValue(ns, "index"));
        } catch (NumberFormatException e) {
            index = INDEX_PARSE_WRONG;
            Log.e(LOG_TAG, cityName + "的序号转换成int型时出错，请检查数据是否正确");
        }
        List<District> districtList = new ArrayList<>();

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

        city = new City(cityName, index, districtList);

        return city;
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：读取district标签，遍历city标签下的district标签，构建区域数据
     * @param parser 解析xml的parser，确保parser的位置位于district标签的startTag
     * @return 区域数据对象
     */
    private District readDistrict(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "district");

        District district;
        String districtName = null;
        int index = 0;

        if (parser.getName().equalsIgnoreCase("district")) {
            // 为了让区域名称String指向的内存地址不同，建立IdentityHashMap时防止因内存地址相同导致的bug
            districtName = parser.getAttributeValue(ns, "name");
            try {
                index = Integer.parseInt(parser.getAttributeValue(ns, "index"));
            } catch (NumberFormatException e) {
                index = INDEX_PARSE_WRONG;
                Log.e(LOG_TAG, districtName + "的序号转换成int型时出错，请检查数据是否正确");
            }

            parser.nextTag();
        } else {
            Log.w(LOG_TAG, "city下出现非district标签");
        }

        district = new District(districtName, index);

        return district;
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：对数据进行排序，并创建哈希表储存解析好的数据，加快读取速度
     */
    private void initParsedDatas(List<Province> provinceList) {
        if (provinceList!= null && !provinceList.isEmpty()) {

            // sort the address datas by index
            if (!mIsDataSortedByPronunciation) {
                int provinceCount = provinceList.size();
                int cityCount;

                Collections.sort(provinceList, new AddressComparator());
                for(int i = 0; i < provinceCount; ++i) {
                    List<City> cityList = provinceList.get(i).getCityList();
                    Collections.sort(cityList, new AddressComparator());
                    cityCount = cityList.size();
                    for (int j = 0; j < cityCount; ++j) {
                        List<District> districtList = cityList.get(j).getDistrictList();
                        Collections.sort(districtList, new AddressComparator());
                    }
                }
            }

            // store address datas to map
            mProvinceDatas = new String[provinceList.size()];
            for (int i=0; i< provinceList.size(); i++) {
                // 遍历所有省的数据
                mProvinceDatas[i] = provinceList.get(i).getName();
                List<City> cityList = provinceList.get(i).getCityList();
                String[] cityNames = new String[cityList.size()];
                for (int j = 0; j < cityList.size(); j++) {
                    // 遍历省下面的所有市的数据
                    cityNames[j] = cityList.get(j).getName();
                    List<District> districtList = cityList.get(j).getDistrictList();
                    // 需要在 distrinctNameArray[0] 插入字符串 “-----”，所以size+1
                    String[] distrinctNameArray = new String[districtList.size()+1];
                    distrinctNameArray[0] = NO_ADDRESS_PICKED;
                    for (int k = 0; k < districtList.size(); k++) {
                        // 遍历市下面所有区/县的数据
                        District districtModel = new District(districtList.get(k).getName(), districtList.get(k).getIndex());
                        distrinctNameArray[k+1] = districtModel.getName();
                    }
                    // 市-区/县的数据，保存到mDistrictDatasMap
                    mDistrictDatasMap.put(cityNames[j], distrinctNameArray);
                }
                // 省-市的数据，保存到mCitisDatasMap
                mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
            }
        }
    }

    /**
     * <P>时间：2015-09-17
     * <P>作者：Qin Yuanyi
     * <P>功能：对省、市、区按序号进行排序的比较器
     */
    private class AddressComparator implements Comparator<Sortable> {
        @Override
        public int compare(Sortable lhs, Sortable rhs) {
            return lhs.getIndex() - rhs.getIndex();
        }
    }

    /**
     * <P>作者：Qin Yuanyi
     * <P>时间：2015-09-09
     * <P>功能：用于解析地址xml文件的异步线程类
     */
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
                Log.e(LOG_TAG, "解析XML文件时发生错误，错误原因：" + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "读取XML文件时发生错误，错误原因：" + e.getMessage());
            }
            return null;
        }



        /**
         * 通知mChineseAddressPicker已经获取地址数据
         */
        @Override
        protected void onPostExecute(Object result) {
            if (mProvinceList != null) {
                if (mProvinceList.size() > 0) {
                    try {
                        initParsedDatas(mProvinceList);
                    } finally {
                        mChineseAddressPicker.onAddressDataGot(mProvinceDatas, mCitisDatasMap, mDistrictDatasMap);
                    }
                }
            }
        }
    }
}
