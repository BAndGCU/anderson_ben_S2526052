/*  Starter project for Mobile Platform Development - 1st diet 26/27
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required source and displays the
    raw data in a TextField
*/

//
// Name                 Ben Anderson
// Student ID           S2526052
// Programme of Study   BSc Software Development
//

// UPDATE THE PACKAGE NAME to include your Student Identifier
package com.example.anderson_ben_s2526052;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Base64;


public class MainActivity extends AppCompatActivity {

    //DOWNLOADING FROM TRAFFIC SCOTLAND

    // !!!!! vvv Removed before upload; insert own valid keys vvv !!!!!
    final String clientId = "INSERT YOUR ID HERE";
    final String clientKey = "INSERT YOUR KEY HERE"; // Removed before upload; insert own valid key

    // !!!!! ^^^ Removed before upload; insert own valid keys ^^^ !!!!!
    final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    final String roadworksURL = "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";

    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TrafficViewModel vm = new ViewModelProvider(this).get(TrafficViewModel.class);
        vm.init(clientId, clientKey, unplannedEventsURL, roadworksURL);

        twoPane = findViewById(R.id.detailContainer) != null;   // true only in landscape

        TextView statusText = findViewById(R.id.statusText);
        vm.status().observe(this, statusText::setText);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ListFragment()).commit();
        }

        findViewById(R.id.btnList).setOnClickListener(v ->
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new ListFragment()).commit());
        findViewById(R.id.btnSearch).setOnClickListener(v -> show(new SearchFragment(), false));
        findViewById(R.id.btnMap).setOnClickListener(v -> { vm.showAllOnMap(); showMap(); });

        vm.detailRequested().observe(this, requested -> {
            if (Boolean.TRUE.equals(requested)) {
                show(new DetailFragment(), twoPane);
                vm.detailShown();
            }
        });
    }

    private void show(Fragment f, boolean twoPane) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction()
                .replace(twoPane ? R.id.detailContainer : R.id.container, f);
        if (!twoPane) t.addToBackStack(null);
        t.commit();
    }

    public void showMap() { show(new MapFragment(), twoPane); }

}






