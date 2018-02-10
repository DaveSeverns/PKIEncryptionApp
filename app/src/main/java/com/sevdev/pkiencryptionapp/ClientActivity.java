package com.sevdev.pkiencryptionapp;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ClientActivity extends AppCompatActivity {

    Button genKeyButton;
    Button decryptButton;
    Button encryptButton;
    EditText editText;
    String textToEncrypt;
    String pubKey;
    String privKey;
    boolean keysAvailable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        keysAvailable = false; // set to false if no keys generated
        editText = findViewById(R.id.editText);

        findViewById(R.id.generateKeyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = getContentResolver().query(Uri.parse("content://com.sevdev.pkiencryptionapp.encryptioncontentprovider"),
                        null,null,null,null);

                cursor.moveToNext();
                pubKey = cursor.getString(0);
                privKey = cursor.getString(1);
                keysAvailable = true;
                Log.e("public key", cursor.getString(0));
                Log.e("private key", cursor.getString(1));
            }
        });

        findViewById(R.id.encryptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!keysAvailable){
                    Toast.makeText(ClientActivity.this, "Need to generate keys", Toast.LENGTH_SHORT).show();
                }
                else if(editText.getText().toString().equals(null)){
                    Toast.makeText(ClientActivity.this, "Enter text to encrypt", Toast.LENGTH_SHORT).show();
                }
                else{

                }
            }
        });


    }
}
