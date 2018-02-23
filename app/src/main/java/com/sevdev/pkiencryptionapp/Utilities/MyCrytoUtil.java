package com.sevdev.pkiencryptionapp.Utilities;

import android.content.Context;
import android.util.Base64;
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

/**
 * Created by davidseverns on 2/22/18.
 */

public class MyCrytoUtil {
    Context context;

    public MyCrytoUtil(Context context){
        this.context = context;
    }
    /*
    uses the cipher object to encode the plain text from string to byte array then encrypt it with public key
     */
    public byte[] encryptText(PublicKey key, byte[] plainText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        Toast.makeText(context, "Text Encrypted", Toast.LENGTH_SHORT).show();
        return cipher.doFinal(plainText);
    }

    /*
    opposite of the encrypt method, cipher object uses the private key matched to the public key to decode the message to
    byte array and convert it back to a string to put it plain text
     */
    public String decryptText(PrivateKey key, byte[] encryptedText) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        Toast.makeText(context, "Text Decrypted", Toast.LENGTH_SHORT).show();
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
        //found reference for this code, the purpose is to take the key as string and convert to byte array
        // and then use the key factory and key spec to convert back to public key
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
