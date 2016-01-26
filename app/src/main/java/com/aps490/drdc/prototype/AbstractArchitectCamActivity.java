package com.aps490.drdc.prototype;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.IOException;


import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectConfig;
import com.wikitude.architect.ArchitectView.ArchitectUrlListener;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;

/**
 * Abstract activity which handles live-cycle events.
 * Feel free to extend from this activity when setting up your own AR-Activity
 *
 */
public abstract class AbstractArchitectCamActivity extends AppCompatActivity implements ArchitectViewHolderInterface{

    /**
     * holds the Wikitude SDK AR-View, this is where camera, markers, compass, 3D models etc. are rendered
     */
    protected ArchitectView					architectView;

    /**
     * sensor accuracy listener in case you want to display calibration hints
     */
    protected SensorAccuracyChangeListener	sensorAccuracyListener;

    /**
     * last known location of the user, used internally for content-loading after user location was fetched
     */
    protected Location lastKnownLocaton;

    /**
     * sample location strategy, you may implement a more sophisticated approach too
     */
    protected ILocationProvider				locationProvider;

    /**
     * location listener receives location updates and must forward them to the architectView
     */
    protected LocationListener locationListener;

    /**
     * urlListener handling "document.location= 'architectsdk://...' " calls in JavaScript"
     */
    protected ArchitectUrlListener 			urlListener;

    /**
     * title shown in activity
     * @return
     */
    public abstract String getActivityTitle();

    /**
     * path to the architect-file (AR-Experience HTML) to launch
     * @return
     */
    @Override
    public abstract String getARchitectWorldPath();

    /**
     * url listener fired once e.g. 'document.location = "architectsdk://foo?bar=123"' is called in JS
     * @return
     */
    @Override
    public abstract ArchitectUrlListener getUrlListener();

    /**
     * @return layout id of your layout.xml that holds an ARchitect View, e.g. R.layout.camview
     */
    @Override
    public abstract int getContentViewId();

    /**
     * @return Wikitude SDK license key, checkout www.wikitude.com for details
     */
    @Override
    public abstract String getWikitudeSDKLicenseKey();

    /**
     * @return layout-id of architectView, e.g. R.id.architectView
     */
    @Override
    public abstract int getArchitectViewId();

//    /**
//     *
//     * @return Implementation of a Location
//     */
//    @Override
//    public abstract ILocationProvider getLocationProvider(final LocationListener locationListener);

    /**
     * @return Implementation of Sensor-Accuracy-Listener. That way you can e.g. show prompt to calibrate compass
     */
    @Override
    public abstract ArchitectView.SensorAccuracyChangeListener getSensorAccuracyListener();

    /**
     * helper to check if video-drawables are supported by this device. recommended to check before launching ARchitect Worlds with videodrawables
     * @return true if AR.VideoDrawables are supported, false if fallback rendering would apply (= show video fullscreen)
     */
    public static final boolean isVideoDrawablesSupported() {
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        return extensions != null && extensions.contains( "GL_OES_EGL_image_external" ) && android.os.Build.VERSION.SDK_INT >= 14 ;
    }

    @Override
    public abstract float getInitialCullingDistanceMeters();


}