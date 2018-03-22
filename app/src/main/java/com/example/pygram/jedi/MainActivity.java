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
    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            Socket client = new Socket("192.168.1.8", 12345);
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            out.writeUTF("7777");
            out.flush();
            out.close();
            client.close();
        }
        catch (IOException e){
            Log.i("1","IOStuff");
        }
    }

    private class Renderer implements SensorEventListener{
        private Sensor mRotationVectorSensor;
        private final float[] mRotationMatrix = new float[16];
        private float[] remappedRotationMatrix = new  float[16];
        private float[] orientations = new float[3];
        private int pre_color,post_color;

        Renderer()
        {
            mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mRotationMatrix[0] = 1;
            mRotationMatrix[5] = 1;
            mRotationMatrix[10] = 1;
            mRotationMatrix[15] = 1;
            pre_color = Color.WHITE;
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
                //Move forward
                if (orientations[1] > 35) {
                    if(orientations[2] < -30){
                        getWindow().getDecorView().setBackgroundColor(post_color = Color.RED);
                    }
                    else if(orientations[2] > 30){
                        getWindow().getDecorView().setBackgroundColor(post_color = Color.BLUE);
                    }
                    else {
                        getWindow().getDecorView().setBackgroundColor(post_color = Color.GREEN);
                    }
                }
                else if(orientations[1] < -15){
                    if(orientations[2] < -30){
                        getWindow().getDecorView().setBackgroundColor(post_color = Color.MAGENTA);
                    }
                    else if(orientations[2] > 30){
                        getWindow().getDecorView().setBackgroundColor(post_color = Color.YELLOW);
                    }
                    else {
                        getWindow().getDecorView().setBackgroundColor(post_color=Color.CYAN);
                    }
                }
                else{
                    getWindow().getDecorView().setBackgroundColor(post_color = Color.BLACK);
                }
                if(pre_color != post_color) {
                    sendSockets();
                    pre_color = post_color;
                }
            }
        }


        public void onAccuracyChanged(Sensor s, int i) {
            //Nothing to put here
        }
        private void sendSockets(){
            try {
                Socket client = new Socket("192.168.1.8",12345);
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                    //Turn left
                if (orientations[1] > 35) {
                    if(orientations[2] < -30){
                        out.writeUTF("2222");
                    }
                    else if(orientations[2] > 30){
                        out.writeUTF("3333");
                    }
                    else {
                        out.writeUTF("1111");
                    }
                }
                else if(orientations[1] < -35){
                    if(orientations[2] < -30){
                        out.writeUTF("5555");
                    }
                    else if(orientations[2] > 30){
                        out.writeUTF("6666");
                    }
                    else {
                        out.writeUTF("4444");
                    }
                }
                else{
                    out.writeUTF("0000");
                }
                out.close();
                client.close();
                Log.i("s","Successful transfer");
            } catch (IOException e) {
                Log.i("1","IOStuff");
            }
        }
    }

}
