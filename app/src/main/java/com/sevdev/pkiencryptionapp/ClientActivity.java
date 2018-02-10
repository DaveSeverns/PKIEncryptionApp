package com.sevdev.pkiencryptionapp;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ClientActivity extends AppCompatActivity {

    Button genKeyButton;
    Button decryptButton;
    Button encryptButton;
    EditText editText;
    String textToEncrypt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        findViewById(R.id.generateKeyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = getContentResolver().query(Uri.parse("content://com.sevdev.pkiencryptionapp.encryptioncontentprovider"),
                        null,null,null,null);

                cursor.moveToNext();
                Log.e("public key", cursor.getString(0));
                Log.e("private key", cursor.getString(1));
            }
        });


    }
}
