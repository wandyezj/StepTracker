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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private LineGraphSeries<DataPoint> _series_raw_x;
    private LineGraphSeries<DataPoint> _series_raw_y;
    private LineGraphSeries<DataPoint> _series_raw_z;

    int MAX_DATA_POINTS = 100;
    private double _graph_last_t = MAX_DATA_POINTS;
    private void AddAccelerometerDataPoint(float x, float y , float z)
    {
        Log.i("init", "INFO AddAccelerometerDataPoint");
        _series_raw_x.appendData(new DataPoint(_graph_last_t, x), true, MAX_DATA_POINTS);
        _series_raw_y.appendData(new DataPoint(_graph_last_t, y), true, MAX_DATA_POINTS);
        _series_raw_z.appendData(new DataPoint(_graph_last_t, z), true, MAX_DATA_POINTS);

        _graph_last_t += 1d;
        /*
        outputX.setText("x:"+String.format("%.1f",x));
        outputY.setText("y:"+String.format("%.1f",y));
        outputZ.setText("z:"+String.format("%.1f",z));
        */
    }

    // Calls for Acceleration Sensor

    // accelerometer stuff
    private SensorManager _sensorManager;
    private Sensor _accelSensor;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                AddAccelerometerDataPoint(x, y, z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    // We use timers to intermittently generate random data for the two graphs
    /*
    private final Handler _handler = new Handler();
    private Runnable _timer1;
    private Runnable _timer2;

    private LineGraphSeries<DataPoint> _series1;
    private LineGraphSeries<DataPoint> _series2;
    private double _graph2LastXValue = MAX_DATA_POINTS;
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // See https://developer.android.com/guide/topics/sensors/sensors_motion.html
        _sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
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

        _series_raw_x.setTitle("Raw X");
        _series_raw_x.setColor(Color.RED);

        _series_raw_y.setTitle("Raw Y");
        _series_raw_y.setColor(Color.GREEN);

        _series_raw_z.setTitle("Raw Z");
        _series_raw_z.setColor(Color.BLUE);

        graph_raw_accelerometer.addSeries(_series_raw_x);
        graph_raw_accelerometer.addSeries(_series_raw_y);
        graph_raw_accelerometer.addSeries(_series_raw_z);

        graph_raw_accelerometer.setTitle("Accelerometer Real-Time Graph (Scrolling)");
        graph_raw_accelerometer.getGridLabelRenderer().setVerticalAxisTitle("X:R  Y:G  Z:B");

        graph_raw_accelerometer.getViewport().setXAxisBoundsManual(true);
        graph_raw_accelerometer.getViewport().setMinX(0);
        graph_raw_accelerometer.getViewport().setMaxX(MAX_DATA_POINTS);

        // Test graphs
    /*
        GraphView graph = (GraphView) this.findViewById(R.id.graph);
        _series1 = new LineGraphSeries<>(generateData());
        graph.addSeries(_series1);
        graph.setTitle("Real-Time Graph (Non-Scrolling)");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Random Data");

        GraphView graph2 = (GraphView) this.findViewById(R.id.graph2);
        _series2 = new LineGraphSeries<>(generateData());
        graph2.setTitle("Real-Time Graph (Scrolling)");
        graph2.addSeries(_series2);
        graph2.getGridLabelRenderer().setVerticalAxisTitle("Random Data");
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);
        */
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
        _timer1 = new Runnable() {
            @Override
            public void run() {
                _series1.resetData(generateData());
                _handler.postDelayed(this, 300);
            }
        };
        _handler.postDelayed(_timer1, 300);

        _timer2 = new Runnable() {
            @Override
            public void run() {
                _graph2LastXValue += 1d;
                _series2.appendData(new DataPoint(_graph2LastXValue, getRandom()), true, 40);
                _handler.postDelayed(this, 200);
            }
        };
        _handler.postDelayed(_timer2, 1000);
        */

        /*
        _timer2 = new Runnable() {
            @Override
            public void run() {
                _graph2LastXValue += 1d;
                _series_raw_x.appendData(new DataPoint(_graph2LastXValue, getRandom()), true, 40);
                _series_raw_y.appendData(new DataPoint(_graph2LastXValue, getRandom()), true, 40);
                _series_raw_z.appendData(new DataPoint(_graph2LastXValue, getRandom()), true, 40);
                _handler.postDelayed(this, 200);
            }
        };
        _handler.postDelayed(_timer2, 1000);
        */
    }

    @Override
    public void onPause() {
        //_handler.removeCallbacks(_timer1);
        //_handler.removeCallbacks(_timer2);
        super.onPause();
    }

    /**
     * Helper function to generate data for the graph
     * @return
     */
    /*
    private DataPoint[] generateData() {
        int count = 30;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double f = _rand.nextDouble() * 0.15 + 0.3;
            double y = Math.sin(i*f+2) + _rand.nextDouble()*0.3 + 5;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }

    double _lastRandom = 2;
    Random _rand = new Random();
    private double getRandom() {
        return _lastRandom += _rand.nextDouble() * 0.5 - 0.25;
    }
    */
}
