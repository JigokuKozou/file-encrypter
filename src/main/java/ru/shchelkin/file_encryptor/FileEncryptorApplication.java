package ru.shchelkin.file_encryptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.shchelkin.file_encryptor.models.Account;
import ru.shchelkin.file_encryptor.models.actions.ApplicationActions;
import ru.shchelkin.file_encryptor.services.AccountService;
import ru.shchelkin.file_encryptor.services.FileEncryptionService;

import java.util.List;

@SpringBootApplication
public class FileEncryptorApplication implements CommandLineRunner {

    private Account account;

    private final ConsoleInterface consoleInterface;

    private final AccountService accountService;

    private final FileEncryptionService fileEncryptionService;

    @Autowired
    public FileEncryptorApplication(ConsoleInterface consoleInterface, AccountService accountService, FileEncryptionService fileEncryptionService) {
        this.consoleInterface = consoleInterface;
        this.accountService = accountService;
        this.fileEncryptionService = fileEncryptionService;
    }

    public static void main(String[] args) {
        SpringApplication.run(FileEncryptorApplication.class, args);
    }

    @Override
    public void run(String... args) {

        try {
            do {
                var authorizationMethod = consoleInterface.requestAction("\nВыберите действие",
                        List.of(ApplicationActions.REGISTER, ApplicationActions.LOGIN, ApplicationActions.EXIT));

                if (authorizationMethod.equals(ApplicationActions.EXIT))
                    break;

                if (account != null) {
                    encryptAccountFiles();
                    account = null;
                }

                String username = consoleInterface.requestString("\nВведите имя пользователя");
                String password = consoleInterface.requestString("Введите пароль");

                if (authorizationMethod.equals(ApplicationActions.LOGIN)) {
                    try {
                        account = accountService.login(username, password);
                    }
                    catch (RuntimeException e) {
                        consoleInterface.showError(e.getMessage());
                        continue;
                    }

                    consoleInterface.showMessage("\nУспешный вход!",
                            ConsoleInterface.ANSI_GREEN);
                }
                else {
                    String repeatedPassword = consoleInterface.requestString("Повторите пароль");
                    try {
                        account = accountService.register(username, password, repeatedPassword);
                    }
                    catch (RuntimeException e) {
                        consoleInterface.showError(e.getMessage());
                        continue;
                    }

                    consoleInterface.showMessage("\nУспешная регистрация!\n" +
                            "Создана папка пользователя: " + account.directory(),
                            ConsoleInterface.ANSI_GREEN);
                }

                decryptAccountFiles();
            }
            while (true);
        }
        catch (RuntimeException e) {
            consoleInterface.showError(e.getMessage());
        } finally {
            if (account != null)
                encryptAccountFiles();
        }
    }

    public void encryptAccountFiles() {
        fileEncryptionService.encryptAllFiles(account.directory(), List.of(account.file()));
        consoleInterface.showMessage("Файлы пользователя \"%s\" зашифрованы!".formatted(account.username()),
                ConsoleInterface.ANSI_BLUE);
    }

    public void decryptAccountFiles() {
        fileEncryptionService.decryptAllFiles(account.directory());
        consoleInterface.showMessage("Файлы пользователя \"%s\" расшифрованы!".formatted(account.username()),
                ConsoleInterface.ANSI_CYAN);
    }
}
