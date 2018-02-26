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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.sevdev.pkiencryptionapp.Utilities.MyCrytoUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ClientActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {


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
    Boolean keySentOverNfc;
    private final String KEY_SENT = "0";
    private final String TEXT_SENT = "1";
    private final String KEY_TAG = "PublicKey";


    TextView textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        keysAvailable = false; // set to false if no keys generated
        encrypted = false;
        keySentOverNfc = false;
        myCrytoUtil = new MyCrytoUtil(ClientActivity.this);

        textView = findViewById(R.id.EncryptedText);

        editText = findViewById(R.id.editText);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Need to Enable NFC", Toast.LENGTH_SHORT).show();
        }

        nfcAdapter.setNdefPushMessageCallback(this,this);
        nfcAdapter.setOnNdefPushCompleteCallback(this,this);
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
                        byteMe = myCrytoUtil.encryptText(privKey, myCrytoUtil.stringToByteArray(editText.getText().toString()));
                        textView.setText(byteMe.toString());

                        editText.setText(byteMe.toString());

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
                    Log.e("Decrypt Button: ", Base64.encodeToString(pubKey.getEncoded(),Base64.DEFAULT));
                    String temp = myCrytoUtil.decryptText(pubKey,byteMe);
                    Log.e("The String is: ", temp);
                    editText.setText(temp);
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
                } catch (UnsupportedEncodingException e) {
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
            try {
                parseIntent(getIntent());
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        if(!keysAvailable)
        {
            Toast.makeText(ClientActivity.this, "Need to generate keys", Toast.LENGTH_SHORT).show();
            return null;
        }
        String textToSend;
        NdefRecord[] records = new NdefRecord[2];
        NdefRecord ndefRecordBody;


        if(keySentOverNfc){

            records[0] = NdefRecord.createMime("text/plain", TEXT_SENT.getBytes());
            records[1] = NdefRecord.createMime("text/plain",byteMe);
        }else{
            try {
                textToSend = myCrytoUtil.publicKeyToPEMFile(pubKey);

            } catch (IOException e) {
                e.printStackTrace();
                textToSend = "Generate keys to send encrypted text";
            }
            records[0] = NdefRecord.createMime("text/plain", KEY_SENT.getBytes());
            ndefRecordBody = NdefRecord.createMime("text/plain", textToSend.getBytes());
            records[1] = ndefRecordBody;
        }
        NdefMessage message;


        message = new NdefMessage(records);

        return message;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        setIntent(intent);
    }

    public void parseIntent(Intent intent) throws InvalidKeySpecException, NoSuchAlgorithmException {

        Parcelable[] raw = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage ndefMessage = (NdefMessage) raw[0];
        String keyAsString;
        String tag = new String(ndefMessage.getRecords()[0].getPayload());
        Log.e("Tag: ",new String(ndefMessage.getRecords()[1].getPayload()));
        Log.e("Without Tags: ",myCrytoUtil.parsePEMKeyAsStringToPublicKey(new String(ndefMessage.getRecords()[1].getPayload())));

        if(tag.equals(KEY_SENT)){
            keyAsString =myCrytoUtil.parsePEMKeyAsStringToPublicKey(new String(ndefMessage.getRecords()[1].getPayload()));
            pubKey = myCrytoUtil.getPubKeyFromString(keyAsString);
            Log.e("Status: ", "Key Received!");
        }else if(tag.equals(TEXT_SENT)) {
            try {


                editText.setText(myCrytoUtil.decryptText(pubKey, ndefMessage.getRecords()[1].getPayload()));
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            encrypted = true;
        }else{
            Toast.makeText(this, "Houston We Have a Problem", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        if(!keySentOverNfc){
            keySentOverNfc = true;
        }
    }
}
