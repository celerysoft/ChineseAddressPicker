package com.eruntech.addresspicker.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.eruntech.addresspicker.R;
import com.eruntech.addresspicker.interfaces.OnAddressDataServiceListener;
import com.eruntech.addresspicker.services.LoadAddressDataService;
import com.eruntech.addresspicker.valueobjects.City;
import com.eruntech.addresspicker.valueobjects.District;
import com.eruntech.addresspicker.valueobjects.Province;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * Created by Administrator on 2015-09-10.
 */
public class ChineseAddressPicker extends LinearLayout
        implements View.OnClickListener, OnWheelChangedListener, OnAddressDataServiceListener {

    /** 上下文 **/
    private Context mContext;

    /** Log标签 **/
    private final String LOG_TAG = this.getClass().getSimpleName();

    /** 地址选择器可见项目数 **/
    private final int VISIBLE_ITEM_COUNT = 5;

    /** 所有省 **/
    protected String[] mProvinceDatas;
    /** key - 省 value - 市 **/
    protected Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
    /** key - 市 values - 区 **/
    protected Map<String, String[]> mDistrictDatasMap = new HashMap<String, String[]>();
    /** key - 区 values - 邮编**/
    protected Map<String, String> mZipcodeDatasMap = new IdentityHashMap<String, String>();

    /** 当前省的名称 **/
    protected String mCurrentProviceName;
    /** 当前市的名称 **/
    protected String mCurrentCityName;
    /** 当前区的名称 **/
    protected String mCurrentDistrictName ="";
    /** 当前区的邮政编码 **/
    protected String mCurrentZipCode ="";



    private WheelView mViewProvince;
    private WheelView mViewCity;
    private WheelView mViewDistrict;
    private Button mBtnConfirm;

    public ChineseAddressPicker(Context context) {
        super(context);

        mContext = context;

        requestAddressData();
    }

    public ChineseAddressPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public ChineseAddressPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        requestAddressData();
    }

    private void requestAddressData() {
        LoadAddressDataService service = new LoadAddressDataService(this);
        service.startToParseData();
        // 异步方法，解析完地址数据后自动调用 onAddressDataGot() 方法
    }

    @Override
    public void onAddressDataGot(List<Province> provinceList) {
        LayoutInflater.from(getContext()).inflate(R.layout.chinese_address_picker, this);

        setUpViews();
        setUpListener();
        setUpData(provinceList);
    }

    private void setUpViews() {
        mViewProvince = (WheelView) findViewById(R.id.wheelview_province);
        mViewCity = (WheelView) findViewById(R.id.wheelview_city);
        mViewDistrict = (WheelView) findViewById(R.id.wheelview_district);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
    }

    private void setUpListener() {
        mViewProvince.addChangingListener(this);
        mViewCity.addChangingListener(this);
        mViewDistrict.addChangingListener(this);

        mBtnConfirm.setOnClickListener(this);
    }

    private void setUpData(List<Province> provinceList) {
        initProvinceDatas(provinceList);

        mViewProvince.setViewAdapter(new ArrayWheelAdapter<String>(mContext, mProvinceDatas));

        mViewProvince.setVisibleItems(VISIBLE_ITEM_COUNT);
        mViewCity.setVisibleItems(VISIBLE_ITEM_COUNT);
        mViewDistrict.setVisibleItems(VISIBLE_ITEM_COUNT);

        updateCities();
        updateDistricts();
    }

    private void initProvinceDatas(List<Province> provinceList) {
        if (provinceList!= null && !provinceList.isEmpty()) {
            // init default picked address data
            mCurrentProviceName = provinceList.get(0).getName();
            List<City> cityList = provinceList.get(0).getCityList();
            if (cityList!= null && !cityList.isEmpty()) {
                mCurrentCityName = cityList.get(0).getName();
                List<District> districtList = cityList.get(0).getDistrictList();
                mCurrentDistrictName = districtList.get(0).getName();
                mCurrentZipCode = districtList.get(0).getZipcode();
            }

            // store address datas to map
            mProvinceDatas = new String[provinceList.size()];
            for (int i=0; i< provinceList.size(); i++) {
                // 遍历所有省的数据
                mProvinceDatas[i] = provinceList.get(i).getName();
                cityList = provinceList.get(i).getCityList();
                String[] cityNames = new String[cityList.size()];
                for (int j = 0; j < cityList.size(); j++) {
                    // 遍历省下面的所有市的数据
                    cityNames[j] = cityList.get(j).getName();
                    List<District> districtList = cityList.get(j).getDistrictList();
                    String[] distrinctNameArray = new String[districtList.size()];
                    // District[] distrinctArray = new District[districtList.size()];
                    for (int k = 0; k < districtList.size(); k++) {
                        // 遍历市下面所有区/县的数据
                        District districtModel = new District(districtList.get(k).getName(), districtList.get(k).getZipcode());
                        // 区/县对于的邮编，保存到mZipcodeDatasMap
                        mZipcodeDatasMap.put(districtList.get(k).getName(), districtList.get(k).getZipcode());
                        // distrinctArray[k] = districtModel;
                        distrinctNameArray[k] = districtModel.getName();
                    }
                    // 市-区/县的数据，保存到mDistrictDatasMap
                    mDistrictDatasMap.put(cityNames[j], distrinctNameArray);
                }
                // 省-市的数据，保存到mCitisDatasMap
                mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
            }
        }
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == mViewProvince) {
            onProvinceChanged();
        } else if (wheel == mViewCity) {
            onCityChanged();
        } else if (wheel == mViewDistrict) {
            onDistrictChanged();
        }
    }

    private void onProvinceChanged() {
        updateCities();
    }

    private void onCityChanged() {
        updateDistricts();
    }

    private void onDistrictChanged() {
        int currentDistrictIndex = mViewDistrict.getCurrentItem();
        mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[currentDistrictIndex];
        mCurrentZipCode = mZipcodeDatasMap.get(mCurrentDistrictName);
    }
    /**
     * 根据当前的省，更新市WheelView的信息
     */
    private void updateCities() {
        int currentItemIndex = mViewProvince.getCurrentItem();
        mCurrentProviceName = mProvinceDatas[currentItemIndex];
        String[] cities = mCitisDatasMap.get(mCurrentProviceName);
        if (cities == null) {
            cities = new String[] { "数据缺失" };
            Log.w(LOG_TAG, mCurrentProviceName + "下的城市信息缺失。");
        }
        mViewCity.setViewAdapter(new ArrayWheelAdapter<String>(mContext, cities));
        mViewCity.setCurrentItem(0);
        updateDistricts();
    }

    /**
     * 根据当前的市，更新区WheelView的信息
     */
    private void updateDistricts() {
        int currentCityIndex = mViewCity.getCurrentItem();
        mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[currentCityIndex];
        String[] districts = mDistrictDatasMap.get(mCurrentCityName);

        if (districts == null) {
            districts = new String[] { "数据缺失" };
            Log.w(LOG_TAG, mCurrentCityName + "下的区域信息缺失。");
        }
        mViewDistrict.setViewAdapter(new ArrayWheelAdapter<String>(mContext, districts));
        mViewDistrict.setCurrentItem(0);

        mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[0];
        mCurrentZipCode = mZipcodeDatasMap.get(mCurrentDistrictName);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            showSelectedResult();
        }
    }

    private void showSelectedResult() {
        Toast.makeText(mContext, "当前选中:" + mCurrentProviceName + "," + mCurrentCityName + ","
                + mCurrentDistrictName + "," + mCurrentZipCode, Toast.LENGTH_SHORT).show();
    }

}
