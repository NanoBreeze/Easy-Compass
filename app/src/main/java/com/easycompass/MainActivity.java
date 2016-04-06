package com.easycompass;

import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    SensorManager sensorManager;
    Sensor magnetometer;
    Sensor accelerometer;

    TextView tv_mag;
    TextView tv_acc;

    boolean magSet = false;
    boolean accSet = false;

    float[] geomagneticVector;      //with respect to device's coordinates, used by getRotationMatrix(...)
    float[] gravityVector;          //with respect to device's coordinates, used by getRotationmatrix(...)
    float[] rotationMatrix;         //matrix used to map one coordinate system (the Earth's) to another (the device's), used by getRotationMatrix(...) and getOrientation(...)
    float[] inclinationMatrix;      //matrix used because it is necessary in getRotationMatrix(...)

    float[] orientations;           //stores device's azimuth, pitch, and roll with respect to Earth's coordinate system, used by getOrientation(...)

    TextView tv_rotation;
    TextView tv_orientation;



    TextView tv_degree;

    ImageView needle;

    public MainActivity()
    {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        geomagneticVector = new float[3];
        gravityVector = new float[3];
        rotationMatrix = new float[9];
        inclinationMatrix = new float[9];
        orientations = new float[3];

        sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);


        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        tv_mag = (TextView) findViewById(R.id.tv_mag);
        tv_acc = (TextView) findViewById(R.id.tv_acc);

        tv_rotation = (TextView) findViewById(R.id.tv_rotation);
        tv_orientation = (TextView) findViewById(R.id.tv_orientation);

        tv_degree = (TextView) findViewById(R.id.tv_degree);
        needle = (ImageView) findViewById(R.id.imageView);

        Log.d("Inside", "OnCreate");
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
    Log.d(getClass().getSimpleName(), "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this, magnetometer);
        sensorManager.unregisterListener(this, accelerometer);

        Log.d(getClass().getSimpleName(), "OnPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(getClass().getSimpleName(), "OnStopped");
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == magnetometer) {
            geomagneticVector = event.values;

            float s0 = event.values[0];
            float s1 = event.values[1];
            float s2 = event.values[2];

            tv_mag.setText("0: " + Float.toString(s0) + ", 1: " + Float.toString(s1) + ", 2: " + Float.toString(s2));

            magSet = true;
        }

        if (event.sensor == accelerometer) {
            gravityVector = event.values;

            float s0 = event.values[0];
            float s1 = event.values[1];
            float s2 = event.values[2];

            tv_acc.setText("0: " + Float.toString(s0) + ", 1: " + Float.toString(s1) + ", 2: " + Float.toString(s2));

            accSet = true;
        }

        if (magSet && accSet) {
            sensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityVector, geomagneticVector);

            String r0 = Float.toString(rotationMatrix[0]);
            String r1 = Float.toString(rotationMatrix[1]);
            String r2 = Float.toString(rotationMatrix[2]);
            String r3 = Float.toString(rotationMatrix[3]);
            String r4 = Float.toString(rotationMatrix[4]);
            String r5 = Float.toString(rotationMatrix[5]);
            String r6 = Float.toString(rotationMatrix[6]);
            String r7 = Float.toString(rotationMatrix[7]);
            String r8 = Float.toString(rotationMatrix[8]);

            tv_rotation.setText("0: " + r0 + " ,1: " + r1 + " ,2: " + r2 + " ,3: " + r3 + " ,4: " + r4 + " ,5: " + r5 + " ,6: " + r6 + " ,7: " + r7 + " ,8: " + r8);

            sensorManager.getOrientation(rotationMatrix, orientations);

            String o0 = Float.toString(orientations[0]);
            String o1 = Float.toString(orientations[1]);
            String o2 = Float.toString(orientations[2]);

            tv_orientation.setText("0: " + o0 + " ,1: " + o1 + " ,2: " + o2);

            //change the radian of o0 to degrees. The radian ranges from -pi to pi.
            double degree = Math.toDegrees(orientations[0]);

            tv_degree.setText(Double.toString(degree));

            Matrix matrix = new Matrix();
            needle.setScaleType(ImageView.ScaleType.MATRIX);   //required
            matrix.postRotate((float)degree, needle.getDrawable().getBounds().width()/2, needle.getDrawable().getBounds().height()/2);
            needle.setImageMatrix(matrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
