package com.sevdev.pkiencryptionapp;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ClientActivity extends AppCompatActivity {

    Button genKeyButton;
    Button decryptButton;
    Button encryptButton;
    EditText editText;
    String textToEncrypt;
    String pubKeyString;
    String privKeyString;
    PublicKey pubKey;
    PrivateKey privKey;
    boolean keysAvailable;
    byte[] byteMe;



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
                pubKeyString = cursor.getString(0);
                privKeyString = cursor.getString(1);
                try {
                    pubKey = getPubKeyFromString(pubKeyString);
                    privKey = getPrivKeyFromString(privKeyString);
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
                else{
                    try {
                        byteMe = encryptText(pubKey, stringToByteArray(editText.getText().toString()));
                        editText.setText(byteArrayToString(byteMe));
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
                    editText.setText(decryptText(privKey, byteMe ));
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

    public byte[] encryptText(PublicKey key, byte[] plainText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        Toast.makeText(this, "Text Encrypted", Toast.LENGTH_SHORT).show();
        return cipher.doFinal(plainText);
    }

    public String decryptText(PrivateKey key, byte[] encryptedText) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        Toast.makeText(this, "Text Decrypted", Toast.LENGTH_SHORT).show();
        return byteArrayToString(cipher.doFinal(encryptedText));

    }

    //function to turn string to byte array
    public byte[] stringToByteArray(String string){
        byte[] array = string.getBytes();
        return array;
    }

    //get string back from byte array
    public String byteArrayToString(byte[] array){
        String string = new String(array);
        return string;
    }

    public PublicKey getPubKeyFromString(String keyAsString)throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = Base64.decode(keyAsString, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        return pubKey;
    }

    public PrivateKey getPrivKeyFromString(String keyAsString)throws NoSuchAlgorithmException, InvalidKeySpecException{
        byte[] publicBytes = Base64.decode(keyAsString, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = keyFactory.generatePrivate(keySpec);
        return privKey;
    }
}
