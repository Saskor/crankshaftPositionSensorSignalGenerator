package com.example.ckpcamsensorsignalgenerator;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class HelperMethods {
    public static void showToast(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0 ,0);
        toast.show();
    }

    public static void showToastLong(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0 ,0);
        toast.show();
    }

    public static String getText(EditText editText) {
        String  getTextString = editText.getText().toString();
        return getTextString;
    }

    public static String getBoolStringFromRBotton(RadioButton rb) {
        if (rb.isChecked()) {
            return "1";
        } else {
            return "0";
        }
    }

    public static String getBoolStringFromCheckBox(CheckBox ch) {
        if (ch.isChecked()) {
            return "1";
        } else {
            return "0";
        }
    }
}
