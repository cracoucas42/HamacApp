package com.hamac.hamacapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hamac.hamacapp.data.Navigation;
import com.hamac.hamacapp.R;

public class RulesActivity extends AppCompatActivity {

    private boolean firstLaunch_flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        //Get ShareData
        Intent myIntent = new Intent();
        myIntent = RulesActivity.this.getIntent();
        firstLaunch_flag = myIntent.getBooleanExtra("FIRST_LAUNCH", true);

        //Start Manage Toolbar > how to share this code into several Activities
        Toolbar hamacToolBar = findViewById(R.id.hamac_toolbar);
        RulesActivity.this.setSupportActionBar(hamacToolBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        /*MenuItem item = menu.findItem(R.id.button_item);
        Button btn = item.getActionView().findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity.this, "Toolbar Button Clicked!", Toast.LENGTH_SHORT).show();
            }
        });*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Navigation nav = new Navigation();
        Intent myIntent = nav.getIntentfromSelectedActivity(RulesActivity.this, item.getItemId(), firstLaunch_flag);

        //Can share another Bundle if necessary
        //myIntent.putExtra("lastName", "Your Last Name Here");
        myIntent.putExtra("FIRST_LAUNCH", firstLaunch_flag);
//        if (currentActivity)
        startActivity(myIntent);

        return true;
    }
}
