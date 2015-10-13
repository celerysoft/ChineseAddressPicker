package com.eruntech.addresspicker.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.eruntech.addresspicker.R;
import com.eruntech.addresspicker.interfaces.OnAddressDataServiceListener;
import com.eruntech.addresspicker.services.LoadXmlAddressDataService;
import com.eruntech.addresspicker.services.LoadJsonAddressDataService;

import java.util.HashMap;
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

    /** Log标签 **/
    private final String LOG_TAG = this.getClass().getSimpleName();
    /** 选择地址的时候没有选中具体地址时的字符串 **/
    private final String NO_ADDRESS_PICKED = "-----";
    /** 地址选择器默认可见项目数 **/
    private final int DEFAULT_VISIBLE_ITEM_COUNT = 5;
    /** 地址选择器最小可见项目数 **/
    private final int MIN_VISIBLE_ITEM_COUNT = 3;


    //从 R.styleable.ChineseAddressPicker 读取
    /** 地址选择器实际可见项目数 **/
    private int mVisibleItemCount;
    /** 控件默认是否可见 **/
    private boolean mDefaultVisible;
    /** 是否显示过渡动画 **/
    private boolean mAnimationVisible = true;
    /** 是否显示动作条，即确认按钮 **/
    private boolean mActionBarVisible = true;
    /** 是否按读音排序，否则按区域代号排序，按读音排序必须XML数据 **/
    private boolean mIsSortedByPronunciation = false;
    /** 是否使用JSON数据，否则使用XML数据，JSON数据更新更全面，但是无法按照读音排序，默认使用JSON数据 **/
    private boolean mIsJsonDataEnable = true;

    /** 包含所有省的名称 **/
    private String[] mProvinceDatas;
    /** key - 省 value - 市 **/
    private Map<String, String[]> mCityDataMap = new HashMap<>();
    /** key - 市 values - 区 **/
    private Map<String, String[]> mDistrictDataMap = new HashMap<>();

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
        if (mCurrentDistrictName.equals(NO_ADDRESS_PICKED)) {
            return null;
        } else {
            return mCurrentDistrictName;
        }
    }
    /** 监听器 **/
    private OnAddressPickerListener mOnAddressPickerListener;
    public void setOnAddressPickerListener(OnAddressPickerListener listener) {
        mOnAddressPickerListener = listener;
    }

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

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChineseAddressPicker, 0, 0);
        try {
            mVisibleItemCount = a.getInteger(R.styleable.ChineseAddressPicker_visibleItemCount, DEFAULT_VISIBLE_ITEM_COUNT);
            mVisibleItemCount = mVisibleItemCount >= MIN_VISIBLE_ITEM_COUNT ? mVisibleItemCount : MIN_VISIBLE_ITEM_COUNT;

            mDefaultVisible = a.getBoolean(R.styleable.ChineseAddressPicker_defaultVisible, true);

            mAnimationVisible = a.getBoolean(R.styleable.ChineseAddressPicker_animationVisible, true);

            mActionBarVisible = a.getBoolean(R.styleable.ChineseAddressPicker_actionBarVisible, true);

            mIsSortedByPronunciation = a.getBoolean(R.styleable.ChineseAddressPicker_sortByPronunciation, false);

            mIsJsonDataEnable = a.getBoolean(R.styleable.ChineseAddressPicker_jsonDataEnable, true);

        } finally {
            a.recycle();
        }

        initPicker(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        requestAddressData();
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：初始化控件
     * @param context 上下文
     */
    private void initPicker(Context context) {
        mContext = context;
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：发送异步请求开始解析储存在本地的中国地址数据库
     */
    private void requestAddressData() {
        if (!mIsJsonDataEnable) {
            LoadXmlAddressDataService service = new LoadXmlAddressDataService(this);
            service.startToParseData(mIsSortedByPronunciation);
        } else {
            LoadJsonAddressDataService jsonService = new LoadJsonAddressDataService(this);
            jsonService.startToParseData();
        }
        // startToParseData() 为异步方法，解析完地址数据后自动调用 onAddressDataGot() 方法
    }

    /**
     * <P>修改时间：2015-09-18
     * <P>作者：Qin Yuanyi
     * <P>功能描述：当储存在本地的中国地址数据被解析完毕时的回调函数
     * @param provinceData 包含所有省名字的字符串数组
     * @param cityDataMap key - 省， value - 市
     * @param districtDatasMap key - 市， value - 区
     */
    @Override
    public void onAddressDataGot(String[] provinceData, Map<String, String[]> cityDataMap, Map<String, String[]> districtDatasMap) {
        LayoutInflater.from(getContext()).inflate(R.layout.chinese_address_picker, this);

        mProvinceDatas = provinceData;
        mCityDataMap = cityDataMap;
        mDistrictDataMap = districtDatasMap;

        setUpViews();
        setUpListener();
        setUpAttributeSet();
        setUpData();
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：定义控件
     */
    private void setUpViews() {
        mViewProvince = (WheelView) findViewById(R.id.wheelview_province);
        mViewCity = (WheelView) findViewById(R.id.wheelview_city);
        mViewDistrict = (WheelView) findViewById(R.id.wheelview_district);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mActionBar = (RelativeLayout) findViewById(R.id.rl_action_bar);
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：定义监听器
     */
    private void setUpListener() {
        mViewProvince.addChangingListener(this);
        mViewCity.addChangingListener(this);
        mViewDistrict.addChangingListener(this);
        mBtnConfirm.setOnClickListener(this);
    }

    /**
     * <P>修改时间：2015-09-14
     * <P>作者：Qin Yuanyi
     * <P>功能描述：根据属性表的设置来对控件进行初始化
     */
    private void setUpAttributeSet() {
        if (!mDefaultVisible) {
            hideWidget();
        }

        if (!mActionBarVisible) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mActionBar.getLayoutParams();
            layoutParams.height = 0;
            mActionBar.setLayoutParams(layoutParams);
        }
    }

    /**
     * <P>修改时间：2015-09-18
     * <P>作者：Qin Yuanyi
     * <P>功能描述：解析完地址数据处理解析好的数据并绑定到控件上
     */
    private void setUpData() {

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
        if (mOnAddressPickerListener != null) {
            mOnAddressPickerListener.onAddressChanged();
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
        mCurrentDistrictName = mDistrictDataMap.get(mCurrentCityName)[currentDistrictIndex];
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：根据当前的省，更新市WheelView的信息
     */
    private void updateCities() {
        int currentItemIndex = mViewProvince.getCurrentItem();
        mCurrentProviceName = mProvinceDatas[currentItemIndex];
        String[] cities = mCityDataMap.get(mCurrentProviceName);
        if (cities == null) {
            cities = new String[] { "数据缺失" };
            Log.w(LOG_TAG, mCurrentProviceName + "下的城市信息缺失。");
        }
        mViewCity.setViewAdapter(new ArrayWheelAdapter<>(mContext, cities));
        mViewCity.setCurrentItem(0);
        updateDistricts();
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：根据当前的市，更新区WheelView的信息
     */
    private void updateDistricts() {
        int currentCityIndex = mViewCity.getCurrentItem();
        mCurrentCityName = mCityDataMap.get(mCurrentProviceName)[currentCityIndex];
        String[] districts = mDistrictDataMap.get(mCurrentCityName);

        if (districts == null) {
            districts = new String[] { "数据缺失" };
            Log.w(LOG_TAG, mCurrentCityName + "下的区域信息缺失。");
        }
        mViewDistrict.setViewAdapter(new ArrayWheelAdapter<>(mContext, districts));
        mViewDistrict.setCurrentItem(0);

        mCurrentDistrictName = mDistrictDataMap.get(mCurrentCityName)[0];
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：Click事件的回调函数
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            Log.v(LOG_TAG, "当前选中：" + mCurrentProviceName + " - " + mCurrentCityName + " - "
                    + mCurrentDistrictName);
            hide();
            if (mOnAddressPickerListener != null) {
                mOnAddressPickerListener.onAddressPicked();
            }

        }
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：显示控件
     */
    public void show() {
        if (mAnimationVisible) {
            showWidgetWithAnimation();
        } else {
            showWidget();
        }
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：显示控件(无动画)
     */
    private void showWidget() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.getLayoutParams();
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        this.setLayoutParams(layoutParams);
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：显示控件（包含过渡动画）
     */
    private void showWidgetWithAnimation() {
        //从屏幕底端之外移动到指定区域
        float fromY = mContext.getResources().getDisplayMetrics().heightPixels + this.getHeight();
        float toY = 0;

        ChineseAddressPickerAnimationListener animationListener = new ChineseAddressPickerAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                showWidget();
            }
        };

        TranslateAnimation animation = new TranslateAnimation(0, 0, fromY, toY);
        animation.setDuration(mContext.getResources().getInteger(android.R.integer.config_longAnimTime));
        animation.setFillBefore(false);
        animation.setAnimationListener(animationListener);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(animation);

        this.startAnimation(animationSet);
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：隐藏控件
     */
    public void hide() {
        if (mAnimationVisible) {
            hideWidgetWithAnimation();
        } else {
            hideWidget();
        }
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：隐藏控件(无动画)
     */
    private void hideWidget() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.getLayoutParams();
        layoutParams.height = 0;
        this.setLayoutParams(layoutParams);
    }

    /**
     * <P>修改时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能描述：隐藏控件（包含过渡动画）
     */
    private void hideWidgetWithAnimation() {
        //从原位置移动到屏幕底端之外
        float fromY = 0;
        float toY = mContext.getResources().getDisplayMetrics().heightPixels + this.getHeight();

        ChineseAddressPickerAnimationListener animationListener = new ChineseAddressPickerAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                hideWidget();
            }
        };

        TranslateAnimation animation = new TranslateAnimation(0, 0, fromY, toY);
        animation.setDuration(mContext.getResources().getInteger(android.R.integer.config_longAnimTime));
        animation.setFillBefore(false);
        animation.setAnimationListener(animationListener);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(animation);

        this.startAnimation(animationSet);
    }

    /**
     * <P>时间：2015-09-14
     * <P>作者：Qin Yuanyi
     * <P>功能：实现动画监听接口的抽象类
     */
    private abstract class ChineseAddressPickerAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {}

        @Override
        public void onAnimationRepeat(Animation animation) {}
    }

    /**
     * Difine some mothod must be implemented when listen {@link ChineseAddressPicker}.
     * <P>时间：2015-09-11
     * <P>作者：Qin Yuanyi
     * <P>功能：定义监听ChineseAddressPicker类必须实现的方法
     */
    public interface OnAddressPickerListener {
        /**
         * <P>修改时间：2015-09-11
         * <P>作者：Qin Yuanyi
         * <P>功能描述：当地址被选中，并按下动作条的确定按钮之后的回调函数
         */
        void onAddressPicked();
        /**
         * <P>修改时间：2015-09-17
         * <P>作者：Qin Yuanyi
         * <P>功能描述：当选择的地址发生改变之后的回调函数
         */
        void onAddressChanged();
    }

}
