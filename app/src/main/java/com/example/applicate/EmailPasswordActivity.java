package com.example.applicate;

import android.app.Activity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailPasswordActivity extends Activity {

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser usuario = mAuth.getCurrentUser();
        if (usuario != null) {
            reload();
        }
    }

    private void reload() {
        // TODO: Implement reload logic
    }

}