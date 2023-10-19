package ru.shchelkin.file_encryptor.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.shchelkin.file_encryptor.ConsoleInterface;
import ru.shchelkin.file_encryptor.models.Account;
import ru.shchelkin.file_encryptor.utils.Md5PasswordEncoder;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.DosFileAttributeView;

@Service
public class AccountService {

    public static final Path ACCOUNTS_DIRECTORY = Paths.get("accounts");

    private final Md5PasswordEncoder passwordEncoder;

    @Autowired
    public AccountService(Md5PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;

        createAccountsDirectory();
    }

    public Account register(String username, String password, String repeatedPassword) {
        username = username.trim();
        password = password.trim();
        repeatedPassword = repeatedPassword.trim();

        if (password.isBlank())
            throw new RuntimeException("Пароль не может быть пустым.");

        if (!password.equals(repeatedPassword))
            throw new RuntimeException("Пароли должны быть равными.");

        return save(username, passwordEncoder.encode(password));
    }

    public Account login(String username, String password) {
        username = username.trim();
        password = password.trim();

        final Path accountFile = getAccountFilePath(getAccountDirectoryPath(username));

        if (!Files.exists(accountFile)) {
            throw new IllegalArgumentException("Пользователь с именем \"%s\" не был найден.".formatted(username));
        }

        try {
            final String storedEncodedPassword = Files.readString(accountFile);

            if (!passwordEncoder.encode(password).equals(storedEncodedPassword))
                throw new IllegalArgumentException("Некорректный пароль.");

            return new Account(username, storedEncodedPassword, accountFile, getAccountDirectoryPath(username));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Account save(String username, String encodedPassword) {
        final Path accountDirectory = getAccountDirectoryPath(username);
        final Path accountFile = getAccountFilePath(accountDirectory);

        if (Files.exists(accountFile))
            throw new IllegalArgumentException("Пользователь с именем \"%s\" уже существует.".formatted(username));

        try {
            Files.createDirectories(accountDirectory);

            Files.write(accountFile, encodedPassword.getBytes(), StandardOpenOption.CREATE_NEW);

            DosFileAttributeView dosView = Files.getFileAttributeView(accountFile, DosFileAttributeView.class);

            if (dosView != null) {
                dosView.setHidden(true);
            }

            return new Account(username, encodedPassword, accountFile, accountDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void createAccountsDirectory() {
        try {
            Files.createDirectories(ACCOUNTS_DIRECTORY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getAccountDirectoryPath(String username) {
        return Paths.get(ACCOUNTS_DIRECTORY.toString(), username);
    }

    private Path getAccountFilePath(Path accountDirectory) {
        return Paths.get(accountDirectory.toString(), "account");
    }
}
