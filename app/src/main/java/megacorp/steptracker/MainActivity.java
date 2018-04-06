package megacorp.steptracker;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private LineGraphSeries<DataPoint> _series_raw_x;
    private LineGraphSeries<DataPoint> _series_raw_y;
    private LineGraphSeries<DataPoint> _series_raw_z;
    private LineGraphSeries<DataPoint> _series_raw_m; // magnitude
    private LineGraphSeries<DataPoint> _series_smooth_m;

    int MAX_DATA_POINTS = 100;
    private double _graph_last_t = MAX_DATA_POINTS;
    private void AddAccelerometerDataPoint(float x, float y , float z)
    {
        Log.i("init", "INFO AddAccelerometerDataPoint");
        _series_raw_x.appendData(new DataPoint(_graph_last_t, x), true, MAX_DATA_POINTS);
        _series_raw_y.appendData(new DataPoint(_graph_last_t, y), true, MAX_DATA_POINTS);
        _series_raw_z.appendData(new DataPoint(_graph_last_t, z), true, MAX_DATA_POINTS);

        _series_raw_m.appendData(new DataPoint(_graph_last_t, x + y + z), true, MAX_DATA_POINTS);

        _graph_last_t += 1d;
    }

    private float _initialSteps = 0;
    private TextView _actualSteps;

    private void AddStep(float steps)
    {
        if (_initialSteps == 0)
        {
            _initialSteps = steps;
        }
        else
        {
            float actualSteps = steps - _initialSteps;
            _actualSteps.setText("Actual: " + Float.toString(actualSteps));
        }
    }


    // Calls for Acceleration Sensor

    // accelerometer stuff
    private SensorManager _sensorManager;
    private Sensor _accelSensor;
    private Sensor _stepCounterSensor;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                AddAccelerometerDataPoint(x, y, z);
                break;

            case Sensor.TYPE_STEP_COUNTER:
                float steps = sensorEvent.values[0];
                AddStep(steps);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _actualSteps = (TextView)findViewById(R.id.steps_baseline);


        // See https://developer.android.com/guide/topics/sensors/sensors_motion.html
        _sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

        _stepCounterSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        _sensorManager.registerListener(this, _stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);



        _accelSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // The official Google accelerometer example code found here:
        //   https://github.com/android/platform_development/blob/master/samples/AccelerometerPlay/src/com/example/android/accelerometerplay/AccelerometerPlayActivity.java
        // explains that it is not necessary to get accelerometer events at a very high rate, by using a slower rate (SENSOR_DELAY_UI), we get an
        // automatic low-pass filter, which "extracts" the gravity component of the acceleration. As an added benefit, we use less power and
        // CPU resources. I haven't experimented with this, so can't be sure.
        // See also: https://developer.android.com/reference/android/hardware/SensorManager.html#SENSOR_DELAY_UI   SENSOR_DELAY_GAME
        _sensorManager.registerListener(this, _accelSensor, SensorManager.SENSOR_DELAY_UI);

        // Accelerometer Sensor Graph
        GraphView graph_raw_accelerometer = (GraphView) this.findViewById(R.id.graph_raw_accelerometer);

        _series_raw_x = new LineGraphSeries<>();
        _series_raw_y = new LineGraphSeries<>();
        _series_raw_z = new LineGraphSeries<>();
        _series_raw_m = new LineGraphSeries<>();

        _series_raw_x.setTitle("Raw X");
        _series_raw_x.setColor(Color.RED);

        _series_raw_y.setTitle("Raw Y");
        _series_raw_y.setColor(Color.GREEN);

        _series_raw_z.setTitle("Raw Z");
        _series_raw_z.setColor(Color.BLUE);

        _series_raw_m.setTitle("Raw M");
        _series_raw_m.setColor(Color.MAGENTA);

        graph_raw_accelerometer.addSeries(_series_raw_x);
        graph_raw_accelerometer.addSeries(_series_raw_y);
        graph_raw_accelerometer.addSeries(_series_raw_z);
        graph_raw_accelerometer.addSeries(_series_raw_m);

        graph_raw_accelerometer.setTitle("Accelerometer Real-Time Graph (Scrolling)");
        graph_raw_accelerometer.getGridLabelRenderer().setVerticalAxisTitle("X:R  Y:G  Z:B M:M");

        graph_raw_accelerometer.getViewport().setXAxisBoundsManual(true);
        graph_raw_accelerometer.getViewport().setMinX(0);
        graph_raw_accelerometer.getViewport().setMaxX(MAX_DATA_POINTS);




    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
