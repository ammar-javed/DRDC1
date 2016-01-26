package com.aps490.drdc.prototype;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
import com.wikitude.architect.ArchitectView.ArchitectUrlListener;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AbstractArchitectCamActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Wikitude Key
     */
    private final String SDK_TRIAL = "z5QxOAr3U14zP2b3NNSoq/GEEyCaa05l1IMnoUWPJefCilXUHn+7Rpe9m/nDCM6E08tFc9nWca76nVjWxWVRNuWNKfMjjRTgeGR46iHWdiHigJ3Ot8/bkfXphSJMXkegb1eNMsAFVLHJJ8RgAtoKP04/YcIUZoxcua9cEkDcdedTYWx0ZWRfX2ZK2TvoOjdKSHxXMw0ESh5SkTeuuFeEfNUkVxxP/sM4zKUi+FNl3kUAyEc0Qb/ER3B8Y9NCw/IaqfcyZjwTcTEh/UMaX0oig6gJ95JhvZy7XAfowrItfo3dQ8nRxW0kMA5QjzRq8aQ7XugR9sY+LXZ8q8oRmIqG2XkglirPbpbLADMhOldWHFIjsg3nGSJnJjBoiK3xdNuPgG6Hfg5jXtiIeNJD+rSb4u2uojhNfBFPWeQBsKlb9GptI1hEJ80ezaDDoxoCqlsZYanNCT/swbpw/83LAWMA4GWPI5zoS9Ja1kT8Qi3Jxm4T0AEudmnwzDXD9v0prpDVKbr3HSSBcafKvO/ZnWc9gbvSibtr73uYtlcKz7u7V6arVwtZ9RF9AJp7gYqr6ULKb8B2XkPTa0wphmUmJBrUeveXPfeZ078n0CVVU17N+QZWkvaiVu1aU0fnGvLp43KmuiqV3dFZ5Vz/4q94WqHGW2V3SkwWHl6YMHhCEajYuBM=";

    /**
     * holds the Wikitude SDK AR-View, this is where camera, markers, compass, 3D models etc. are rendered
     */
    protected ArchitectView architectView;

    /**
     * last time the calibration toast was shown, this avoids too many toast shown when compass needs calibration
     */
    private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();

    /**
     * extras key for architect-url to load, usually already known upfront, can be relative folder to assets (myWorld.html --> assets/myWorld.html is loaded) or web-url ("http://myserver.com/myWorld.html"). Note that argument passing is only possible via web-url
     */
    protected static final String EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL = "activityArchitectWorldUrl";

    /**
     * sensor accuracy listener in case you want to display calibration hints
     */
    protected SensorAccuracyChangeListener	sensorAccuracyListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

		/* pressing volume up/down should cause music volume changes */
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        initNativeLib(getApplicationContext());
        setContentView(R.layout.activity_main);

        this.setTitle(getActivityTitle());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         //Commenting out until we want on-screen button(s)
         FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
         fab.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();
        }
        });
         */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        try {
            // Architectview setup
            this.architectView = (ArchitectView) this.findViewById(getArchitectViewId());
            this.architectView.onCreate(getWikitudeSDKLicenseKey());
        }  catch (RuntimeException rex) {
            this.architectView = null;
            Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
            Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
        }


        // set accuracy listener if implemented, you may e.g. show calibration prompt for compass using this listener
        this.sensorAccuracyListener = this.getSensorAccuracyListener();

        // set urlListener, any calls made in JS like "document.location = 'architectsdk://foo?bar=123'" is forwarded to this listener, use this to interact between JS and native Android activity/fragment
        this.urlListener = this.getUrlListener();

        // register valid urlListener in architectView, ensure this is set before content is loaded to not miss any event
        if (this.urlListener != null && this.architectView != null) {
            this.architectView.registerUrlListener(this.getUrlListener());

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (this.architectView != null) {

            this.architectView.onPostCreate();

            try {
                this.architectView.load( getARchitectWorldPath() );

                if (this.getInitialCullingDistanceMeters() != ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS) {
                    // set the culling distance - meaning: the maximum distance to render geo-content
                    this.architectView.setCullingDistance( this.getInitialCullingDistanceMeters() );
                }

            } catch (IOException e) {
                Log.e("AMMAR: onPostCreate:", "Loading index.html failed");
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // call mandatory live-cycle method of architectView
        if ( this.architectView != null ) {
            this.architectView.onResume();

            // register accuracy listener in architectView, if set
            if (this.sensorAccuracyListener!=null) {
                this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // call mandatory live-cycle method of architectView
        if ( this.architectView != null ) {
            this.architectView.onPause();

            // unregister accuracy listener in architectView, if set
            if ( this.sensorAccuracyListener != null ) {
                this.architectView.unregisterSensorAccuracyChangeListener(this.sensorAccuracyListener);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // call mandatory live-cycle method of architectView
        if ( this.architectView != null ) {
            this.architectView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if ( this.architectView != null ) {
            this.architectView.onLowMemory();
        }
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

    @Override
    public String getWikitudeSDKLicenseKey() {
        return this.SDK_TRIAL;
    }

    @Override
    public SensorAccuracyChangeListener getSensorAccuracyListener() {
        return new SensorAccuracyChangeListener() {
            @Override
            public void onCompassAccuracyChanged(int accuracy) {
                /* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3 */
                if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && MainActivity.this != null && !MainActivity.this.isFinishing() && System.currentTimeMillis() - MainActivity.this.lastCalibrationToastShownTimeMillis > 5 * 1000) {
                    Toast.makeText(MainActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG).show();
                    MainActivity.this.lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
                }
            }
        };
    }

    @Override
    public float getInitialCullingDistanceMeters() {
        // you need to adjust this in case your POIs are more than 50km away from user here while loading or in JS code (compare 'AR.context.scene.cullingDistance')
        return ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS;
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public int getArchitectViewId() {
        return R.id.architectView;
    }

    @Override
    public String getActivityTitle() {
        return "Prototype";
    }

    @Override
    public String getARchitectWorldPath() {
        return "CR_MT/index.html";
    }

    @Override
    public ArchitectUrlListener getUrlListener() {
        return new ArchitectUrlListener() {

            @Override
            public boolean urlWasInvoked(String uriString) {
                // by default: no action applied when url was invoked
                return false;
            }
        };
    }


}
