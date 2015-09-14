package com.eruntech.addresspicker.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
 * A Chinese Address Picker for Picking a location.
 * <p>时间：2015-09-11
 * <p>作者 Qin Yuanyi
 * <p>功能：中国省市区级联地址选择器
 */
public class ChineseAddressPicker extends LinearLayout
        implements View.OnClickListener, OnWheelChangedListener, OnAddressDataServiceListener {

    /** 上下文 **/
    private Context mContext;
    /** 通过构造函数传进的OnAddressPickerListener实例 **/
    private OnAddressPickerListener mOnAddressPickerListener;

    /** Log标签 **/
    private final String LOG_TAG = this.getClass().getSimpleName();

    /** 地址选择器默认可见项目数 **/
    private final int DEFAULT_VISIBLE_ITEM_COUNT = 5;

    //从 R.styleable.ChineseAddressPicker 读取
    /** 地址选择器实际可见项目数 **/
    private int mVisibleItemCount;
    /** 控件默认是否可见 **/
    private boolean mDefaultVisible;
    /** 是否显示过渡动画 **/
    private boolean mAnimationVisible = true;
    /** 是否显示动作条，即确认按钮 **/
    private boolean mActionBarVisible = true;

    /** 所有省 **/
    private String[] mProvinceDatas;
    /** key - 省 value - 市 **/
    private Map<String, String[]> mCitisDatasMap = new HashMap<>();
    /** key - 市 values - 区 **/
    private Map<String, String[]> mDistrictDatasMap = new HashMap<>();
    /** key - 区 values - 邮编**/
    private Map<String, String> mZipcodeDatasMap = new IdentityHashMap<>();

    /** 当前选中的省的名称 **/
    private String mCurrentProviceName;
    public String getProviceName() {
        return mCurrentProviceName;
    }
    /** 当前选中的市的名称 **/
    private String mCurrentCityName;
    public String getCityName() {
        return mCurrentCityName;
    }
    /** 当前选中的区的名称 **/
    private String mCurrentDistrictName;
    public String getDistrictName() {
        return mCurrentDistrictName;
    }
    /** 当前选中的区的邮政编码 **/
    private String mCurrentZipCode;

    //控件声明，setUpViews()方法
    /** 省份WheelView的引用 **/
    private WheelView mViewProvince;
    /** 城市WheelView的引用 **/
    private WheelView mViewCity;
    /** 区域WheelView的引用 **/
    private WheelView mViewDistrict;
    /** 确认按钮的引用 **/
    private Button mBtnConfirm;
    /** ActionBar的引用 **/
    private RelativeLayout mActionBar;

    public ChineseAddressPicker(Context context) {
        super(context);

        initPicker(context);

        requestAddressData();
    }

    public ChineseAddressPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPicker(context);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChineseAddressPicker, 0, 0);
        try {
            mVisibleItemCount = a.getInteger(R.styleable.ChineseAddressPicker_visibleItemCount, DEFAULT_VISIBLE_ITEM_COUNT);
            mVisibleItemCount = mVisibleItemCount >= 3 ? mVisibleItemCount : 3;

            mDefaultVisible = a.getBoolean(R.styleable.ChineseAddressPicker_defaultVisible, true);

            mAnimationVisible = a.getBoolean(R.styleable.ChineseAddressPicker_animationVisible, true);

            mActionBarVisible = a.getBoolean(R.styleable.ChineseAddressPicker_actionBarVisible, true);
        } finally {
            a.recycle();
        }
    }

    public ChineseAddressPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：初始化控件
     * @param context 上下文
     */
    private void initPicker(Context context) {
        mContext = context;
        if (context instanceof OnAddressPickerListener && mActionBarVisible) {
            mOnAddressPickerListener = (OnAddressPickerListener) context;
        } else {
            Log.w(LOG_TAG, "当显示ActionBar时，构造传入的Context类最好实现OnAddressPickerListener接口，不然无法响应选择地址事件");
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        requestAddressData();
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：发送异步请求开始解析储存在本地的中国地址数据库
     */
    private void requestAddressData() {
        LoadAddressDataService service = new LoadAddressDataService(this);
        service.startToParseData();
        // 异步方法，解析完地址数据后自动调用 onAddressDataGot() 方法
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：当储存在本地的中国地址数据被解析完毕时的回调函数
     * @param provinceList 解析好的中国地址数据
     */
    @Override
    public void onAddressDataGot(List<Province> provinceList) {
        LayoutInflater.from(getContext()).inflate(R.layout.chinese_address_picker, this);

        setUpViews();
        setUpListener();
        setUpAttributeSet();
        setUpData(provinceList);
    }

    private void setUpViews() {
        mViewProvince = (WheelView) findViewById(R.id.wheelview_province);
        mViewCity = (WheelView) findViewById(R.id.wheelview_city);
        mViewDistrict = (WheelView) findViewById(R.id.wheelview_district);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mActionBar = (RelativeLayout) findViewById(R.id.rl_action_bar);
    }

    private void setUpListener() {
        mViewProvince.addChangingListener(this);
        mViewCity.addChangingListener(this);
        mViewDistrict.addChangingListener(this);
        mBtnConfirm.setOnClickListener(this);
    }

    private void setUpAttributeSet() {
        if (mDefaultVisible) {
            show();
        } else {
            hide();
        }

        if (!mActionBarVisible) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mActionBar.getLayoutParams();
            layoutParams.height = 0;
            mActionBar.setLayoutParams(layoutParams);
        }
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：解析完地址数据处理解析好的数据并绑定到控件上
     */
    private void setUpData(List<Province> provinceList) {
        initParsedDatas(provinceList);

        mViewProvince.setViewAdapter(new ArrayWheelAdapter<>(mContext, mProvinceDatas));

        mViewProvince.setVisibleItems(mVisibleItemCount);
        mViewCity.setVisibleItems(mVisibleItemCount);
        mViewDistrict.setVisibleItems(mVisibleItemCount);

        updateCities();
        updateDistricts();
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：创建哈希表储存解析好的数据，加快读取速度
     */
    private void initParsedDatas(List<Province> provinceList) {
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

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：选择的地址发生改变时
     */
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
        mViewCity.setViewAdapter(new ArrayWheelAdapter<>(mContext, cities));
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
        mViewDistrict.setViewAdapter(new ArrayWheelAdapter<>(mContext, districts));
        mViewDistrict.setCurrentItem(0);

        mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[0];
        mCurrentZipCode = mZipcodeDatasMap.get(mCurrentDistrictName);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            showSelectedResult();
            hide();
            mOnAddressPickerListener.onAddressPicked();
        }
    }

    private void showSelectedResult() {
        Log.v(LOG_TAG, "当前选中：" + mCurrentProviceName + " - " + mCurrentCityName + " - "
                + mCurrentDistrictName + "，邮编：" + mCurrentZipCode);
    }

    public void show() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.getLayoutParams();
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        this.setLayoutParams(layoutParams);
    }

    public void hide() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.getLayoutParams();
        layoutParams.height = 0;
        this.setLayoutParams(layoutParams);
    }

    public interface OnAddressPickerListener {
        void onAddressPicked();
    }

}
