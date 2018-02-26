package com.sevdev.pkiencryptionapp.Utilities;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by davidseverns on 2/22/18.
 */

public class MyCrytoUtil {
    Context context;
    Cipher cipher;
    Cipher cipher2;

    public MyCrytoUtil(Context context){
        this.context = context;
    }
    /*
    uses the cipher object to encode the plain text from string to byte array then encrypt it with public key
     */
    public byte[] encryptText(PrivateKey key, byte[] plainText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher2 = Cipher.getInstance("RSA");
        cipher2.init(Cipher.ENCRYPT_MODE, key);
        Toast.makeText(context, "Text Encrypted", Toast.LENGTH_SHORT).show();
        return cipher2.doFinal(plainText);
    }

    /*
    opposite of the encrypt method, cipher object uses the private key matched to the public key to decode the message to
    byte array and convert it back to a string to put it plain text
     */
    public String decryptText(PublicKey key, byte[] encryptedText) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        cipher = Cipher.getInstance("RSA");
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
    public String byteArrayToString(byte[] array) throws UnsupportedEncodingException {
        String string = new String(array);
        return string;
    }

    public PublicKey getPubKeyFromString(String keyAsString)throws NoSuchAlgorithmException, InvalidKeySpecException {
        //found reference for this code, the purpose is to take the key as string and convert to byte array
        // and then use the key factory and key spec to convert back to public key
        byte[] publicBytes = android.util.Base64.decode(keyAsString, android.util.Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        return pubKey;
    }

    public PrivateKey getPrivKeyFromString(String keyAsString)throws NoSuchAlgorithmException, InvalidKeySpecException{
        byte[] publicBytes = android.util.Base64.decode(keyAsString, android.util.Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = keyFactory.generatePrivate(keySpec);
        return privKey;
    }

    public String publicKeyToPEMFile(PublicKey pk) throws IOException {
        StringWriter writer = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        pemWriter.writeObject(pk);
        pemWriter.close();
        return writer.toString();
    }

    /**
     * This method takes the public key in the .pem format,
     * removes the header and footer and returns it back as Public Key
     * @param pemString
     * @return
     */
    public String parsePEMKeyAsStringToPublicKey(String pemString)throws NoSuchAlgorithmException,InvalidKeySpecException{
        String tempKeyString = pemString.replace("-----BEGIN PUBLIC KEY-----\n","");
        tempKeyString = tempKeyString.replace("-----END PUBLIC KEY-----\n","");

        return tempKeyString;
    }

}
