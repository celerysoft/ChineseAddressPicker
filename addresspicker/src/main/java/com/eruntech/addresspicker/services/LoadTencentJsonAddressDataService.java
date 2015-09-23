package com.eruntech.addresspicker.services;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eruntech.addresspicker.R;
import com.eruntech.addresspicker.utils.StringUtils;
import com.eruntech.addresspicker.widgets.ChineseAddressPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * <P>作者：Qin Yuanyi
 * <P>时间：2015-09-23
 * <P>功能：通过调用腾讯地图开放平台的WebService API获取地址JSON数据并解析出来
 * <P>API调用方法请参考：http://lbs.qq.com/webservice_v1/guide-region.html
 */
public class LoadTencentJsonAddressDataService {

    /** Log tag **/
    private final String LOG_TAG = this.getClass().getSimpleName();

    private String TENCENT_MAP_KEY;
    private String TENCENT_API_URL;

    /** ChineseAddressPicker控件的引用 **/
    private ChineseAddressPicker mChineseAddressPicker;



    /** 包含所有省的名称 **/
    private String[] mProvinceDatas;
    /** key - 省 value - 市 **/
    private Map<String, String[]> mCityDatasMap = new HashMap<>();
    /** key - 市 values - 区 **/
    private Map<String, String[]> mDistrictDatasMap = new HashMap<>();

    public LoadTencentJsonAddressDataService(ChineseAddressPicker chineseAddressPicker) {
        mChineseAddressPicker = chineseAddressPicker;
        TENCENT_MAP_KEY = chineseAddressPicker.getContext().getString(R.string.tencent_map_key);
        TENCENT_API_URL = chineseAddressPicker.getContext().getString(R.string.tencent_api_url);
    }

    public void startToParseData() {
        //new getAddressDataThread().run();
        new GetAddressDataAsyncTask().execute("");
    }

    /**
     * <P>修改时间：2015-09-23
     * <P>作者：Qin Yuanyi
     * <P>功能描述：调用腾讯API获取地址JSON数据
     */
    private String requestDataFromTencentServer() throws IOException, JSONException {
        String json = null;
        JSONObject outputJsonObject = new JSONObject();
        JSONArray outputProvinceArray = new JSONArray();
        JSONObject outputProvinceObject;
        boolean isMunicipality = false;
        final String[] municipalities = new String[]{"北京", "天津", "上海", "重庆"};


        String mainJsonString = requestTencentData(null);
        JSONObject mainJsonObject = new JSONObject(mainJsonString);
        JSONArray provinceJsonArray = mainJsonObject.getJSONArray("result").getJSONArray(0);
        JSONObject provinceJsonObject;
        int ProvinceCount = provinceJsonArray.length();
        for (int i = 0; i < ProvinceCount; ++i) {
            Log.d(LOG_TAG, "i=" + i);
            outputProvinceObject = provinceJsonArray.getJSONObject(i);
            String provinceName = outputProvinceObject.getString("name");
            for (String municipalityName : municipalities) {
                if (provinceName.equals(municipalityName)) {
                    isMunicipality = true;
                    break;
                } else {
                    isMunicipality = false;
                }
            }
            String provinceId = outputProvinceObject.getString("id");
            String provinceJsonString = requestTencentData(provinceId);
            provinceJsonObject = new JSONObject(provinceJsonString);
            JSONArray cityJsonArray = provinceJsonObject.getJSONArray("result").getJSONArray(0);
            JSONArray outputCityArray = provinceJsonObject.getJSONArray("result").getJSONArray(0);
            JSONObject outputCityObject;
            int cityCount;
            if (isMunicipality) {
                cityCount = 0;
            } else {
                cityCount = cityJsonArray.length();
            }
            for (int j = 0; j < cityCount; ++j) {
                outputCityObject = cityJsonArray.getJSONObject(i);
                String cityId = outputCityObject.getString("id");
                String cityJsonString = requestTencentData(cityId);
                JSONObject cityJsonObject = new JSONObject(cityJsonString);
                JSONArray districtJsonArray = cityJsonObject.getJSONArray("result").getJSONArray(0);
                JSONArray outputDistrictArray = cityJsonObject.getJSONArray("result").getJSONArray(0);
                outputCityObject.put("district", outputDistrictArray);
                outputCityArray.put(outputCityObject);
            }
            if (isMunicipality) {
                JSONArray outputDistrictArray = outputCityArray;
                outputCityObject = outputProvinceObject;
                outputCityObject.put("district", outputDistrictArray);
                outputCityArray.put(outputCityObject);
                outputProvinceObject.put("city", outputCityArray);
                outputProvinceObject.remove("district");
                outputProvinceArray.put(outputProvinceObject);
            } else {
                outputProvinceObject.put("city", outputCityArray);
                outputProvinceArray.put(outputProvinceObject);
            }
            Log.d(LOG_TAG, outputProvinceArray.toString());
        }


        outputJsonObject.put("data", outputProvinceArray);
        json = outputJsonObject.toString();
        Log.i(LOG_TAG, json);

        return json;
    }

    private void test() {

    }

    /**
     * <P>修改时间：2015-09-23
     * <P>作者：Qin Yuanyi
     * <P>功能描述：调用腾讯API获取地址JSON数据
     */
    private String requestTencentData(String id) throws IOException {
        InputStream inputStream = null;
        int len = 1024;

        try {
            String connectedUrl = TENCENT_API_URL + "?";
            if (id != null) {
                connectedUrl += "&id=" + id;
            }
            connectedUrl += "&key=" + TENCENT_MAP_KEY;
            URL url = new URL(connectedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            // response == 200 代表连接成功
            int response = conn.getResponseCode();
            Log.d(LOG_TAG, "响应为: " + response);
            inputStream = conn.getInputStream();

            String contentAsString = StringUtils.InputStreamToString(inputStream, len, "UTF-8");
            return contentAsString;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private class getAddressDataThread extends Thread {
        @Override
        public void run() {
            try {
                requestDataFromTencentServer();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.run();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private class GetAddressDataAsyncTask extends AsyncTask<String, Object, Object> {
        @Override
        protected String doInBackground(String... params) {
            try {
                requestDataFromTencentServer();
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
            //mChineseAddressPicker.onAddressDataGot(mProvinceDatas, mCityDatasMap, mDistrictDatasMap);
        }
    }

}
