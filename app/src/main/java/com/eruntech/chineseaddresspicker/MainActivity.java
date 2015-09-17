package com.eruntech.chineseaddresspicker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.eruntech.addresspicker.widgets.ChineseAddressPicker;


public class MainActivity extends Activity implements ChineseAddressPicker.OnAddressPickerListener {

    ChineseAddressPicker mPicker;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupViews();
        setupListener();
    }

    private void setupViews() {
        mPicker = (ChineseAddressPicker) findViewById(R.id.main_picker);

        mButton = (Button) findViewById(R.id.main_btn);
        mButton.setText(getString(R.string.btn_main_text));
    }

    private void setupListener() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == mButton.getId()) {
                    mPicker.show();
                }
            }
        };

        mButton.setOnClickListener(onClickListener);
    }

    @Override
    public void onAddressPicked() {
        String address = null;
        if (mPicker.getProviceName() != null) {
            address = mPicker.getProviceName();
            if (mPicker.getCityName() != null) {
                address += " - " + mPicker.getCityName();
                if (mPicker.getDistrictName() != null) {
                    address += " - " + mPicker.getDistrictName();
                }
            }
        }
        mButton.setText(address);
    }

    @Override
    public void onAddressChanged() {
        String address = null;
        if (mPicker.getProviceName() != null) {
            address = mPicker.getProviceName();
            if (mPicker.getCityName() != null) {
                address += " - " + mPicker.getCityName();
                if (mPicker.getDistrictName() != null) {
                    address += " - " + mPicker.getDistrictName();
                }
            }
        }
        mButton.setText(address);
    }
}
