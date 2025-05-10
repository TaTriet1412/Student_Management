package com.example.quanlyhocvien;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quanlyhocvien.object.Account;

import java.util.ArrayList;

public class AccountViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Account>> accountsLiveData = new MutableLiveData<>(new ArrayList<>());

    // Phương thức để lấy dữ liệu
    public MutableLiveData<ArrayList<Account>> getAccountsLiveData() {
        return accountsLiveData;
    }

    // Phương thức để cập nhật dữ liệu
    public void setAccountsLiveData(ArrayList<Account> accounts) {
        accountsLiveData.setValue(accounts);
    }

    // Phương thức để thêm một tài khoản mới
    public void addAccount(Account account) {
        ArrayList<Account> accounts = accountsLiveData.getValue();
        assert accounts != null;
        accounts.add(account);
        accountsLiveData.setValue(accounts);
    }

    // Phương thức để xóa một tài khoản
    public void removeAccount(Account account) {
        ArrayList<Account> accounts = accountsLiveData.getValue();
        if (accounts != null) {
            boolean removed = false;
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getUID().equals(account.getUID())) {
                    accounts.remove(i);
                    removed = true;
                    break;
                }
            }
            if (removed) {
                accountsLiveData.setValue(accounts);
            }
        }
    }

    // Phương thức để cập nhật thông tin của một tài khoản
    public void updateAccount(Account account) {
        ArrayList<Account> accounts = accountsLiveData.getValue();
        assert accounts != null;
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getUID().equals(account.getUID())) {
                accounts.set(i, account);
                break;
            }
        }
        accountsLiveData.setValue(accounts);
    }
}
