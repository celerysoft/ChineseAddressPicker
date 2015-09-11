package com.eruntech.chineseaddresspicker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.eruntech.addresspicker.widgets.ChineseAddressPicker;


/**
 *
 */
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
        mButton.setText("请选择地址");
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
        String address = mPicker.getProviceName() + " - "
                + mPicker.getCityName() + " - "
                + mPicker.getDistrictName();
        mButton.setText(address);
    }
}
