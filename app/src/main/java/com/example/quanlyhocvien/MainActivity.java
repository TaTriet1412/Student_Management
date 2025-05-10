package com.example.quanlyhocvien;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.quanlyhocvien.Fragment.CertificateFragment;
import com.example.quanlyhocvien.Fragment.LoginHistoryFragment;
import com.example.quanlyhocvien.utils.GlobalVariables;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottom_navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);
        checkRole();
        loadFragment(new StudentFragment());
//        loadFragment(new AddCertificateFragment());
//        loadFragment(new EditCertificateFragment());
    }


    private NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.page_student) {
                loadFragment(new StudentFragment());
                return true;
            } else if (item.getItemId() == R.id.page_employee) {
                loadFragment(new EmployeeFragment());
                return true;
            } else if (item.getItemId() == R.id.page_myProfile) {
                loadFragment(new MyProfileFragment());
                return true;
            }
            return false;
        }
    };

    public void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void checkRole() {
        int role = GlobalVariables.getInstance().getCurrentAccount().getRole();
        if (role != 0) {
            // Không phải admin
            Menu menu = bottom_navigation.getMenu();
            menu.removeItem(R.id.page_employee);
        }
    }
}