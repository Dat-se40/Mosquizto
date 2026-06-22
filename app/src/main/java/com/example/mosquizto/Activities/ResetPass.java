package com.example.mosquizto.Activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.R;
import com.example.mosquizto.ViewModels.ResetPassViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPass extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etMailReset;
    private Button btnSendLink;

    private ResetPassViewModel resetPassViewModel ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resetpass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.imageButton_back).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        prefillEmailIfProvided();
    }

    private void prefillEmailIfProvided() {
        String email = getIntent().getStringExtra(getString(R.string.intent_key_email));
        if (email != null && !email.trim().isEmpty()) {
            etMailReset.setText(email.trim());
            btnSendLink.setEnabled(true);
            btnSendLink.setAlpha(1.0f);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.imageButton_back);
        etMailReset = findViewById(R.id.editText_MailReset);
        btnSendLink = findViewById(R.id.btn_SendLink);
        resetPassViewModel = new ViewModelProvider(this).get(ResetPassViewModel.class);

        resetPassViewModel.errorMessage.observe(this, msg ->
        {
            Toast.makeText(this,msg, Toast.LENGTH_LONG).show();
        });
        resetPassViewModel.isResetLinkSet.observe(this,isSent ->
        {
            if(isSent){
                String email = etMailReset.getText().toString().trim();
                Intent intent = new Intent(ResetPass.this, RSPassNoti.class);
                intent.putExtra("user_email", email);
                startActivity(intent);
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etMailReset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                if (!email.isEmpty()) {
                    btnSendLink.setEnabled(true);
                    btnSendLink.setAlpha(1.0f);
                } else {
                    btnSendLink.setEnabled(false);
                    btnSendLink.setAlpha(0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSendLink.setOnClickListener(v -> {
            String email = etMailReset.getText().toString().trim();
            resetPassViewModel.ForgetPassword(email);
//            Intent intent = new Intent(ResetPass.this, RSPassNoti.class);
//            intent.putExtra("user_email", email);
//            startActivity(intent);
//
        });
    }
}
