package com.eruntech.addresspicker.interfaces;

import com.eruntech.addresspicker.services.LoadAddressDataService;
import com.eruntech.addresspicker.valueobjects.Province;

import java.util.List;

/**
 * Difine some mothod must be implemented when listen {@link LoadAddressDataService}.
 * <P>时间：2015-09-11
 * <P>作者：Qin Yuanyi
 * <P>功能：定义监听LoadAddressDataService类必须实现的方法
 * @see LoadAddressDataService
 */
public interface OnAddressDataServiceListener {
    /**
     * call this method when the address xml resource is parsed.
     * <P>当储存在本地的中国地址数据被解析完毕时的回调函数
     * @param provinceList 解析好的中国地址数据
     */
    void onAddressDataGot(List<Province> provinceList);
}
