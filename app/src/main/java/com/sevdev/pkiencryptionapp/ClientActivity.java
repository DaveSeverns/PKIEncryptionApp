package com.sevdev.pkiencryptionapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sevdev.pkiencryptionapp.Utilities.MyCrytoUtil;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ClientActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {


    EditText editText;
    String pubKeyString;
    String privKeyString;
    PublicKey pubKey;
    PrivateKey privKey;
    boolean keysAvailable;
    byte[] byteMe;
    boolean encrypted;
    MyCrytoUtil myCrytoUtil;
    NfcAdapter nfcAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        keysAvailable = false; // set to false if no keys generated
        encrypted = false;
        myCrytoUtil = new MyCrytoUtil(ClientActivity.this);
        editText = findViewById(R.id.editText);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Need to Enable NFC", Toast.LENGTH_SHORT).show();
        }

        nfcAdapter.setNdefPushMessageCallback(this,this);
        findViewById(R.id.generateKeyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getKeys();
            }
        });

        findViewById(R.id.encryptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!keysAvailable){
                    Toast.makeText(ClientActivity.this, "Need to generate keys", Toast.LENGTH_SHORT).show();
                }
                else if(editText.getText().toString() == null){
                    Toast.makeText(ClientActivity.this, "Enter text to encrypt", Toast.LENGTH_SHORT).show();
                }
                else if (encrypted){
                    Toast.makeText(ClientActivity.this, "Text already encrypted, press decrypt to try again",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    try {
                        byteMe = myCrytoUtil.encryptText(pubKey, myCrytoUtil.stringToByteArray(editText.getText().toString()));
                        editText.setText(myCrytoUtil.byteArrayToString(byteMe));
                        encrypted = true;
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }


                }
            }
        });

        findViewById(R.id.decryptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    editText.setText(myCrytoUtil.decryptText(privKey, byteMe ));
                    encrypted = false;
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void getKeys(){
        Cursor cursor = getContentResolver().query(Uri.parse("content://com.sevdev.pkiencryptionapp.encryptioncontentprovider"),
                null,null,null,null);

        cursor.moveToNext();
        pubKeyString = cursor.getString(0);
        privKeyString = cursor.getString(1);
        try {
            pubKey = myCrytoUtil.getPubKeyFromString(pubKeyString);
            privKey = myCrytoUtil.getPrivKeyFromString(privKeyString);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        Toast.makeText(ClientActivity.this, "Keys Generated", Toast.LENGTH_SHORT).show();
        keysAvailable = true;
        Log.e("public key", cursor.getString(0));
        Log.e("private key", cursor.getString(1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())){
            parseIntent(getIntent());
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String textToSend = editText.getText().toString();
        NdefMessage message;
        NdefRecord ndefRecord = NdefRecord.createMime("text/plain", textToSend.getBytes());
        message = new NdefMessage(ndefRecord);

        return message;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        setIntent(intent);
    }

    public void parseIntent(Intent intent){
        Parcelable[] raw = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage ndefMessage = (NdefMessage) raw[0];
        editText.setText(new String(ndefMessage.getRecords()[0].getPayload()));
    }
}
