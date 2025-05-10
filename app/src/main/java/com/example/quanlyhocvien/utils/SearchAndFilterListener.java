package com.example.quanlyhocvien.utils;

public interface SearchAndFilterListener {
    void onSearchTriggered(String content, boolean sortName, boolean sortAge, int gender);
    void onFilterChanged(String content, boolean sortName, boolean sortAge, int gender);
    void onFilterReset(String content, boolean sortName, boolean sortAge, int gender);
}