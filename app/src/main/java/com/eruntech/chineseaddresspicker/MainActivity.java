package com.eruntech.chineseaddresspicker;

import android.app.Activity;
import android.os.Bundle;

import com.eruntech.addresspicker.widgets.ChineseAddressPicker;


/**
 *
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ChineseAddressPicker picker = (ChineseAddressPicker) findViewById(R.id.main_picker);
    }
}
