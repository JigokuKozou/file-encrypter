package ru.shchelkin.file_encryptor;

import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.util.List;
import java.util.Scanner;

@Service
public class ConsoleInterface implements Closeable {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    private final Scanner scanner = new Scanner(System.in);

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showMessage(String message, String ANSI_FONT_CODE) {
        System.out.println(ANSI_FONT_CODE + message + ANSI_RESET);
    }

    public void showError(String errorMessage) {
        System.out.println(ANSI_RED + "Ошибка: " + errorMessage + ANSI_RESET);
    }

    public String requestString(String message) {
        System.out.println(message);

        return scanner.nextLine();
    }

    public Enum<?> requestAction(String message, List<Enum<?>> actions) {
        do {
            System.out.println(message);

            for (int i = 0; i < actions.size(); i++) {
                System.out.printf(" %d %s%n", i + 1, actions.get(i));
            }

            System.out.print(": ");

            try {
                return actions.get(Integer.parseInt(scanner.nextLine()) - 1);
            }
            catch (RuntimeException e) {
                showError("Некорректное значение.");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        while (true);
    }

    public void close() {
        scanner.close();
    }
}
