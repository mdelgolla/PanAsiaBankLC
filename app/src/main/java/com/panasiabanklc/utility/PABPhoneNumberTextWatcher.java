package com.panasiabanklc.utility;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by user1 on 9/6/2017.
 */

    public class PABPhoneNumberTextWatcher implements TextWatcher {
    int mPreviousLen;
    boolean keyDel;
    private EditText et;
    public PABPhoneNumberTextWatcher(EditText et)
    {
        this.et = et;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        mPreviousLen = s.length();

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {
        // TODO: Do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        keyDel = mPreviousLen > s.length();
        et.setError(null);
        if(!keyDel){
            if (!(et.getText().toString().startsWith("0")) ){
                et.setText("");
                et.setError("Start Mobile Number With '0'");
            }
        }
    }
}