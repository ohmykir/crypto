package Encryption;

import Utils.ImageUtils;
import Utils.KeyUtils;
import prng.XorShift;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;

public class StreamCipher implements CipherInterface {
    @Override
    public void encrypt(String input, String output, String key, String iv) throws IOException {
        BufferedImage image = ImageUtils.loadImage("imgs/" + input);
        byte[] imageBytes = ImageUtils.imageToBytes(image);

        byte[] keyStream = generateKeyStream(key, iv, imageBytes.length);

        byte[] encrypted = new byte[imageBytes.length];
        for (int i = 0; i < imageBytes.length; i++) {
            encrypted[i] = (byte) (keyStream[i] ^ imageBytes[i]);
        }

        BufferedImage encryptedImage = ImageUtils.bytesToImage(encrypted);
        ImageUtils.saveImage(encryptedImage, "imgs/encrypted/" + output);
    }

    @Override
    public void decrypt(String input, String output, String key, String iv) throws IOException {
        BufferedImage encryptedImage = ImageUtils.loadImage("imgs/encrypted/" + input);
        byte[] imageBytes = ImageUtils.imageToBytes(encryptedImage);

        byte[] keyStream = generateKeyStream(key, iv, imageBytes.length);

        byte[] decryptedImage = new byte[imageBytes.length];
        for (int i = 0; i < imageBytes.length; i++) {
            decryptedImage[i] = (byte) (keyStream[i] ^ imageBytes[i]);
        }

        BufferedImage image = ImageUtils.bytesToImage(decryptedImage);
        ImageUtils.saveImage(image,"imgs/decrypted/" +  output);
    }

    @Override
    public String generateIV() {
        SecureRandom random = new SecureRandom();

        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return KeyUtils.bytesToHex(iv);
    }

    private byte[] generateKeyStream(String key, String iv, int length) {
        long seed = KeyUtils.getSeed(key, iv);
        XorShift prng = new XorShift(seed);

        byte[] keyStream = new byte[length];
        for (int i = 0; i < length; i++) {
            keyStream[i] = prng.nextByte();
        }

        return keyStream;
    }
}