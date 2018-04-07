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
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.Random;

import static java.lang.Math.sqrt;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private LineGraphSeries<DataPoint> _series_raw_x;
    private LineGraphSeries<DataPoint> _series_raw_y;
    private LineGraphSeries<DataPoint> _series_raw_z;
    private LineGraphSeries<DataPoint> _series_raw_s; // sum
    private LineGraphSeries<DataPoint> _series_raw_m; // magnitude

    private LineGraphSeries<DataPoint> _series_smooth_x;
    private LineGraphSeries<DataPoint> _series_smooth_y;
    private LineGraphSeries<DataPoint> _series_smooth_z;

    private LineGraphSeries<DataPoint> _series_smooth_m;

    private PointsGraphSeries<DataPoint> _series_sample; // base off of smoothing window
    private PointsGraphSeries<DataPoint> _series_peak; // base off of smoothing window


    // smoothing accelerometer signal stuff
    // Increasing the size of the smoothing window will increasingly smooth the accel signal; however,
    // at a cost of responsiveness. Play around with different window sizes: 20, 50, 100...
    // Note that I've implemented a simple Mean Filter smoothing algorithm
    private static int SMOOTHING_WINDOW_SIZE = 20;

    private float _rawAccelValues[] = new float[3];
    private float _accelValueHistory[][] = new float[3][SMOOTHING_WINDOW_SIZE];
    private float _runningAccelTotal[] = new float[3];
    private float _curAccelAvg[] = new float[3];
    private int _curReadIndex = 0;

    private void Smooth(float x, float y, float z)
    {
        _rawAccelValues[0] = x;
        _rawAccelValues[1] = y;
        _rawAccelValues[2] = z;

        // Smoothing algorithm adapted from: https://www.arduino.cc/en/Tutorial/Smoothing
        for (int i = 0; i < 3; i++) {
            _runningAccelTotal[i] = _runningAccelTotal[i] - _accelValueHistory[i][_curReadIndex];
            _accelValueHistory[i][_curReadIndex] = _rawAccelValues[i];
            _runningAccelTotal[i] = _runningAccelTotal[i] + _accelValueHistory[i][_curReadIndex];
            _curAccelAvg[i] = _runningAccelTotal[i] / SMOOTHING_WINDOW_SIZE;
        }

        _curReadIndex++;
        if(_curReadIndex >= SMOOTHING_WINDOW_SIZE){
            _curReadIndex = 0;
        }
    }

    private static int MAGNITUDE_WINDOW_SIZE = 20;
    private double _magnitudes[] = new double[MAGNITUDE_WINDOW_SIZE];
    private int _index = 0;

    private int Previous(int index, int size)
    {
        if (index == 0)
        {
            return size - 1;
        }

        return index - 1;
    }

    private int Next(int index, int size)
    {
        if (index == size - 1)
        {
            return 0;
        }

        return index + 1;
    }

    private void PeakDetection(double smooth_m, double t)
    {
        //Log.i("peak", "INFO PeakDetection");
        // Post process previous and look for a peak

        _magnitudes[_index] = smooth_m;


        

        int middle_index = Previous(_index, MAGNITUDE_WINDOW_SIZE);
        int previous_index = Previous(middle_index, MAGNITUDE_WINDOW_SIZE);
        int next_index = Next(middle_index, MAGNITUDE_WINDOW_SIZE);

        //Log.i("peak", String.format("%d, %d, %d", previous_index, middle_index, next_index));

        //Log.i("peak", "INFO PeakDetection - Index");
        double previous = _magnitudes[previous_index];
        double middle = _magnitudes[middle_index];
        double next = _magnitudes[next_index];

        //Log.i("peak", "INFO PeakDetection - compare");
        if (previous < middle && middle > next)
        {
            //Log.i("peak", "INFO PeakDetection - add");
            double peak_t = t - 1;
            _series_peak.appendData(new DataPoint(peak_t, middle), true, MAX_DATA_POINTS);

        }



        // revolving buffer
        _index++;
        if (_index >= MAGNITUDE_WINDOW_SIZE)
        {
            _index = 0;
        }

    }



    private double magnitude(float x, float y, float z)
    {
        return sqrt(x * x + y * y + z * z);
    }


    int MAX_DATA_POINTS = 100;
    private double _graph_last_t = MAX_DATA_POINTS;
    private void AddAccelerometerDataPoint(float x, float y , float z)
    {
        //Log.i("init", "INFO AddAccelerometerDataPoint");
        _series_raw_x.appendData(new DataPoint(_graph_last_t, x), true, MAX_DATA_POINTS);
        _series_raw_y.appendData(new DataPoint(_graph_last_t, y), true, MAX_DATA_POINTS);
        _series_raw_z.appendData(new DataPoint(_graph_last_t, z), true, MAX_DATA_POINTS);

        _series_raw_s.appendData(new DataPoint(_graph_last_t, x + y + z), true, MAX_DATA_POINTS);

        _series_raw_m.appendData(new DataPoint(_graph_last_t, magnitude(x, y, z)), true, MAX_DATA_POINTS);


        // _series_smooth_m
        Smooth(x, y, z);
        float smooth_x = _curAccelAvg[0];
        float smooth_y = _curAccelAvg[1];
        float smooth_z = _curAccelAvg[2];

        _series_smooth_x.appendData(new DataPoint(_graph_last_t, smooth_x), true, MAX_DATA_POINTS);
        _series_smooth_y.appendData(new DataPoint(_graph_last_t, smooth_y), true, MAX_DATA_POINTS);
        _series_smooth_z.appendData(new DataPoint(_graph_last_t, smooth_z), true, MAX_DATA_POINTS);
        double smooth_m = magnitude(smooth_x, smooth_y, smooth_z);
        _series_smooth_m.appendData(new DataPoint(_graph_last_t, smooth_m), true, MAX_DATA_POINTS);

        _series_sample.appendData(new DataPoint(_graph_last_t, smooth_m), true, MAX_DATA_POINTS);


        PeakDetection(smooth_m, _graph_last_t);

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


    private LineGraphSeries<DataPoint> MakeSeries(String name, int color, int thinkness, GraphView graph)
    {
        LineGraphSeries<DataPoint> series =  new LineGraphSeries<>();
        series.setTitle(name);
        series.setColor(color);
        series.setThickness(thinkness);

        graph.addSeries(series);
        return series;
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

        int RAW_THICKNESS = 4;
        _series_raw_x = MakeSeries("Raw X", Color.RED, RAW_THICKNESS, graph_raw_accelerometer);
        _series_raw_y = MakeSeries("Raw Y", Color.GREEN, RAW_THICKNESS, graph_raw_accelerometer);
        _series_raw_z = MakeSeries("Raw Z", Color.BLUE, RAW_THICKNESS, graph_raw_accelerometer);
        _series_raw_s = MakeSeries("Raw S", Color.YELLOW, RAW_THICKNESS, graph_raw_accelerometer);
        _series_raw_m = MakeSeries("Raw M", Color.GRAY, RAW_THICKNESS, graph_raw_accelerometer);

        int SMOOTH_THICKNESS = 8;
        _series_smooth_x = MakeSeries("Smooth X", Color.RED, SMOOTH_THICKNESS, graph_raw_accelerometer);
        _series_smooth_y = MakeSeries("Smooth Y", Color.GREEN, SMOOTH_THICKNESS, graph_raw_accelerometer);
        _series_smooth_z = MakeSeries("Smooth Z", Color.BLUE, SMOOTH_THICKNESS, graph_raw_accelerometer);
        _series_smooth_m = MakeSeries("Smooth M", Color.BLACK, SMOOTH_THICKNESS, graph_raw_accelerometer);

        int POINT_SIZE= 10;
        _series_peak  = new PointsGraphSeries<>();
        _series_peak.setTitle("Peaks");
        _series_peak.setShape(PointsGraphSeries.Shape.TRIANGLE);
        _series_peak.setSize(POINT_SIZE);
        _series_peak.setColor(Color.MAGENTA);

        graph_raw_accelerometer.addSeries(_series_peak);


        _series_sample = new PointsGraphSeries<>();
        _series_sample.setTitle("sample raw m");
        _series_sample.setShape(PointsGraphSeries.Shape.POINT);
        _series_sample.setSize(2);
        _series_sample.setColor(Color.CYAN);

        graph_raw_accelerometer.addSeries(_series_sample);



        /*



        graph_raw_accelerometer.addSeries(_series_raw_x);
        graph_raw_accelerometer.addSeries(_series_raw_y);
        graph_raw_accelerometer.addSeries(_series_raw_z);
        graph_raw_accelerometer.addSeries(_series_raw_s);
        graph_raw_accelerometer.addSeries(_series_raw_m);


        graph_raw_accelerometer.addSeries(_series_smooth_m);
*/
        graph_raw_accelerometer.setTitle("Accelerometer Real-Time Graph (Scrolling)");
        graph_raw_accelerometer.getGridLabelRenderer().setVerticalAxisTitle("X:R  Y:G  Z:B  S:Y  M:GR  SM:BK");

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
