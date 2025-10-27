package Encryption;

import java.io.IOException;

public interface CipherInterface {
    void encrypt(String input, String output, String key, String iv) throws IOException;

    void decrypt(String input, String output, String key, String iv) throws IOException;

    String generateIV();
}