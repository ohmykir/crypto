package Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {
    public static BufferedImage loadImage(String input) throws IOException {
        File file = new File(input);
        if (!file.exists()) {
            throw new IOException("Файл не найден: " + input);
        }

        return ImageIO.read(file);
    }

    public static void saveImage(BufferedImage image, String output) throws IOException {
        File file = new File(output);
        file.getParentFile().mkdirs();

        String format = getImageFormat(output);
        ImageIO.write(image, format, file);
    }

    public static String getImageFormat(String output) {
        return output.substring(output.lastIndexOf(".") + 1).toUpperCase();
    }

    public static byte[] imageToBytes(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] bytes = new byte[width * height * 3];

        int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
        int i = 0;

        for (int pixel : rgb) {
            bytes[i++] = (byte) ((pixel >> 16) & 0xFF);
            bytes[i++] = (byte) ((pixel >> 8) & 0xFF);
            bytes[i++] = (byte) (pixel & 0xFF);
        }

        return bytes;
    }

    //предполагаем, что изображение квадратное
    public static BufferedImage bytesToImage(byte[] bytes) {
        int pixelCount = bytes.length / 3;
        int side = (int) Math.sqrt(pixelCount);

        if (side * side * 3 != bytes.length) {
            throw new IllegalArgumentException("Неверный размер данных");
        }

        BufferedImage image = new BufferedImage(side, side, BufferedImage.TYPE_INT_RGB);

        int i = 0;
        for (int y = 0; y < side; y++) {
            for (int x = 0; x < side; x++) {
                int red = bytes[i++] & 0xFF;
                int green = bytes[i++] & 0xFF;
                int blue = bytes[i++] & 0xFF;

                int rgb = red << 16 | green << 8 | blue;
                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }

    public static int[][][] imageToChannels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][][] channels = new int[3][height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                channels[0][y][x] = (rgb >> 16) & 0xFF;
                channels[1][y][x] = (rgb >> 8) & 0xFF;
                channels[2][y][x] = rgb & 0xFF;
            }
        }

        return channels;
    }

    public static BufferedImage channelsToImage(int[][][] channels) {
        int width = channels[0].length;
        int height = channels[0][0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int red = channels[0][y][x] & 0xFF;
                int green = channels[1][y][x] & 0xFF;
                int blue = channels[2][y][x] & 0xFF;

                int rgb = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }

    public static byte[] channelsToBytes(int[][][] channels) {
        int height = channels[0].length;
        int width = channels[0][0].length;

        byte[] bytes = new byte[height * width * 3];

        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bytes[i++] = (byte) channels[0][y][x];
                bytes[i++] = (byte) channels[1][y][x];
                bytes[i++] = (byte) channels[2][y][x];
            }
        }

        return bytes;
    }

    public static int[][][] bytesToChannels(byte[] bytes){
        int size = (int) Math.sqrt(bytes.length / 3);
        int[][][] channels = new int[3][size][size];

        int i = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                channels[0][y][x] = bytes[i++] & 0xFF;
                channels[1][y][x] = bytes[i++] & 0xFF;
                channels[2][y][x] = bytes[i++] & 0xFF;
            }
        }

        return channels;
    }
}