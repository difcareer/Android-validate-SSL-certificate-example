package org.ssl;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {

    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static final String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX_DIGITS[(b & 0xf0) >> 4]);
            sb.append(HEX_DIGITS[b & 0x0f]);
        }
        return sb.toString();
    }

    public static final String md5base64(byte buffer[]) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(buffer);
        byte buf2[] = digest.digest();
        return Base64.encodeToString(buf2, Base64.NO_WRAP | Base64.NO_PADDING | Base64.NO_CLOSE);
    }

    public static final String utf8md5base64(String str) throws NoSuchAlgorithmException {
        byte buf1[] = str.getBytes();
        return md5base64(buf1);
    }
}
