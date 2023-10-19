package ru.shchelkin.file_encryptor.utils;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class Md5PasswordEncoder {

    private final MessageDigest md;

    public Md5PasswordEncoder() {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String encode(String password) {
        byte[] digest = md.digest(password.getBytes());

        return byteToHex(digest);
    }

    private String byteToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
