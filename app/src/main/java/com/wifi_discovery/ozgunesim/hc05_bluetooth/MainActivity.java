package com.wifi_discovery.ozgunesim.hc05_bluetooth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btn1,btn2,btn3;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    ProgressDialog pDialog;
    BackgroundTask bt;
    byte[] msg1 = new byte[]{(byte)0x00000001};
    byte[] msg2 = new byte[]{(byte)0x00000002};
    byte[] msg3 = new byte[]{(byte)0x00000003};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = (Button)findViewById(R.id.btn1);
        btn2 = (Button)findViewById(R.id.btn2);
        btn3 = (Button)findViewById(R.id.btn3);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData(msg1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData(msg2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData(msg3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        buttonActivation(false);

        runBT();

    }

    private void sendData(byte[] msg) throws IOException {
        mmOutputStream.write(msg);
    }

    private void runBT(){
        bt = new BackgroundTask(MainActivity.this);
        bt.execute();
    }

    public class BackgroundTask extends AsyncTask<Void, Void, Boolean> {
        private Context _context;
        public BackgroundTask(Context c){
            _context = c;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Bağlantı Kuruluyor...");
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            return findBT();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if(result != null){
                if(result == true){
                    showAlert("Bağlantı Kuruldu.");
                    buttonActivation(true);
                }else{
                    showAlert("Bağlantı Başarısız :(");
                    buttonActivation(false);
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Boolean result) {
            super.onCancelled(result);
        }

    }

    private void showAlert(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("HC05 Bağlantısı");

        builder
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        bt.cancel(true);
        runBT();
    }

    private Boolean findBT()
    {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            return false;
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            //pDialog.hide();
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
            return null;
        }


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Boolean isFound = false;
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-05"))
                {
                    mmDevice = device;
                    isFound = true;
                    break;
                }
            }
            if(isFound){
                //lblStatus.setText("Bulunan: " + mmDevice.getName() + "\nCihaza Baglaniyor...");
                //pDialog.setMessage("Cihaza Baglaniyor...");
                return openBT();
            }else {
                return false;
            }
        } else {
            return false;
        }
    }

    private Boolean openBT()
    {

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            //lblStatus.setText("HC-05'e Baglandi.");
            //buttonActivation(true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
            //lblStatus.setText("HC05'e Baglanilamadi.");
        }
        //pDialog.hide();

    }

    void closeBT()
    {
        try {
            if(mmOutputStream != null)
                mmOutputStream.close();
            if(mmInputStream != null)
                mmInputStream.close();
            if(mmSocket != null)
                mmSocket.close();
            buttonActivation(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void buttonActivation(boolean isEnabled){
        btn1.setEnabled(isEnabled);
        btn2.setEnabled(isEnabled);
        btn3.setEnabled(isEnabled);
    }


}
