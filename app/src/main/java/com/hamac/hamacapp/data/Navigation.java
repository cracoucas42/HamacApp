package com.hamac.hamacapp.data;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.hamac.hamacapp.R;
import com.hamac.hamacapp.activity.AboutActivity;
import com.hamac.hamacapp.activity.HelpActivity;
import com.hamac.hamacapp.activity.MapsActivity;
import com.hamac.hamacapp.activity.ProfilActivity;
import com.hamac.hamacapp.activity.RulesActivity;
import com.hamac.hamacapp.activity.SearchActivity;
import com.hamac.hamacapp.activity.SettingsActivity;

public class Navigation
{
    public Navigation() {
    }

    public Intent getIntentfromSelectedActivity(Context context, int itemId, boolean firstLaunch_flag)
    {
        Intent myIntent = new Intent();

        switch (itemId) {
            // action with ID show_map was selected
            case R.id.show_map:
                Toast.makeText(context, "Show Map selected", Toast.LENGTH_SHORT)
                        .show();
                myIntent = new Intent(context, MapsActivity.class);
                break;
            // action with ID action_search was selected
            case R.id.show_search:
                Toast.makeText(context,
                        "Button Search in ToolBar clicked",
                        Toast.LENGTH_LONG).show();
                myIntent = new Intent(context, SearchActivity.class);
                break;
            // action with ID show_profil was selected
            case R.id.show_profil:
                myIntent = new Intent(context, ProfilActivity.class);
                break;
            // action with ID action_settings was selected
            case R.id.action_settings:
                Toast.makeText(context, "Settings selected", Toast.LENGTH_SHORT)
                        .show();
                myIntent = new Intent(context, SettingsActivity.class);
                break;
            case R.id.show_rules:
                myIntent = new Intent(context, RulesActivity.class);
                break;
            case R.id.show_about:
                myIntent = new Intent(context, AboutActivity.class);
                break;
            default:
                break;
        }

        //Save ShareData through Activity to get it on next launch of activities
//        myIntent.putExtra("HAMAC_LIST", (Serializable) HamacList);
        myIntent.putExtra("FIRST_LAUNCH", firstLaunch_flag);
        //Can share another Bundle if necessary
        //myIntent.putExtra("lastName", "Your Last Name Here");
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return (myIntent);
    }
}
