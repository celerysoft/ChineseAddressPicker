package com.eruntech.addresspicker.services;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.eruntech.addresspicker.utils.StringUtils;
import com.eruntech.addresspicker.widgets.ChineseAddressPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse the local Chinese address JSON data in assets.
 * <P>作者：Qin Yuanyi
 * <P>时间：2015-09-18
 * <P>功能：解析储存在assets的中国地址库JSON数据
 */
public class LoadJsonAddressDataService {

    /** Log tag **/
    private final String LOG_TAG = this.getClass().getSimpleName();

    /** ChineseAddressPicker控件的引用 **/
    private ChineseAddressPicker mChineseAddressPicker;

    /** 包含所有省的名称 **/
    private String[] mProvinceDatas;
    /** key - 省 value - 市 **/
    private Map<String, String[]> mCityDatasMap = new HashMap<>();
    /** key - 市 values - 区 **/
    private Map<String, String[]> mDistrictDatasMap = new HashMap<>();

    public LoadJsonAddressDataService(ChineseAddressPicker chineseAddressPicker) {
        mChineseAddressPicker = chineseAddressPicker;
    }

    /**
     * <P>修改时间：2015-09-18
     * <P>作者：Qin Yuanyi
     * <P>功能描述：发送异步请求开始解析储存在本地的中国地址数据库
     */
    public void startToParseData() {
        new GetAddressDataAsyncTask().execute("address_data.json");
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：解析位置assets下指定位置的本地xml文件
     */
    private void parseJsonData() throws JSONException, IOException {

        AssetManager asset = mChineseAddressPicker.getContext().getAssets();

        InputStream jsonStream = asset.open("address_data.json");

        try {
            String json = StringUtils.InputStreamToString(jsonStream, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            jsonObject.remove("整理者");
            JSONArray provincesWithOrder = jsonObject.getJSONArray("中国省份自治区直辖市顺序");
            jsonObject.remove("中国省份自治区直辖市顺序");

            int provinceCount = provincesWithOrder.length();
            mProvinceDatas = new String[provinceCount];
            for (int i = 0; i < provinceCount; ++i) {
                //mProvinceDatas[i] = provincesWithOrder.get(i).toString();
                mProvinceDatas[i] = provincesWithOrder.getString(i);
            }

            if (jsonObject.length() != provincesWithOrder.length()) {
                Log.w(LOG_TAG, "JSON文件中省份数量和中国省份自治区直辖市顺序中的省份数量不一致，请排查JSON文件");
            }

            JSONArray provinceNames = jsonObject.names();
            provinceCount = provinceNames.length();
            int cityCount;
            int districtCount;
            for (int i = 0; i < provinceCount; ++i) {
                String provinceName = provinceNames.getString(i);
                JSONObject province = jsonObject.getJSONObject(provinceName);
                JSONArray cityNames = province.names();
                cityCount = cityNames.length();
                String cityName;
                String[] cityDatas = new String[cityCount];
                for (int j = 0; j < cityCount; ++j) {
                    cityName = cityNames.getString(j);
                    cityDatas[j] = cityName;
                    JSONArray city = province.getJSONArray(cityName);
                    districtCount = city.length();
                    String[] districtDatas = new String[districtCount];
                    for (int k = 0; k < districtCount; ++k) {
                        districtDatas[k] = city.getString(k);
                    }
                    mDistrictDatasMap.put(cityName, districtDatas);
                }
                mCityDatasMap.put(provinceName, cityDatas);
            }
        } finally {
            jsonStream.close();
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
                parseJsonData();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "解析JSON文件时发生错误，错误原因：" + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "读取JSON文件时发生错误，错误原因：" + e.getMessage());
            }
            return null;
        }

        /**
         * 通知mChineseAddressPicker已经获取地址数据
         */
        @Override
        protected void onPostExecute(Object result) {
            mChineseAddressPicker.onAddressDataGot(mProvinceDatas, mCityDatasMap, mDistrictDatasMap);
        }
    }
}
