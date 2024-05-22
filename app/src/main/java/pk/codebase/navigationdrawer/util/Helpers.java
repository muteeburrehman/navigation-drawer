package pk.codebase.navigationdrawer.util;

import android.app.Application;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Helpers extends Application {

    // Function to convert hexadecimal string to bytes
    public static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    // Function to convert bytes to hexadecimal string
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] convertTo32Bytes(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // If the hash is less than 32 bytes, we pad it with zeros
            byte[] result = new byte[32];
            System.arraycopy(hash, 0, result, 0, Math.min(hash.length, 32));

            return result;
        } catch (NoSuchAlgorithmException e) {
             Log.w("IOException", e.getMessage(), e);
            return null;
        }
    }
}
