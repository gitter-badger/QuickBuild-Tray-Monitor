package com.pmease.quickbuild.monitor.utils;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

public class EncryptUtils {

	private static final String ENCRYPTION_KEY = "123456789012345678901234567890";
	
    public static String encrypt(String string) {
    	if (string == null)
    		return null;
        try {
            KeySpec keySpec = new DESedeKeySpec(ENCRYPTION_KEY.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            Cipher cipher = Cipher.getInstance("DESede");
        	
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ciphertext = cipher.doFinal(string.getBytes("UTF8"));
            return new String(Base64.encodeBase64(ciphertext));
        } catch (Exception e) {
        	if (e instanceof RuntimeException)
        		throw (RuntimeException)e;
        	else
        		throw new RuntimeException(e);
		}
    }

    public static String decrypt(String string) {
    	if (string == null)
    		return null;
        try {
            KeySpec keySpec = new DESedeKeySpec(ENCRYPTION_KEY.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            Cipher cipher = Cipher.getInstance("DESede");
        	
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytes = cipher.doFinal(Base64.decodeBase64(string.getBytes()));
            return new String(bytes);
        } catch (Exception e) {
        	if (e instanceof RuntimeException)
        		throw (RuntimeException)e;
        	else
        		throw new RuntimeException(e);
        }
    }

}
