package com.example.quanlyhocvien.utils;

import com.example.quanlyhocvien.object.Account;

public class GlobalVariables {
    private static GlobalVariables instance;
    private Account currentAccount;

    // Private constructor để tránh khởi tạo từ bên ngoài
    private GlobalVariables() {}

    // Lấy instance của class (Singleton)
    public static synchronized GlobalVariables getInstance() {
        if (instance == null) {
            instance = new GlobalVariables();
        }
        return instance;
    }

    // Getter và setter cho currentAccount
    public Account getCurrentAccount() {
        return currentAccount;
    }
    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }
}

