package ru.shchelkin.file_encryptor.models;

import java.nio.file.Path;

public record Account(String username, String encodedPassword, Path file, Path directory) {
}
