import Encryption.CipherInterface;
import Encryption.ArnoldCatMapCipher;
import Encryption.StreamCipher;
import Utils.KeyUtils;
import Utils.MetadataUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ImageEncryption {
    public static void main(String[] args) {
        Arguments arguments = parseArguments(args);
        try {
            if (arguments.mode.equals("encrypt")) {
                encrypt(arguments);
                analyze(arguments);
            } else if (arguments.mode.equals("decrypt")) {
                decrypt(arguments);
            } else {
                System.err.println("Неизвестный режим: " + arguments.mode);
            }
        } catch (IOException | NoSuchAlgorithmException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void encrypt(Arguments args) throws IOException {
        System.out.println("Шифрование изображения: " +  args.input);

        CipherInterface cipher = createCipher(args.algorithm);

        String iv = args.iv;
        if (iv == null) {
            iv = cipher.generateIV();
        }

        cipher.encrypt(args.input, args.output, args.key, iv);
        System.out.println("Изображение зашифровано: " + args.output);

        String metaName = args.output.replace(".png", "_meta.json");
        MetadataUtils.saveMetadata(metaName, args.algorithm, args.key, iv);
        System.out.println("Метаданные сохранены: " + metaName);
    }

    private static void decrypt(Arguments arguments) throws IOException {
        System.out.println("Расшифрование файла: " +  arguments.input);

        if (arguments.iv == null) {
            var metadata = MetadataUtils.loadMetadata(arguments.meta);
            arguments.iv = (String) metadata.get("iv");
        }

        CipherInterface cipher = createCipher(arguments.algorithm);
        cipher.decrypt(arguments.input, arguments.output, arguments.key, arguments.iv);

        System.out.println("Файл расшифрован: " + arguments.output);
    }

    private static void analyze(Arguments args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        Arguments oneBitDiffArgs = new Arguments();

        String basePath = args.output.substring(0, args.output.lastIndexOf('.'));
        String newOutput = basePath + "EncAnotherKey.png";

        oneBitDiffArgs.mode = args.mode;
        oneBitDiffArgs.input = args.input;
        oneBitDiffArgs.output = newOutput;
        oneBitDiffArgs.algorithm = args.algorithm;
        oneBitDiffArgs.key = changeOneBit(args.key);
        oneBitDiffArgs.iv = args.iv;

        encrypt(oneBitDiffArgs);

        String pythonScript = "python/ImageAnalyzer.py";
        if (!new java.io.File(pythonScript).exists()) {
            System.err.println("Ошибка: файл не найден: " + pythonScript);
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(
                "python",
                pythonScript,
                args.input,
                args.output,
                oneBitDiffArgs.output
        );

        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            System.err.println("Python скрипт завершился с ошибкой (код: " + exitCode + ")");
        } else {
            System.out.println("Анализ завершён успешно!");
        }
    }

    private static CipherInterface createCipher(String algorithm) {
        if (algorithm.equals("stream")) {
            return new StreamCipher();
        } else if (algorithm.equals("perm-mix")) {
            return new ArnoldCatMapCipher();
        }

        throw new IllegalArgumentException("Неизвестный алгоритм: " + algorithm);
    }

    private static String changeOneBit(String key) throws NoSuchAlgorithmException {
        return KeyUtils.flipSpecificBit(key, 0);
    }

    private static Arguments parseArguments(String[] args) {
        Arguments arguments = new Arguments();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--mode" -> arguments.mode = args[++i];
                case "--in" -> arguments.input = args[++i];
                case "--out" -> arguments.output = args[++i];
                case "--algo" -> arguments.algorithm = args[++i];
                case "--key" -> arguments.key = args[++i];
                case "--iv" -> arguments.iv = args[++i];
                case "--meta" -> arguments.meta = args[++i];
            }
        }

        return arguments;
    }

    private static class Arguments {
        String mode;
        String input;
        String output;
        String algorithm;
        String key;
        String iv;
        String meta;
    }
}