package com.easycompass;

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
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    SensorManager sMgr;
    Sensor mag;
    Sensor acc;

    TextView tv_mag;
    TextView tv_acc;

    boolean magSet = false;
    boolean accSet = false;

    float[] magValues = {0,0,0};
    float[] accValues = {0,0,0};

    TextView tv_rotation;
    TextView tv_orientation;

    float[] r = new float[9];
    float[] i = new float[9];

    float[] oValues = new float[3];

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

        sMgr = (SensorManager)this.getSystemService(SENSOR_SERVICE);


        mag = sMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        acc = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sMgr.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL);
        sMgr.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);

        tv_mag = (TextView) findViewById(R.id.tv_mag);
        tv_acc = (TextView) findViewById(R.id.tv_acc);

        tv_rotation = (TextView) findViewById(R.id.tv_rotation);
        tv_orientation = (TextView) findViewById(R.id.tv_orientation);
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
        if (event.sensor == mag) {
            magValues = event.values;

            float s0 = event.values[0];
            float s1 = event.values[1];
            float s2 = event.values[2];

            tv_mag.setText("0: " + Float.toString(s0) + ", 1: " + Float.toString(s1) + ", 2: " + Float.toString(s2));

            magSet = true;
        }

        if (event.sensor == acc) {
            accValues = event.values;

            float s0 = event.values[0];
            float s1 = event.values[1];
            float s2 = event.values[2];

            tv_acc.setText("0: " + Float.toString(s0) + ", 1: " + Float.toString(s1) + ", 2: " + Float.toString(s2));

            accSet = true;
        }

        if (magSet && accSet) {
            sMgr.getRotationMatrix(r, i, accValues, magValues);

            String r0 = Float.toString(r[0]);
            String r1 = Float.toString(r[1]);
            String r2 = Float.toString(r[2]);
            String r3 = Float.toString(r[3]);
            String r4 = Float.toString(r[4]);
            String r5 = Float.toString(r[5]);
            String r6 = Float.toString(r[6]);
            String r7 = Float.toString(r[7]);
            String r8 = Float.toString(r[8]);

            tv_rotation.setText("0: " + r0 + " ,1: " + r1 + " ,2: " + r2 + " ,3: " + r3 + " ,4: " + r4 + " ,5: " + r5 + " ,6: " + r6 + " ,7: " + r7 + " ,8: " + r8);

            sMgr.getOrientation(r, oValues);

            String o0 = Float.toString(oValues[0]);
            String o1 = Float.toString(oValues[1]);
            String o2 = Float.toString(oValues[2]);

            tv_orientation.setText("0: " + o0 + " ,1: " + o1 + " ,2: " + o2);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
