package Encryption;

import Utils.ImageUtils;
import Utils.KeyUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

public class ArnoldCatMapCipher implements CipherInterface{
    private int iterations;

    public ArnoldCatMapCipher() {
        this.iterations = 30;
    }

    @Override
    public void encrypt(String input, String output, String key, String iv) throws IOException {
        BufferedImage image = ImageUtils.loadImage("imgs/" + input);

        int [][][] channels = ImageUtils.imageToChannels(image);

        for (int ch = 0; ch < channels.length; ch++) {
            channels[ch] = applyArnoldCatMap(channels[ch], iterations);
        }

        byte[] transformedBytes = ImageUtils.channelsToBytes(channels);
        byte[] encrypted = applySubstitution(transformedBytes, key, iv);

        BufferedImage encryptedImage = ImageUtils.bytesToImage(encrypted);
        ImageUtils.saveImage(encryptedImage, "imgs/encrypted/" + output);
    }

    @Override
    public void decrypt(String input, String output, String key, String iv) throws IOException {
        BufferedImage encryptedImage = ImageUtils.loadImage("imgs/encrypted/" + input);
        byte[] imageBytes = ImageUtils.imageToBytes(encryptedImage);

        byte[] transformedBytes = removeSubstitution(imageBytes, key, iv);
        int[][][] channels = ImageUtils.bytesToChannels(transformedBytes);

        for (int ch = 0; ch < channels.length; ch++) {
            channels[ch] = applyInverseArnoldCatMap(channels[ch], iterations);
        }

        BufferedImage image = ImageUtils.channelsToImage(channels);
        ImageUtils.saveImage(image,"imgs/decrypted/" + output);
    }

    @Override
    public String generateIV() {
        SecureRandom random = new SecureRandom();
        int iterations = random.nextInt(26) + 5;

        return String.valueOf(iterations);
    }

    /**
     * Прямое преобразование Arnold Cat Map
     * [x'] = [1 1] [x]   (mod N)
     * [y']   [1 2] [y]
     * **/
    private int[][] applyArnoldCatMap(int[][] channel, int iterations) {
        int N = channel.length;
        int[][] result = copyChannel(channel);

        for (int iter = 0; iter < iterations ; iter++) {
            int[][] temp = new int[N][N];

            for (int y = 0; y < N; y++) {
                for (int x = 0; x < N; x++) {
                    int newX = mod((x + y), N);
                    int newY = mod((x + 2 * y), N);

                    temp[newY][newX] = result[y][x];
                }
            }

            result = temp;
        }

        return result;
    }

    /**
     * Обратное преобразование Arnold Cat Map
     * [x'] = [ 2  -1] [x]   (mod N)
     * [y']   [-1   1] [y]
     */
    private int[][] applyInverseArnoldCatMap(int[][] channel, int iterations) {
        int N = channel.length;
        int[][] result = copyChannel(channel);

        for (int iter = 0; iter < iterations ; iter++) {
            int[][] temp = new int[N][N];

            for (int y = 0; y < N; y++) {
                for (int x = 0; x < N; x++) {
                    int newX = mod((2 * x - y), N);
                    int newY = mod((-x + y), N);

                    temp[newY][newX] = result[y][x];
                }
            }

            result = temp;
        }

        return result;
    }

    private int mod(int a, int n) {
        a %= n;
        if (a < 0) a += n;
        return a;
    }

    private byte[] applySubstitution(byte[] input, String key, String iv) {
        long seed = KeyUtils.getSeed(key, iv);
        Random rand = new Random(seed);

        byte[] result = new byte[input.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (input[i] ^ (rand.nextInt() & 0xFF));
        }

        return result;
    }

    private byte[] removeSubstitution(byte[] input, String key, String iv) {
        return applySubstitution(input, key, iv);
    }

    private int[][] copyChannel(int[][] channel) {
        int N = channel.length;
        int[][] copy = new int[N][N];

        for (int i = 0; i < N; i++) {
            System.arraycopy(channel[i], 0, copy[i], 0, N);
        }

        return copy;
    }
}