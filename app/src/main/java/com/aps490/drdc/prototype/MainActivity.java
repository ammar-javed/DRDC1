package com.aps490.drdc.prototype;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.wikitude.architect.ArchitectView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String SDK_TRIAL = "z5QxOAr3U14zP2b3NNSoq/GEEyCaa05l1IMnoUWPJefCilXUHn+7Rpe9m/nDCM6E08tFc9nWca76nVjWxWVRNuWNKfMjjRTgeGR46iHWdiHigJ3Ot8/bkfXphSJMXkegb1eNMsAFVLHJJ8RgAtoKP04/YcIUZoxcua9cEkDcdedTYWx0ZWRfX2ZK2TvoOjdKSHxXMw0ESh5SkTeuuFeEfNUkVxxP/sM4zKUi+FNl3kUAyEc0Qb/ER3B8Y9NCw/IaqfcyZjwTcTEh/UMaX0oig6gJ95JhvZy7XAfowrItfo3dQ8nRxW0kMA5QjzRq8aQ7XugR9sY+LXZ8q8oRmIqG2XkglirPbpbLADMhOldWHFIjsg3nGSJnJjBoiK3xdNuPgG6Hfg5jXtiIeNJD+rSb4u2uojhNfBFPWeQBsKlb9GptI1hEJ80ezaDDoxoCqlsZYanNCT/swbpw/83LAWMA4GWPI5zoS9Ja1kT8Qi3Jxm4T0AEudmnwzDXD9v0prpDVKbr3HSSBcafKvO/ZnWc9gbvSibtr73uYtlcKz7u7V6arVwtZ9RF9AJp7gYqr6ULKb8B2XkPTa0wphmUmJBrUeveXPfeZ078n0CVVU17N+QZWkvaiVu1aU0fnGvLp43KmuiqV3dFZ5Vz/4q94WqHGW2V3SkwWHl6YMHhCEajYuBM=";
    protected ArchitectView architectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNativeLib(getApplicationContext());
        setContentView(R.layout.activity_main);

        this.setTitle("DRDC Prototype");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Commenting out until we want on-screen button(s)
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Architectview setup
        this.architectView = (ArchitectView) this.findViewById(R.id.architectView);
        this.architectView.onCreate( SDK_TRIAL );
    }

    public static void initNativeLib(Context context) {
        try {
            // Try loading our native lib, see if it works...
            System.loadLibrary("libarchitect");
        } catch (UnsatisfiedLinkError er) {
            ApplicationInfo appInfo = context.getApplicationInfo();
            String libName = "libarchitect.so";
            String destPath = context.getFilesDir().toString();
            try {
                String soName = destPath + File.separator + libName;
                new File(soName).delete();
                UnzipUtil.extractFile(appInfo.sourceDir, "lib/" + Build.CPU_ABI + "/" + libName, destPath);
                System.load(soName);
            } catch (IOException e) {
                // extractFile to app files dir did not work. Not enough space? Try elsewhere...
                destPath = context.getExternalCacheDir().toString();
                // Note: location on external memory is not secure, everyone can read/write it...
                // However we extract from a "secure" place (our apk) and instantly load it,
                // on each start of the app, this should make it safer.
                String soName = destPath + File.separator + libName;
                new File(soName).delete(); // this copy could be old, or altered by an attack
                try {
                    UnzipUtil.extractFile(appInfo.sourceDir, "lib/" + Build.CPU_ABI + "/" + libName, destPath);
                    System.load(soName);
                } catch (IOException e2) {
                    Log.e("AMMAR:", "Exception in InstallInfo.init(): " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPostCreate( Bundle savedInstanceState ) {
        super.onPostCreate(savedInstanceState);

        if ( this.architectView != null ) {

            this.architectView.onPostCreate();

            try {
                this.architectView.load("CR_MT/index.html");
            } catch (IOException e) {
                Log.e("AMMAR: onPostCreate:", "Loading index.html failed");
            }
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
