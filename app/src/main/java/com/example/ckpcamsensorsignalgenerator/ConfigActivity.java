package com.example.ckpcamsensorsignalgenerator;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ConfigActivity extends AppCompatActivity {
    private Button setCPKsignal;
    private Button setCAMsignal;
    private Button launchTest;

    // ckpSignalHashMap 60allTeeth,2teethMissing,1signalIncrease
    // camSignalHashMap; 1camSensorExist,1sigOnFirstCKPTooth
    // private ArrayList<String[]> fronts; 1 front number, 1 first rev crank, 10 front location tooth number, 025 FLTN addition
    // private String signalParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        //GUI Elems
        setCPKsignal = (Button)findViewById(R.id.setCKPSignalParamsButton);
        setCAMsignal = (Button)findViewById(R.id.setCAMSignalParamsButton);
        launchTest = (Button)findViewById(R.id.launchTest);
        //Buttons ClickListeners
        setCPKsignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ckpIntent = new Intent(ConfigActivity.this, setCKPSIgnalActivity.class);
                startActivity(ckpIntent);
            }
        });

        setCAMsignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camIntent = new Intent(ConfigActivity.this, setCamSignalActivity.class);
                startActivity(camIntent);
            }
        });


        launchTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent testIntent = new Intent(ConfigActivity.this, testActivity.class);
                startActivity(testIntent);
            }
        });
    }
    //Action Bar Back Arrow ClickListener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home buttonArrToIntent a = (ArrToIntent) data.getParcelableExtra("ArrToIntent");
            //            fronts = a.fronts;
            case android.R.id.home:
                setActivityResult();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private  void setActivityResult() {
        Intent mainIntent = new Intent(ConfigActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

}
