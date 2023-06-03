package com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser;

import android.util.Base64;

import com.imcorp.animeprog.Config;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser.AllohaVideoParser.FILE_SEPARATOR;

public class AESDecrypt implements SimpleDataDecrypter{
    // if string starts with #7
    private String iv,salt,chipperText;
    private Set<String> passwords;
    @Override
    public boolean checkIf(String i){
        return i.startsWith("#7");
    }
    AESDecrypt(final Set<String> token){
        this.passwords=token;
    }
    AESDecrypt(){}
    @Override
    public String decrypt(String data) throws GeneralSecurityException, UnsupportedEncodingException {
        final String[] splited = data.split(FILE_SEPARATOR);
        if(splited.length!=3)throw new AssertionError();
        salt = splited[2];
        iv = splited[1];
        chipperText = splited[0].substring(2);
        GeneralSecurityException ex=null;
        for (String i :this.passwords) {
            try {
                return decryptFromData(i);
            }catch (GeneralSecurityException e){
                ex=e;
            }
        }
        throw ex;
    }
    private String decryptFromData(final String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        int keySize = 8; // 8 words = 256-bit
        int ivSize = 4; // 4 words = 128-bit

        //byte[] iv = hexStringToByteArray(this.iv);
        byte[] salt = hexStringToByteArray(this.salt);
        byte[] cipherText = android.util.Base64.decode(chipperText.getBytes(Config.coding), Base64.DEFAULT);//fromBase64(chipperText);

        byte[] javaKey = new byte[keySize * 4];
        byte[] javaIv = new byte[ivSize * 4];
        evpKDF(password.getBytes(Config.coding), keySize, ivSize, salt, javaKey, javaIv);

        Cipher aesCipherForEncryption = Cipher.getInstance("AES/CBC/PKCS5Padding"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!

        IvParameterSpec ivSpec = new IvParameterSpec(javaIv);
        aesCipherForEncryption.init(Cipher.DECRYPT_MODE, new SecretKeySpec(javaKey, "AES"), ivSpec);

        byte[] byteMsg = aesCipherForEncryption.doFinal(cipherText);
        return new String(byteMsg, Config.coding);

    }
    private void evpKDF(byte[] password, int keySize, int ivSize, byte[] salt, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        evpKDF(password, keySize, ivSize, salt, 1, "MD5", resultKey, resultIv);
    }
    private void evpKDF(final byte[] password, final int keySize, final int ivSize, final byte[] salt, final int iterations, final String hashAlgorithm, final byte[] resultKey, final byte[] resultIv) throws NoSuchAlgorithmException {
        int targetKeySize = keySize + ivSize;
        byte[] derivedBytes = new byte[targetKeySize * 4];
        int numberOfDerivedWords = 0;
        byte[] block = null;
        MessageDigest hasher = MessageDigest.getInstance(hashAlgorithm);
        while (numberOfDerivedWords < targetKeySize) {
            if (block != null) {
                hasher.update(block);
            }
            hasher.update(password);
            block = hasher.digest(salt);
            hasher.reset();

            // Iterations
            for (int i = 1; i < iterations; i++) {
                block = hasher.digest(block);
                hasher.reset();
            }

            System.arraycopy(block, 0, derivedBytes, numberOfDerivedWords * 4,
                    Math.min(block.length, (targetKeySize - numberOfDerivedWords) * 4));

            numberOfDerivedWords += block.length/4;
        }

        System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4);
        System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4);

    }
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}