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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mosquizto.R;

public class ResetPass extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etMailReset;
    private Button btnSendLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resetpass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.imageButton_back);
        etMailReset = findViewById(R.id.editText_MailReset);
        btnSendLink = findViewById(R.id.btn_SendLink);
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
                    btnSendLink.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    btnSendLink.setTextColor(Color.parseColor("#0a0a2c"));
                } else {
                    btnSendLink.setEnabled(false);
                    btnSendLink.setAlpha(0.5f);
                    btnSendLink.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4254ff")));
                    btnSendLink.setTextColor(Color.parseColor("#f4f2f8"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSendLink.setOnClickListener(v -> {
            String email = etMailReset.getText().toString().trim();
            Intent intent = new Intent(ResetPass.this, RSPassNoti.class);
            intent.putExtra("user_email", email);
            startActivity(intent);
        });
    }
}
