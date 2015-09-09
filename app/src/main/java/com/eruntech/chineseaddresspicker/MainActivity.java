package com.eruntech.chineseaddresspicker;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.eruntech.chineseaddresspicker.interfaces.OnAddressDataServiceListener;
import com.eruntech.chineseaddresspicker.services.LoadAddressDataService;
import com.eruntech.chineseaddresspicker.valueobjects.City;
import com.eruntech.chineseaddresspicker.valueobjects.District;
import com.eruntech.chineseaddresspicker.valueobjects.Province;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 *
 */
public class MainActivity extends Activity
        implements View.OnClickListener, OnWheelChangedListener, OnAddressDataServiceListener {

    /**
     * 所有省
     */
    protected String[] mProvinceDatas;
    /**
     * key - 省 value - 市
     */
    protected Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
    /**
     * key - 市 values - 区
     */
    protected Map<String, String[]> mDistrictDatasMap = new HashMap<String, String[]>();

    /**
     * key - 区 values - 邮编
     */
    protected Map<String, String> mZipcodeDatasMap = new HashMap<String, String>();

    /**
     * 当前省的名称
     */
    protected String mCurrentProviceName;
    /**
     * 当前市的名称
     */
    protected String mCurrentCityName;
    /**
     * 当前区的名称
     */
    protected String mCurrentDistrictName ="";

    /**
     * 当前区的邮政编码
     */
    protected String mCurrentZipCode ="";



    private WheelView mViewProvince;
    private WheelView mViewCity;
    private WheelView mViewDistrict;
    private Button mBtnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoadAddressDataService service = new LoadAddressDataService(this);
        service.startToParseData();
        // 异步方法，解析完地址数据后自动调用 onAddressDataGot() 方法
    }

    @Override
    public void onAddressDataGot(List<Province> provinceList) {
        setContentView(R.layout.activity_main);

        setUpViews();
        setUpListener();
        initProvinceDatas(provinceList);
        setUpData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpViews() {
        mViewProvince = (WheelView) findViewById(R.id.id_province);
        mViewCity = (WheelView) findViewById(R.id.id_city);
        mViewDistrict = (WheelView) findViewById(R.id.id_district);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
    }

    private void setUpListener() {
        // 添加change事件
        mViewProvince.addChangingListener(this);
        // 添加change事件
        mViewCity.addChangingListener(this);
        // 添加change事件
        mViewDistrict.addChangingListener(this);
        // 添加onclick事件
        mBtnConfirm.setOnClickListener(this);
    }

    private void setUpData() {
        mViewProvince.setViewAdapter(new ArrayWheelAdapter<String>(MainActivity.this, mProvinceDatas));
        // 设置可见条目数量
        mViewProvince.setVisibleItems(7);
        mViewCity.setVisibleItems(7);
        mViewDistrict.setVisibleItems(7);
        updateCities();
        updateAreas();
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == mViewProvince) {
            updateCities();
        } else if (wheel == mViewCity) {
            updateAreas();
        } else if (wheel == mViewDistrict) {
            mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[newValue];
            mCurrentZipCode = mZipcodeDatasMap.get(mCurrentDistrictName);
        }
    }

    /**
     * 根据当前的市，更新区WheelView的信息
     */
    private void updateAreas() {
        int pCurrent = mViewCity.getCurrentItem();
        mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
        String[] areas = mDistrictDatasMap.get(mCurrentCityName);

        if (areas == null) {
            areas = new String[] { "" };
        }
        mViewDistrict.setViewAdapter(new ArrayWheelAdapter<String>(this, areas));
        mViewDistrict.setCurrentItem(0);
    }

    /**
     * 根据当前的省，更新市WheelView的信息
     */
    private void updateCities() {
        int pCurrent = mViewProvince.getCurrentItem();
        mCurrentProviceName = mProvinceDatas[pCurrent];
        String[] cities = mCitisDatasMap.get(mCurrentProviceName);
        if (cities == null) {
            cities = new String[] { "" };
        }
        mViewCity.setViewAdapter(new ArrayWheelAdapter<String>(this, cities));
        mViewCity.setCurrentItem(0);
        updateAreas();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                showSelectedResult();
                setSelectedResult();
                break;
            default:
                break;
        }
    }



    private void initProvinceDatas(List<Province> provinceList) {
        if (provinceList!= null && !provinceList.isEmpty()) {
            mCurrentProviceName = provinceList.get(0).getName();
            List<City> cityList = provinceList.get(0).getCityList();
            if (cityList!= null && !cityList.isEmpty()) {
                mCurrentCityName = cityList.get(0).getName();
                List<District> districtList = cityList.get(0).getDistrictList();
                mCurrentDistrictName = districtList.get(0).getName();
                mCurrentZipCode = districtList.get(0).getZipcode();
            }
        }

        //TODO update store to hashmap
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
                String[] distrinctNameArray = new String[districtList.size()];
                District[] distrinctArray = new District[districtList.size()];
                for (int k = 0; k < districtList.size(); k++) {
                    // 遍历市下面所有区/县的数据
                    District districtModel = new District(districtList.get(k).getName(), districtList.get(k).getZipcode());
                    // 区/县对于的邮编，保存到mZipcodeDatasMap
                    mZipcodeDatasMap.put(districtList.get(k).getName(), districtList.get(k).getZipcode());
                    distrinctArray[k] = districtModel;
                    distrinctNameArray[k] = districtModel.getName();
                }
                // 市-区/县的数据，保存到mDistrictDatasMap
                mDistrictDatasMap.put(cityNames[j], distrinctNameArray);
            }
            // 省-市的数据，保存到mCitisDatasMap
            mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
        }
    }

    private void showSelectedResult() {
        Toast.makeText(MainActivity.this, "当前选中:" + mCurrentProviceName + "," + mCurrentCityName + ","
                + mCurrentDistrictName + "," + mCurrentZipCode, Toast.LENGTH_SHORT).show();
    }

    private void setSelectedResult() {
        //TODO finish seting result;
        setResult(RESULT_OK);
    }


}
