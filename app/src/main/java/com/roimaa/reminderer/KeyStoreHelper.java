package com.roimaa.reminderer;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;

import javax.security.auth.x500.X500Principal;

public class KeyStoreHelper {
    private static final String TAG = KeyStoreHelper.class.getSimpleName();
    private static KeyStoreHelper mInstance;
    private final Context mContext;

    public static KeyStoreHelper getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new KeyStoreHelper(context);
        }
        return mInstance;
    }

    private KeyStoreHelper(Context context) {
        mContext = context;
    }

    public Boolean createNewAccount(String userName, String password) {
        AlgorithmParameterSpec spec =  new KeyGenParameterSpec.Builder(userName, KeyProperties.PURPOSE_SIGN)
                .setCertificateSubject(new X500Principal("CN=" + userName))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setCertificateSerialNumber(BigInteger.valueOf(1337))
                .build();

        try {
            KeyPairGenerator kpGenerator = KeyPairGenerator
                    .getInstance(
                            "RSA",
                            "AndroidKeyStore"
                    );
            kpGenerator.initialize(spec);
            kpGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Generating key failed", e);
            return false;
        }

        KeyStore ks = getKeyStore();
        if (null == ks) return false;
        PrivateKey privateKey;

        try {
            privateKey = (PrivateKey) ks.getKey(userName, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            Log.e(TAG, "Failed to get PrivateKey", e);
            return false;
        }

        try {
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initSign(privateKey);
            s.update(password.getBytes(StandardCharsets.UTF_8));
            String encrypted = Base64.encodeToString(s.sign(), Base64.NO_WRAP);
            PrefUtils.putString(mContext, userName, encrypted);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e ) {
            Log.e(TAG, "Failed to sign password", e);
            return false;
        }

        return true;
    }

    public Boolean authenticateUser(String userName, String password) {
        byte[] encrypted = Base64.decode(PrefUtils.getString(mContext, userName), Base64.NO_WRAP);

        KeyStore keyStore = getKeyStore();
        if (null == keyStore) return false;

        try {
            Certificate cert = keyStore.getCertificate(userName);
            if (null == cert) return false;
            PublicKey publicKey = cert.getPublicKey();
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(publicKey);
            s.update(password.getBytes(StandardCharsets.UTF_8));
            return s.verify(encrypted);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | KeyStoreException  e ) {
            Log.e(TAG, "Error verifying password", e);
            return false;
        }
    }

    private KeyStore getKeyStore() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException  e)  {
            Log.e(TAG, "Error getting KeyStore", e);
        }
        return keyStore;
    }
}
