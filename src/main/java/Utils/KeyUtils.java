package Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class KeyUtils {
    public static long getSeed(String key, String iv) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = key + iv;
            byte[] hash = digest.digest(combined.getBytes());

            long seed = 0;
            for (byte b : hash) {
                seed = (seed << 8) | (b & 0xFF);
            }

            return seed;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String flipSpecificBit(String key, int pos) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(key.getBytes());

        int byteIndex = pos / 8;
        int bitIndex = pos % 8;

        if (byteIndex < keyBytes.length) {
            keyBytes[byteIndex] ^= (1 << bitIndex);
        }

        return bytesToHex(keyBytes);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length() / 2; i++) {
            int high = Integer.parseInt(hex.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hex.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }

        return result;
    }
}