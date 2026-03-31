package com.example.mosquizto.Activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.ViewModels.CreateAccountViewModel;

import javax.inject.Inject;

public class ProfilePage extends AppCompatActivity {
    private TextView tvUserName ;
    @Inject
    private SessionManager sessionManager ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnBack).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        initViews();
        setupListeners();
        observeViewModel();
    }

    private void observeViewModel() {
    }

    private void setupListeners() {

    }

    private void initViews() {
         tvUserName.setText(sessionManager.getCurrentUserProfile().getUsername()) ;
    }
}
