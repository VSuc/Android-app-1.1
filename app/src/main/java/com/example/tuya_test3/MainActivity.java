package com.example.tuya_test3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.INeedLoginListener;
import com.tuya.smart.sdk.api.IResultCallback;


import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.enums.TYDevicePublishModeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String appKey = "";
    String appSecret = "";


    String countryCode = "420";
    String uid = "Vasek";
    String pass = "123456";

    long hid;
    boolean stat = true;

    List<HomeBean> homeBeanList;
    List<DeviceBean> deviceBeanList;

    HomeBean chosenHome;
    DeviceBean chosenDevice;
    ITuyaDevice mdevice;
    HashMap<String, Object> hashMap = new HashMap<>();
    Runnable r;


    volatile int cr = 0;
    volatile int cg = 0;
    volatile int cb = 0;


    int suc = 0;
    int err = 0;

    String lastErr;

    TextView textViewLogin;
    TextView textViewResult;
    TextView textViewErr;
    TextView textViewStatus;
    EditText editTextLog;

    Spinner listHome;
    Spinner listDevices;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewLogin = findViewById(R.id.textViewLogin);
        textViewResult = findViewById(R.id.textViewResult);
        textViewErr = findViewById(R.id.textViewErr);
        textViewStatus = findViewById(R.id.textViewStatus);
        editTextLog = findViewById(R.id.editTextLog);

        listHome = findViewById(R.id.listHome);
        listDevices = findViewById(R.id.listDevices);




        //Inicializace spojení
        TuyaHomeSdk.init(this.getApplication(), appKey, appSecret);
        TuyaHomeSdk.setDebugMode(true);




        //Přihlášení uživatele
        login();

        //V případě runtime potřeby prihlášení:
        TuyaHomeSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
                relogin();

           }
        });



        final Button pairBut = (Button) findViewById(R.id.pairBut);
        pairBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent p = new Intent(getBaseContext(), PairView.class);

                //Přiřadit zařízení k právě vybranému domu
                p.putExtra("hid", chosenHome.getHomeId());

                //EZ/AP pair mode
                RadioButton ez = findViewById(R.id.radioButtonEZ);
                p.putExtra("ez", ez.isChecked());

                startActivityForResult(p, 1);


            }
        });


        //Po stisknutí tlačítka Control se odešle příkaz s dpID 1 a hodnotou true/false do právě vybraného zařízení
        final Button controlBut = (Button) findViewById(R.id.controlBut);
        controlBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Vytvořit instanci právě vybraného zařízení
                mdevice = TuyaHomeSdk.newDeviceInstance(chosenDevice.getDevId());
                //Mapa příkazů
                HashMap<String, Object > hashMap = new HashMap<>();
                hashMap.put("1",stat);
                stat = !stat;
                //Odeslat příkaz
                mdevice.publishDps(JSONObject.toJSONString(hashMap), new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        editTextLog.setText("E " + error + code);
                    }

                    @Override
                    public void onSuccess() {
                        editTextLog.setText("OK");
                    }
                });

            }
        });



        //Při změně vybraného domu načíst zařízení
        listHome.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadDevices(homeBeanList.get(position).getHomeId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        //Při změně vybraného zařízení nastavit jako vybrané
        listDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(deviceBeanList != null)
                    chosenDevice = deviceBeanList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //V případě úspěšného párování aktualizovat seznamy
        if(requestCode == 1 && resultCode == RESULT_OK);
            loadHomes();
    }



    //Funkce pro testování propustnosti serveru
    public void sendFalse()
    {
        new Thread( new Runnable(){
            @Override
            public void run(){
                Looper.prepare();
                while (true)
                {
                for(int i = 0; i < 100;i++) {

                    try {
                        Thread.sleep(50);
                    }
                    catch (Exception e)
                    {

                    }
                            HashMap<String, Object> val = new HashMap<>();
                            /*
                            //kod použitý pro testování RGB wifi žárovky - změna barvy, formát RRGGBBHHHHSSVV, HSV data můžou být nahrazena nulami
                            val.put("5", String.format("%02x", cr) + String.format("%02x", cg) + String.format("%02x", cb) + "00000000");
                            cb++;
                            if (cb > 255)
                            {
                                cb = 0;
                                cg++;
                                if (cg > 255)
                                {
                                    cg = 0;
                                    cr++;
                                    if (cr > 255)
                                        cr = 0;
                                }
                            }

                            cr++;

                             */
                            //Testování wifi relay
                            val.put("1", false);
                            //Odeslat příkaz
                            mdevice.publishDps(JSONObject.toJSONString(val), TYDevicePublishModeEnum.TYDevicePublishModeInternet, new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    err++;
                                    lastErr = error;

                                }

                                @Override
                                public void onSuccess() {
                                    suc++;
                                }
                            });
               }
                //Vypsat výsledky
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String val = "";
                        val += "SUCCES: " + suc;
                        val += '\n';
                        val += "ERROR: " + err;

                        editTextLog.setText(val);
                        //et.setText(String.format("%02x", cr)+String.format("%02x", cg)+String.format("%02x", cb)+"00000000");
                        textViewErr.setText(lastErr);
                    }
                });
            }}

        }).start();

    }



    //Načíst domy uživatele
    public void loadHomes()
    {
        //Načtení domů z cloudu
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> beans) {

                if(beans.size()>0)
                {
                    //Uložení seznamu
                    homeBeanList = beans;
                    //Vytvoření seznamu domů
                    String [] arr = new String[beans.size()];
                    for(int i = 0; i < beans.size(); i++)
                    {
                        arr[i] = beans.get(i).getName() + ": " + beans.get(i).getHomeId();
                    }


                    //Nastavení seznamu domů do spinneru
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, arr);
                    listHome.setAdapter(adapter);



                    //Vybrání id prvního domu ze seznamu
                    hid = beans.get(0).getHomeId();


                    //Načtení zařízení v domě
                    loadDevices(hid);

                }


                else {
                    editTextLog.setText("NO HOMES");
                }


            }




            @Override
            public void onError(String errorCode, String error) {
                editTextLog.setText(errorCode);
            }
        });

    }


    //načíst zařízení ve vybraném domě
    public void loadDevices(long homeID)
    {

        //Načtení detailu vybraného domu
        TuyaHomeSdk.newHomeInstance(homeID).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {


                chosenHome = bean;

                //Vytvoření seznamu domů
                String[] arr ;
                if(chosenHome.getDeviceList().size()>0) {
                    arr = new String[chosenHome.getDeviceList().size()];
                    for (int i = 0; i < chosenHome.getDeviceList().size(); i++) {
                        arr[i] = chosenHome.getDeviceList().get(i).devId;
                    }

                    deviceBeanList = chosenHome.getDeviceList();
                    //Vybrání prvního zařízení v domě
                    chosenDevice = chosenHome.getDeviceList().get(0);

                    //Nastavení listeneru pro všechna zařízení v domě
                    for(DeviceBean d:deviceBeanList)
                    {
                        registerDeviceListener(d.getDevId());
                    }

                    //Spuštění funkce pro testování propustnosti serveru
                    //sendFalse();
                }
                else
                {
                    arr = new String[1];
                    arr[0] = "NO DEVICES";
                }

                //Nastavení seznamu zařízení do spinneru
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, arr);
                listDevices.setAdapter(adapter);

            }

            @Override
            public void onError(String errorCode, String errorMsg) {

                editTextLog.setText(errorMsg);
            }
        });
    }

    //Přiřadit listener změn k zařízení
    public void registerDeviceListener(final String deviceID)
    {

        ITuyaDevice device = TuyaHomeSdk.newDeviceInstance(deviceID);

        device.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                textViewStatus.setText(devId + ": " + dpStr);
            }

            @Override
            public void onRemoved(String devId) {

            }

            @Override
            public void onStatusChanged(String devId, boolean online) {

                textViewStatus.setText(devId + ": " +online);
            }

            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {

            }

            @Override
            public void onDevInfoUpdate(String devId) {

            }
        });
    }

    //První přihlášení
    public void login()
    {

        TuyaHomeSdk.getUserInstance().loginWithUid(countryCode, uid, pass, new ILoginCallback(){
            @Override
            public void onSuccess(User user) {
                textViewLogin.setText("OK");
                loadHomes();
            }

            @Override
            public void onError(String code, String error) {

                textViewLogin.setText(code);
            }
        });
    }


    //Opakované přihlášení
    public void relogin()
    {

        TuyaHomeSdk.getUserInstance().loginWithUid(countryCode, uid, pass, new ILoginCallback(){
            @Override
            public void onSuccess(User user) {
                textViewLogin.setText("OK");
            }

            @Override
            public void onError(String code, String error) {

                textViewLogin.setText(code);
            }
        });
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
}
