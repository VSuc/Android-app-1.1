package com.example.tuya_test3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.builder.ActivatorBuilder;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.enums.ActivatorModelEnum;

public class PairView extends AppCompatActivity {

    String t;
    long homeId;
    boolean ez;
    protected ITuyaActivator mTuyaActivator;
    ITuyaSmartActivatorListener listener;

    //Wifi SSID a heslo
    String ssid;
    String pass;

    RadioButton radEZ;
    Button b;

    EditText etPass;
    EditText etSSID;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pairviewcontent);

        //Získat homeID
        Bundle extras = getIntent().getExtras();
        homeId = extras.getLong("hid");

        //Vypsat homeID
        TextView homeidview = (TextView) findViewById(R.id.textViewHome);
        homeidview.setText(Long.toString(homeId));

        radEZ = findViewById(R.id.radioButtonEZ);
        etSSID = findViewById(R.id.editTextSSID);
        etPass = findViewById(R.id.editTextPass);

        //načíst a vypsat jméno a heslo
        SharedPreferences settings = getApplicationContext().getSharedPreferences("WIFI", 0);
        ssid = settings.getString("ssid", "");
        etSSID.setText(ssid);
        pass = settings.getString("pass", "");
        etPass.setText(pass);

        //Nastavit listener kliknutí
        b = findViewById(R.id.buttonPair);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.setEnabled(false);
                //Získat data z grafických prvků
                ez = radEZ.isChecked();

                ssid = etSSID.getText().toString();
                pass = etPass.getText().toString();

                SharedPreferences settings = getApplicationContext().getSharedPreferences("WIFI", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("ssid", etSSID.getText().toString());
                editor.putString("pass", etPass.getText().toString());
                editor.apply();

                //Spustit párování
                pair();
            }
        });

    }

    private void pair()
    {
        //Listener událostí párování
        listener = new ITuyaSmartActivatorListener() {
            @Override
            public void onError(String errorCode, String errorMsg) {
                TextView act = (TextView) findViewById(R.id.textViewActive);
                act.setText(errorMsg);
                mTuyaActivator.stop();
            }

            //Zařízení se úspěšně připojí k serveru
            @Override
            public void onActiveSuccess(DeviceBean devResp) {

                TextView act = (TextView) findViewById(R.id.textViewActive);
                act.setText("OK");
                mTuyaActivator.stop();

                setResult(RESULT_OK);

            }

            @Override
            public void onStep(String step, Object data) {
                //Zařízení nalezeno
                if(step == "device_find")
                {
                    TextView x = (TextView) findViewById(R.id.textViewFound);
                    x.setText("OK");
                }
                //Data odeslána
                else if(step == "device_bind_success")
                {
                    TextView x = (TextView) findViewById(R.id.textViewPaired);
                    x.setText("OK");
                }
            }
        };

        //Získání párovacího tokenu
        TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId, new ITuyaActivatorGetToken() {
            @Override
            public void onSuccess(String token) {
                t = token;
                TextView tok = (TextView) findViewById(R.id.textViewToken);
                tok.setText(token);
                //EZ/AP pair
                if(ez)
                    EZpair();
                else
                    APpair();
            }

            @Override
            public void onFailure(String s, String s1) {
                TextView tok = (TextView) findViewById(R.id.textViewToken);
                tok.setText(s + " " + s1);
            }
        });
    }

    //Spárovat zařízení pomocí klasického AP modu:
    //Zařízení musí blikat každé dvě sekundy
    //Připojit se k potřebnému AP až po spuštění párování! - je potřeba nejdřív získat token ze serveru
    //
    private void APpair()
    {
        mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newActivator(new ActivatorBuilder()
                .setSsid(ssid)
                .setContext(this)
                .setPassword(pass)
                .setActivatorModel(ActivatorModelEnum.TY_AP)
                .setTimeOut(60)
                .setToken(t)
                .setListener(listener));

        mTuyaActivator.start();
    }

    //Spárovat zařízení pomoci EZ modu (UDP broadcast)
    //Zařízení musí blikat dvakrát za sekundu
    //je nezbytné mít telefon připojený ke stejné wifi, na kterou se připojuje zařízení
    private void EZpair()
    {
        mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newMultiActivator(new ActivatorBuilder()
                .setSsid(ssid)
                .setContext(this)
                .setPassword(pass)
                .setActivatorModel(ActivatorModelEnum.TY_EZ)
                .setTimeOut(100)
                .setToken(t)
                .setListener(listener));

        mTuyaActivator.start();
    }
}
