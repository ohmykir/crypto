package Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MetadataUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void saveMetadata(String metaName, String algorithm, String key, String iv) throws IOException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("algorithm", algorithm);
        metadata.put("key", key);
        metadata.put("iv", iv);

        File file = new File("imgs/encrypted/" + metaName);
        file.getParentFile().mkdirs();

        objectMapper.writeValue(file, metadata);
    }

    public static Map<String, Object> loadMetadata(String metaName) throws IOException {
        File file = new File("imgs/encrypted/" + metaName);
        if (!file.exists()){
            throw new IOException("Файл метаданных не найден: " + metaName);
        }

        return objectMapper.readValue(file, Map.class);
    }
}