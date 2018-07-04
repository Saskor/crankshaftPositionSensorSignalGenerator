package com.example.ckpcamsensorsignalgenerator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.google.gson.Gson;
import java.util.ArrayList;

public class setCamSignalActivity extends AppCompatActivity {
    //GUI Elems variables
    private RadioButton camSignalTrueRadioButton;
    private RadioButton camSignalFalseRadioButton;
    private RadioButton firstTeethCamSignalTrueRadioButton;
    private RadioButton firstTeethCamSignalFalseRadioButton;
    private Button applyCAMSignalParams;
    //Set RecyclerView variables
    private RecyclerView recyclerView;
    private RecyclerView.Adapter listAdapter;
    private RecyclerView.LayoutManager llm;

    private ArrayList<String[]> fronts; //1 front number, 1 first rev crank, 10 front location tooth number, 025 FLTN addition
    private String camSignalExist;
    private String camSignalOnFirstTeethCrank;
    private String[] temp;
    private AlertDialog alert;
    private AlertDialog.Builder builder;

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mShareEditor;
    String jsonFronts;
    int flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_cam_signal);

        Context context = getApplicationContext();
        //Get SharedPreferences
        mSharedPreferences = getSharedPreferences("ckpSignalGenerator.SHARED_PREF", Context.MODE_PRIVATE);
        mShareEditor = mSharedPreferences.edit();
        //GUI Elems
        camSignalTrueRadioButton = (RadioButton)findViewById(R.id.camSignalTrueRadioButton);
        camSignalFalseRadioButton = (RadioButton)findViewById(R.id.camSignalFalseRadioButton);
        firstTeethCamSignalTrueRadioButton = (RadioButton)findViewById(R.id.firstTeethCamSignalTrueRadioButton);
        firstTeethCamSignalFalseRadioButton = (RadioButton)findViewById(R.id.firstTeethCamSignalFalseRadioButton);
        applyCAMSignalParams = (Button)findViewById(R.id.applyCAMSIgnalParams);
        recyclerView = (RecyclerView)findViewById(R.id.rv);
        //Get Values From SharedPreferences
        jsonFronts = mSharedPreferences.getString("jsonFronts","");
        ArrToIntent a = new Gson().fromJson(jsonFronts, ArrToIntent.class);
        if (a != null) {
            fronts = a.fronts;
        }

        if (fronts == null || fronts.size() == 0) {
            fronts = new ArrayList<String[]>();
            temp = new String[]{"", "", "", "00"};
            fronts.add(temp);
        }

        if (mSharedPreferences.getString("camSensorExist","").matches("0")) {
            camSignalFalseRadioButton.setChecked(true);
            camSignalTrueRadioButton.setChecked(false);
        }

        if (mSharedPreferences.getString("sigOnFirstCKPTooth","").matches("0")) {
            firstTeethCamSignalFalseRadioButton.setChecked(true);
            firstTeethCamSignalTrueRadioButton.setChecked(false);
        }

        //Set RecyclerView
        listAdapter = new MyListAdapter(fronts);
        recyclerView.setAdapter(listAdapter);
        llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        applyCAMSignalParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveActivityState();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home
            case android.R.id.home:
            saveActivityState();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private  void saveActivityState() {
        String frontNumber = "";
        String toothNumber = "";
        //Check Missed EditText Inputs in Recycler View
        for (int i = 0; i < fronts.size(); i++) {
            if(fronts.get(i)[0].matches("")) {
                if(frontNumber.matches("")) {
                    frontNumber += (i+1);
                } else {
                    frontNumber += ", " + (i+1);
                }
            }
            if(fronts.get(i)[2].matches("")) {
                if(toothNumber.matches("")) {
                    toothNumber += (i+1);
                } else {
                    toothNumber += ", " + (i+1);
                }
            }
        }

        if (!frontNumber.matches("") || !toothNumber.matches("")) {
            missingCamFrontsInput(frontNumber, toothNumber);
        } else {
            saveStateAndLaunchParentActivity();
        }
        //End Check
    }

    private void missingCamFrontsInput(String frontNumber, String toothNumber) {
        String alertString ="";
        if (!frontNumber.matches("")) {
            alertString = "Удалите строку № "+ frontNumber +" фронта сигнала ДПРВ, или укажите номер фронта.";
        }

        if (!toothNumber.matches("")) {
            if (!frontNumber.matches("")) {
                alertString += "\n\nУдалите строку № "+ toothNumber +" фронта сигнала ДПРВ, или укажите номер зуба задающего диска, на котором находится фронт сигнала ДПРВ.";

            } else {
                alertString += "Удалите строку № "+ toothNumber +" фронта сигнала ДПРВ, или укажите номер зуба задающего диска, на котором находится фронт сигнала ДПРВ.";
            }
        }
        alertString += "\n\nЖелаете заполнить текстовые поля или удалить строки?";

        builder = new AlertDialog.Builder(setCamSignalActivity.this);
        builder.setTitle("Внимание, незаполненные текстовые поля")
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

    private void saveStateAndLaunchParentActivity(){
        camSignalExist = HelperMethods.getBoolStringFromRBotton(camSignalTrueRadioButton);
        camSignalOnFirstTeethCrank = HelperMethods.getBoolStringFromRBotton(firstTeethCamSignalTrueRadioButton);
        jsonFronts = new Gson().toJson(new ArrToIntent(fronts));
        //Put Values Of GUI Elems To SharePref
        mShareEditor.putString("camSensorExist",camSignalExist);
        mShareEditor.putString("sigOnFirstCKPTooth",camSignalOnFirstTeethCrank);
        mShareEditor.putString("jsonFronts", jsonFronts);
        mShareEditor.apply();
        Intent configActivityIntent = new Intent(setCamSignalActivity.this, ConfigActivity.class);
        startActivity(configActivityIntent);
    }
}
