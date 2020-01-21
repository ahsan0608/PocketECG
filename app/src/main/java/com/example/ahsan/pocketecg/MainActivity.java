package com.example.ahsan.pocketecg;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Handler;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    TextView data;

    //Buttons
    Button bConnect1;
    Button bConnect2;
    Button bDisconnect;
    Button bClear;
    Button bXminus;
    Button bXplus;

    //toggle Buttons
    ToggleButton tbLock;
    ToggleButton tbScroll;
    ToggleButton tbStream;
    static boolean Lock;
    static boolean AutoScrollX;
    static boolean Stream;

    //graph init
    static LinearLayout GraphView;
    static GraphView graphView;
    static GraphViewSeries Series;

    private static double graph2LastXValue = 0;
    private static int XView = 10;


    private final String DEVICE_ADDRESS1="B8:27:EB:68:A0:58"; //20:15:12:07:24:60";  pi-> B8:27:EB:68:A0:58
    private final String DEVICE_ADDRESS2="98:D3:31:F5:3E:D4"; //20:15:12:07:24:60";  pi-> B8:27:EB:68:A0:58
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    boolean deviceConnected=false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;

    int pulse;
    private String helpingHandContactNo;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout background = (LinearLayout) findViewById(R.id.bg);
        background.setBackgroundColor(Color.BLACK);


        init();
        ButtonInit();
        setUiEnabled(false);
    }

    public void setUiEnabled(boolean bool)
    {
        bConnect1.setEnabled(!bool);
        bConnect2.setEnabled(!bool);
        bDisconnect.setEnabled(bool);
        bClear.setEnabled(bool);
    }


    private void init() {


        data = (TextView) findViewById(R.id.dataText);

       GraphView = (LinearLayout) findViewById(R.id.Graph);
        Series = new GraphViewSeries("ECG signal",
                new GraphViewSeries.GraphViewStyle(Color.RED,5),
                new GraphView.GraphViewData[]{new GraphView.GraphViewData(0,0)});
        graphView = new LineGraphView(this,"ELECTROCARDIOGRAM");

        graphView.setBackgroundColor(Color.BLACK);
        graphView.setViewPort(0,XView);
        graphView.setScrollable(true);
        graphView.setScalable(true);
        graphView.setShowLegend(true);
        graphView.setLegendAlign(com.jjoe64.graphview.GraphView.LegendAlign.BOTTOM);
        graphView.setManualYAxis(true);
        graphView.setManualYAxisBounds(5,0);
        graphView.addSeries(Series);
//        graphView.redrawAll();
        GraphView.addView(graphView);


    }

    private void ButtonInit() {
        bConnect1 = (Button) findViewById(R.id.btConnect1);
        bConnect1.setOnClickListener(this);
        bConnect2 = (Button) findViewById(R.id.btConnect2);
        bConnect2.setOnClickListener(this);
        bDisconnect = (Button) findViewById(R.id.btDisConnect);
        bDisconnect.setOnClickListener(this);
        bClear = findViewById(R.id.btClear);
        bClear.setOnClickListener(this);
        bXminus = (Button) findViewById(R.id.btXminus);
        bXminus.setOnClickListener(this);
        bXplus = (Button) findViewById(R.id.btXplus);
        bXplus.setOnClickListener(this);

        tbLock = (ToggleButton) findViewById(R.id.tbLock);
        tbLock.setOnClickListener(this);
        tbScroll = (ToggleButton) findViewById(R.id.tbScroll);
        tbScroll.setOnClickListener(this);
        tbStream = (ToggleButton) findViewById(R.id.tbStream);
        tbStream.setOnClickListener(this);
        Lock= true;
        AutoScrollX = true;
        Stream = true;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btConnect1:
                if(BTinit1())
                {
                    if(BTconnect())
                    {
                        setUiEnabled(true);
                        deviceConnected=true;
                        beginListenForData1();
                        data.setText("Collecting data....");
                        //data.append("\nConnection Opened 1!\n");
                    }

                }
                break;
            case R.id.btConnect2:
                if(BTinit2())
                {
                    if(BTconnect())
                    {
                        setUiEnabled(true);
                        deviceConnected=true;
                        beginListenForData2();
                        //data.append("\nConnection Opened 2!\n");
                    }

                }
                break;
            case R.id.btDisConnect:
                stopThread = true;
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setUiEnabled(false);
                deviceConnected=false;
                //data.append("\nConnection Closed!\n");
                break;
            case R.id.btClear:
                data.setText("");
                break;
            case R.id.btXminus:
                if (XView>1){
                    XView--;
                }
                break;
            case R.id.btXplus:
                if (XView<30){
                    XView++;
                }
                break;
            case R.id.tbLock:
                if (tbLock.isChecked()){
                    Lock = true;
                }else {
                    Lock = false;
                }
                break;
            case R.id.tbScroll:
                if (tbScroll.isChecked()){
                    AutoScrollX = true;
                }else {
                    AutoScrollX = false;
                }
                break;
            case R.id.tbStream:
                if (tbStream.isChecked()){
                    Lock = true;
//                    if (Bluetooth.connectedThread != null){
//                        Bluetooth.connectedThread.write("E");
//                    }
                }else {
                    Lock = false;
//                    if (Bluetooth.connectedThread != null){
//                        Bluetooth.connectedThread.write("Q");
//                    }
                }
                break;

        }

    }


    public boolean BTinit1()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS1))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }



    public boolean BTinit2()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS2))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }




    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return connected;
    }


    public boolean isFloatNumber(String num){
        //Log.d("checkfloatNum", num);
        try{
            Double.parseDouble(num);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }





    void beginListenForData1()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            //final String string=new String(rawBytes,0,5);
                            runOnUiThread(new Runnable() {
                                public void run()
                                {
                                    // pulse = Integer.parseInt(string.toString());
                                    //data.append(string);
                                    //textView2.setText(pulse+10);

                                    //-------------------***********------------------------


                                    data.setText(string);

//                                    if (isFloatNumber(string)) {
//
//                                        Series.appendData(new GraphView.GraphViewData(graph2LastXValue,Double.parseDouble(string)),AutoScrollX);
//                                        graphView.scrollToEnd();
//
//                                        //X-axis control
//                                        if (graph2LastXValue >= XView && Lock == true){
//                                            Series.resetData(new GraphView.GraphViewData[] {});
//                                            graph2LastXValue = 0;
//                                        }else{
//                                            graph2LastXValue += 0.1;
//                                        }
//
//                                        if(Lock == true) {
//                                            graphView.setViewPort(0, XView);
//                                        }
//                                        else {
//                                            graphView.setViewPort(graph2LastXValue-XView, XView);
//                                        }
//
//                                        //refresh
//                                        GraphView.removeView(graphView);
//                                        GraphView.addView(graphView);
//
//                                    } else {
//                                        data.setText(string);
//                                    }


                                 //   -------------------************--------------------
                                }
                            });

//                            try {
//                                Thread.sleep(600);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }

                }
            }
        });

        thread.start();
    }



    void beginListenForData2()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            //final String string=new String(rawBytes,0,5);
                            runOnUiThread(new Runnable() {
                                public void run()
                                {
                                    // pulse = Integer.parseInt(string.toString());
                                    //data.append(string);
                                    //textView2.setText(pulse+10);

                                    //-------------------***********------------------------


                                    if (isFloatNumber(string)) {

                                        Series.appendData(new GraphView.GraphViewData(graph2LastXValue,Double.parseDouble(string)),AutoScrollX);
                                        graphView.scrollToEnd();

                                        //X-axis control
                                        if (graph2LastXValue >= XView && Lock == true){
                                            Series.resetData(new GraphView.GraphViewData[] {});
                                            graph2LastXValue = 0;
                                        }else{
                                            graph2LastXValue += 0.1;
                                        }

                                        if(Lock == true) {
                                            graphView.setViewPort(0, XView);
                                        }
                                        else {
                                            graphView.setViewPort(graph2LastXValue-XView, XView);
                                        }

                                        //refresh
                                        GraphView.removeView(graphView);
                                        GraphView.addView(graphView);

                                    }


                                    //   -------------------************--------------------
                                }
                            });

//                            try {
//                                Thread.sleep(600);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }

                }
            }
        });

        thread.start();
    }
















}
