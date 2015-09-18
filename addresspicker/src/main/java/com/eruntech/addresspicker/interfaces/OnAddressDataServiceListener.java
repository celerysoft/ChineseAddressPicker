package com.eruntech.addresspicker.interfaces;

import com.eruntech.addresspicker.services.LoadXmlAddressDataService;

import java.util.Map;

/**
 * Difine some mothod must be implemented when listen {@link LoadXmlAddressDataService}.
 * <P>时间：2015-09-18
 * <P>作者：Qin Yuanyi
 * <P>功能：定义监听LoadAddressDataService类必须实现的方法
 * @see LoadXmlAddressDataService
 */
public interface OnAddressDataServiceListener {
    /**
     * call this method when the address data is parsed.
     * <P>当储存在本地的中国地址数据被解析完毕时的回调函数
     * @param provinceData 包含所有省名字的字符串数组
     * @param cityDataMap key - 省， value - 市
     * @param districtDatasMap key - 市， value - 区
     */
    void onAddressDataGot(String[] provinceData, Map<String, String[]> cityDataMap, Map<String, String[]> districtDatasMap);
}
