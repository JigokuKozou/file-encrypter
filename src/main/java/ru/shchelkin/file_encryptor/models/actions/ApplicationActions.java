package ru.shchelkin.file_encryptor.models.actions;

public enum ApplicationActions {
    NONE("Не выбрано"), REGISTER("Зарегистрироваться"), LOGIN("Войти"), EXIT("Выйти");

    private String displayName;

    ApplicationActions(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
