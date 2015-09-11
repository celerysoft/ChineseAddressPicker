package com.eruntech.addresspicker.interfaces;

import com.eruntech.addresspicker.services.LoadAddressDataService;
import com.eruntech.addresspicker.valueobjects.Province;

import java.util.List;

/**
 * Difine some mothod must be implemented when listen {@link LoadAddressDataService}.
 * @see LoadAddressDataService
 * @author Qin Yuanyi
 * 时间：2015-09-09
 * 功能：定义监听LoadAddressDataService类必须实现的方法
 */
public interface OnAddressDataServiceListener {
    /**
     * call this method when the address xml resource is parsed.
     */
    void onAddressDataGot(List<Province> provinceList);
}
