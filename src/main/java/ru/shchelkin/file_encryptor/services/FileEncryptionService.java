package ru.shchelkin.file_encryptor.services;

import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import java.util.List;

@Service
public class FileEncryptionService {

    public static final String ENCRYPTED_FILE_SUFFIX = ".encrypted";

    public static final String CRYPTOGRAPHIC_ALGORITHM = "AES";

    private final Key secretKey;

    public FileEncryptionService() {
        secretKey = new SecretKeySpec("ehCp1tTM66DSNyFT".getBytes(), CRYPTOGRAPHIC_ALGORITHM);
    }

    public void encryptAllFiles(Path directory, List<Path> excludedPaths) {
        try (var paths = Files.walk(directory)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !excludedPaths.contains(path))
                    .filter(path -> !isEncrypted(path))
                    .forEach(file -> encryptFile(file, getEncryptedFilePath(file)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void decryptAllFiles(Path directory) {
        try (var paths = Files.walk(directory)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(FileEncryptionService::isEncrypted)
                    .forEach(file -> decryptFile(file, getDecryptedFilePath(file)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void encryptFile(Path input, Path output) {
        try (var inputStream = Files.newInputStream(input, StandardOpenOption.DELETE_ON_CLOSE);
             var outputStream = Files.newOutputStream(output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            Cipher cipher = Cipher.getInstance(CRYPTOGRAPHIC_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            processFile(inputStream, outputStream, cipher);

        } catch (Exception e) {
            throw new RuntimeException("Error encrypting file: " + input, e);
        }
    }

    public void decryptFile(Path input, Path output) {
        try (var inputStream = Files.newInputStream(input, StandardOpenOption.DELETE_ON_CLOSE);
             var outputStream = Files.newOutputStream(output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            Cipher cipher = Cipher.getInstance(CRYPTOGRAPHIC_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            processFile(inputStream, outputStream, cipher);

        } catch (Exception e) {
            throw new RuntimeException("Error decrypting file: " + input, e);
        }
    }

    private void processFile(InputStream inputStream, OutputStream outputStream, Cipher cipher) throws IOException, IllegalBlockSizeException, BadPaddingException {
        byte[] inputBuffer = new byte[102400];
        int bytesRead;
        while ((bytesRead = inputStream.read(inputBuffer)) != -1) {
            byte[] outputBuffer = cipher.update(inputBuffer, 0, bytesRead);
            outputStream.write(outputBuffer);
        }
        byte[] outputBuffer = cipher.doFinal();
        outputStream.write(outputBuffer);
    }

    private static Path getEncryptedFilePath(Path path) {
        return path.resolveSibling(path.getFileName() + ENCRYPTED_FILE_SUFFIX);
    }

    private static Path getDecryptedFilePath(Path path) {
        return path.resolveSibling(stripExtension(path.getFileName()));
    }

    private static boolean isEncrypted(Path path) {
        return path.getFileName().toString().endsWith(ENCRYPTED_FILE_SUFFIX);
    }

    private static String stripExtension(Path path) {
        String fileName = path.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex != -1) {
            return fileName.substring(0, extensionIndex);
        }
        return fileName;
    }
}
