package com.example.pygram.jedi;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.IOException;
import android.os.StrictMode;

public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Renderer ObjRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        ObjRenderer = new Renderer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ObjRenderer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ObjRenderer.stop();
    }

    private class Renderer implements SensorEventListener{
        private Sensor mRotationVectorSensor;
        private final float[] mRotationMatrix = new float[16];
        private float[] remappedRotationMatrix = new  float[16];
        private float[] orientations = new float[3];
        private DataOutputStream out;

        Renderer()
        {
            mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mRotationMatrix[0] = 1;
            mRotationMatrix[5] = 1;
            mRotationMatrix[10] = 1;
            mRotationMatrix[15] = 1;
        }

        private void start() {
            mSensorManager.registerListener(this, mRotationVectorSensor, 10000); }

        private void stop() {
            mSensorManager.unregisterListener(this);
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                SensorManager.remapCoordinateSystem(mRotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedRotationMatrix);
                SensorManager.getOrientation(remappedRotationMatrix, orientations);
                for(int i = 0; i < 3; i++) {
                    orientations[i] = (float) (Math.toDegrees(orientations[i]));
                }
                sendSockets();
            }
        }

        public void onAccuracyChanged(Sensor s, int i) {
            //Nothing to put here
        }
        private void sendSockets(){
            try {
                Socket client = new Socket("192.168.1.8", 12345);
                out = new DataOutputStream(client.getOutputStream());
                    //Turn left
                if (orientations[2] < -30) {
                    out.writeUTF("22");
                }
                //Turn right
                else if (orientations[2] > 45) {
                    out.writeUTF("33");
                }
                //Move forward
                else if (orientations[1] > 30) {
                    out.writeUTF("11");
                }
                //Stop
                else {
                    out.writeUTF("00");
                }
                out.flush();
                out.close();
                client.close();
            } catch (IOException e) {
                Log.i("1","IOStuff");
            }
        }
    }

}
