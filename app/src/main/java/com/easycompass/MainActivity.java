package com.easycompass;

import android.content.Intent;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor magnetometer;
    Sensor accelerometer;


    boolean magSet = false;
    boolean accSet = false;

    float[] geomagneticVector;      //with respect to device's coordinates, used by getRotationMatrix(...)
    float[] gravityVector;          //with respect to device's coordinates, used by getRotationmatrix(...)
    float[] rotationMatrix;         //matrix used to map one coordinate system (the Earth's) to another (the device's), used by getRotationMatrix(...) and getOrientation(...)
    float[] inclinationMatrix;      //matrix used because it is necessary in getRotationMatrix(...), not used

    float[] orientations;           //stores device's azimuth, pitch, and roll with respect to Earth's coordinate system, used by getOrientation(...)


    TextView tv_adjustedDegree;
    ImageView needle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        geomagneticVector = new float[3];
        gravityVector = new float[3];
        rotationMatrix = new float[9];
        inclinationMatrix = new float[9];
        orientations = new float[3];

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        needle = (ImageView) findViewById(R.id.imageView);
        tv_adjustedDegree = (TextView) findViewById(R.id.tv_adjustedDegree);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(getClass().getSimpleName(), "OnStart");
    }

    /*
        When we press the homescreen or Square button, onPause and onStop happens. Therefore, unregister sensors when onPause() is called
        When we return, from home screen, onStart and onResume happens. Therefore, register sensors when onResume() is called
     */

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this, magnetometer);
        sensorManager.unregisterListener(this, accelerometer);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /* Since the Email Feedback menu item starts an intent that calls another app to compose an email, its ability to work relies on the existence of other apps.
    As a result, if the device, doesn't have an app that can compose emails, then it makes for a better user experience to remove the Email Feedback menu item.
    If the device does have an app that can compose emails, do display the Email Feedback menu item.
     */

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));

        if (intent.resolveActivity(getPackageManager()) == null) {
            menu.removeItem(R.id.emailMenuItem);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            //open About activity
            case R.id.aboutMenuItem:
                Intent aboutIntent = new Intent(this, About.class);
                startActivity(aboutIntent);
                return true;


            /*  The emailMenuItem would only be shown if the device has another app that can send email.
                If this is called, it is guaranteed that the user can compose emails
                However, we still include a check anyways
            */
            case R.id.emailMenuItem:

                //open another app to send email:
                String[] addresses = getResources().getStringArray(R.array.emailAddresses);

                Intent webIntent = new Intent(Intent.ACTION_SENDTO);
                webIntent.setData(Uri.parse("mailto:"));
                webIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
                webIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedbackSubjectLine));

                //technically, we don't need to check if the intent can start but double checking
                if (webIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(webIntent);
                } else {
                    throw new RuntimeException("In the if/else for resolving intent. This should not have occured");
                }

                return true;
            default:
                throw new RuntimeException("Inside default case of switch statement in onOptionsItemSelected. The id is not handled.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == magnetometer) {
            geomagneticVector = event.values;
            magSet = true;
        }

        if (event.sensor == accelerometer) {
            gravityVector = event.values;
            accSet = true;
        }

        if (magSet && accSet) {

            //changes rotationMatrix so that it would correctly map device's coordinates with Earth's
            sensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityVector, geomagneticVector);

            sensorManager.getOrientation(rotationMatrix, orientations);


            //change orientation[0], the device's pitch to be in degrees. Degree ranges from -180 to 180.
            double rawDegree = Math.toDegrees(orientations[0]);

//            tv_degree.setText(Double.toString(degree));

            //rotate the image representing the needle to the appropriate degree
            Matrix matrix = new Matrix();
            needle.setScaleType(ImageView.ScaleType.MATRIX);   //required
            matrix.postRotate((float) rawDegree, needle.getDrawable().getBounds().width() / 2, needle.getDrawable().getBounds().height() / 2);
            needle.setImageMatrix(matrix);

            /* degree
               0
           -90      90
            -179  179
              If the degree is negative, add 360 to it to make it the appropriate positive degree
             */

            int degree = (int) rawDegree;

            if (degree < 0) {
                degree += 360;
            }

            //show the degree
            tv_adjustedDegree.setText(getPoint(degree) + "," + String.valueOf(degree) + "°");

        }
    }

    //gets N, NE, E, etc. from the degree
    private String getPoint(int adjustedDegree) {
        // N (315-22.5), NE (22.5-67.5), E (67.5-112.5), SE (112.5-157.5), S(157.5-202.5), SW(202.5-247.5), W(247.5-292.5), NW(292.5-337.5)

        if (isBetween(adjustedDegree, 22.5, 67.5)) {
            return getResources().getString(R.string.northEast);
        }
        else if (isBetween(adjustedDegree, 67.5, 112.5)) {
            return getResources().getString(R.string.east);
        }
        else if (isBetween(adjustedDegree, 112.5, 157.5)) {
            return getResources().getString(R.string.southEast);
        }
        else if (isBetween(adjustedDegree, 157.5, 202.5)) {
            return getResources().getString(R.string.south);
        }
        else if (isBetween(adjustedDegree, 202.5, 247.5)) {
            return getResources().getString(R.string.southWest);
        }
        else if (isBetween(adjustedDegree, 247.5, 292.5)) {
            return getResources().getString(R.string.west);
        }
        else if (isBetween(adjustedDegree, 292.5, 337.5)) {
            return getResources().getString(R.string.northWest);
        }
        else {
            return getResources().getString(R.string.north);
        }
    }

    //checks if smaller < intToCheck <= larger
    private boolean isBetween(int intToCheck, double smaller, double larger) {
        if ((smaller < intToCheck) && (intToCheck <= larger)) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
