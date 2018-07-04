package com.example.ckpcamsensorsignalgenerator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;

public class setCKPSIgnalActivity extends AppCompatActivity {
    //GUI Elems
    private EditText allTeeth;
    private EditText teethMissing;
    private RadioButton increaseSignalRadioButton;
    private RadioButton decreaseSignalRadioButton;
    private Button applyCKPSignalParams;


    //60allTeeth,2teethMissing,1signalIncrease
    private String ckpSignalIncrease;

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mShareEditor;

    String allTeethText;
    String teethMissingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ckpsignal);
        //Get SharedPreferences
        mSharedPreferences = getSharedPreferences("ckpSignalGenerator.SHARED_PREF", Context.MODE_PRIVATE);
        mShareEditor = mSharedPreferences.edit();
        //GUI Elems
        allTeeth = (EditText)findViewById(R.id.allTeeth);
        teethMissing = (EditText)findViewById(R.id.teethMissing);
        increaseSignalRadioButton = (RadioButton)findViewById(R.id.increaseSignalRadioButton);
        decreaseSignalRadioButton = (RadioButton)findViewById(R.id.decreaseSignalRadioButton);
        applyCKPSignalParams = (Button)findViewById(R.id.applyCKPSignalParams);
        //Set GUI Elems Values
        allTeethText = mSharedPreferences.getString("allTeeth","");
        teethMissingText = mSharedPreferences.getString("teethMissing", "");
        ckpSignalIncrease = mSharedPreferences.getString("signalIncrease", "");

        if (allTeethText != null) {
            allTeeth.setText(allTeethText);
        }
        if (teethMissingText != null) {
            teethMissing.setText(teethMissingText);
        }
        if (ckpSignalIncrease.matches("0")) {
            increaseSignalRadioButton.setChecked(false);
            decreaseSignalRadioButton.setChecked(true);
        }

        applyCKPSignalParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveActivityState();
            }
        });
    }
    //Back Arrow In Action Bar ClickListener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                saveActivityState();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveActivityState() {
        if (HelperMethods.getText(allTeeth).matches("") || HelperMethods.getText(teethMissing).matches("")) {
            missingCKPInput();
        } else {
            saveStateAngLaunchParentActivity();
        }
    }

    private void missingCKPInput() {
        allTeethText = HelperMethods.getText(allTeeth);
        teethMissingText = HelperMethods.getText(teethMissing);
        String alertString ="";
        AlertDialog alert;
        AlertDialog.Builder builder;
        if (allTeethText.matches("") || teethMissingText.matches("")) {

            if (allTeethText.matches("")) {
                alertString = "Вы не заполнили поле \"Всего зубов\" в пункте \"Формула задающего диска\"";
            }

            if (teethMissingText.matches("")) {
                if (!alertString.matches("")) {
                    alertString += "\n\nВы не заполнили поле \"Пропуск зубов\" в пункте \"Формула задающего диска\"";
                } else {
                    alertString += "Вы не заполнили поле \"Пропуск зубов\" в пункте \"Формула задающего диска\"";
                }
            }
            alertString += "\n\nЖелаете заполнить текстовые поля?";

            builder = new AlertDialog.Builder(setCKPSIgnalActivity.this);
            builder.setTitle("Внимание, незаполненные текстовые поля.")
                    .setMessage(alertString)
                    .setCancelable(false)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int flag) {
                        }
                    })
                    .setNegativeButton("Нет",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    saveStateAngLaunchParentActivity();
                                }
                            });
            alert = builder.create();
            alert.show();
        }
    }

    private void saveStateAngLaunchParentActivity(){
        ckpSignalIncrease = HelperMethods.getBoolStringFromRBotton(increaseSignalRadioButton);
        //Put Values Of GUI Elems to SharedPrefs
        mShareEditor.putString("allTeeth",HelperMethods.getText(allTeeth));
        mShareEditor.putString("teethMissing",HelperMethods.getText(teethMissing));
        mShareEditor.putString("signalIncrease", ckpSignalIncrease);
        mShareEditor.apply();
        Intent configActivityIntent = new Intent(setCKPSIgnalActivity.this, ConfigActivity.class);
        startActivity(configActivityIntent);
    }
}
