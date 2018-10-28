package com.hamac.hamacapp.activity;

import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import com.hamac.hamacapp.R;


public class ParameterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton hamacMap = (FloatingActionButton) findViewById(R.id.hamacMap);
//        hamacMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        //.setAction("Action", null).show();
//                Intent intent = new Intent(ParameterActivity.this, MapsActivity.class); //MainActivity.this replace getActivity() due to Fragment
//                startActivity(intent);
//            }
//        });
    }

}
