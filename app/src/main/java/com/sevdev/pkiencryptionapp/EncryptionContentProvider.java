package com.sevdev.pkiencryptionapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Base64;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class EncryptionContentProvider extends ContentProvider {

    KeyPair keyPair;
    KeyPairGenerator kpg;
    PublicKey publicKey;
    PrivateKey privateKey;


    public EncryptionContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }

    //provides a private and public key to the client in the form of a string will have to be converted back to keys
    //when encrypting and decrypting
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        generateKeys();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        //encode the keys from byte array to string
        String publicKeyAsString = Base64.encodeToString(publicKey.getEncoded(),Base64.DEFAULT);
        String privateKeyAsString = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
        String[] headings = {"public key","private key"};
        MatrixCursor matrixCursor = new MatrixCursor(headings);
        // add the public and private keys as strings and package them as row in the cursor
        matrixCursor.addRow(new String[]{publicKeyAsString,privateKeyAsString});

        return matrixCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //using keypairgenerator to get public and private RSA keys, stores them in key pair object
    public boolean generateKeys(){
        boolean encrypted = false;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            keyPair = kpg.generateKeyPair();
            encrypted = true;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encrypted;
    }
}
