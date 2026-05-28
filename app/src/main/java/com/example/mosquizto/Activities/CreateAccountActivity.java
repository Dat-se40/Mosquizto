package com.example.mosquizto.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;
import com.example.mosquizto.ViewModels.CreateAccountViewModel;

import java.util.Calendar;
import java.util.regex.Pattern;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateAccountActivity extends AppCompatActivity {

    private CreateAccountViewModel viewModel;

    private ImageButton btnBack;
    private EditText etEmail;
    private EditText etPassword;
    private ImageButton btnTogglePassword;
    private ImageButton btnToggleConfirmPassword ;
    private Button btnCreateAccount;

    private EditText etConfirmPassword;
    private EditText etFullName ;
    private EditText etUserName ;
    private String selectedBirthdate = "";

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

        viewModel = new ViewModelProvider(this).get(CreateAccountViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        btnBack          = findViewById(R.id.btnBack);
        etEmail          = findViewById(R.id.etEmail);
        etFullName      = findViewById(R.id.etFullName);
        etUserName = findViewById(R.id.etUserName);
        etPassword       = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword) ;
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword) ;
    }

    private void setupListeners() {
        // Back
        btnBack.setOnClickListener(v -> finish());

        // Toggle password
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility(this.etPassword,this.btnTogglePassword));
        btnToggleConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(this.etConfirmPassword,this.btnToggleConfirmPassword));
        // --- CẬP NHẬT GIAO DIỆN NÚT ĐĂNG KÝ ---

        // 1. Cài đặt trạng thái ban đầu (Làm mờ nút)
        updateCreateAccountButtonState();

        // 2. TextWatcher lắng nghe mọi thay đổi
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mỗi khi có bất kỳ ô nào thay đổi text, kiểm tra lại toàn bộ form
                updateCreateAccountButtonState();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        // Gắn TextWatcher vào TẤT CẢ các ô nhập liệu
        etEmail.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);
        etFullName.addTextChangedListener(watcher);
        etUserName.addTextChangedListener(watcher);


        // Submit
        btnCreateAccount.setOnClickListener(v -> {
            String email     = etEmail.getText().toString().trim();
            String password  = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString() ;
            String fullName = etFullName.getText().toString() ;
            String userName = etUserName.getText().toString();

            viewModel.signup(fullName,userName,email, password ,confirmPassword);
        });
    }

    // Hàm tổng hợp kiểm tra và cập nhật giao diện nút
    private void updateCreateAccountButtonState() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String fullName = etFullName.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();

        // Các điều kiện hợp lệ
        boolean emailOk = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matcher(email).matches();
        boolean passwordOk = password.length() >= 8;
        boolean passwordsMatch = password.equals(confirmPassword);
        boolean allFilled = !fullName.isEmpty() && !userName.isEmpty() && !confirmPassword.isEmpty();

        // Nếu tất cả hợp lệ -> Sáng nút
        if (emailOk && passwordOk && passwordsMatch && allFilled) {
            btnCreateAccount.setEnabled(true);
            btnCreateAccount.setAlpha(1.0f);
        } else {
            // Nếu có lỗi/thiếu -> Làm mờ nút
            btnCreateAccount.setEnabled(false);
            btnCreateAccount.setAlpha(0.5f);
        }
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            switch (state) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    new android.os.Handler().postDelayed(this::navigateToNotify   , 3000);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(this, viewModel.getErrorMessage(), Toast.LENGTH_LONG).show();
                    viewModel.resetState();
                    break;
                default:
                    setLoading(false);
                    break;
            }
        });
        viewModel.message.observe(this, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            // Thông báo gửi otp bên mail
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnCreateAccount.setEnabled(false);
            btnCreateAccount.setAlpha(0.5f);
            btnCreateAccount.setText("Processing…");
        } else {
            btnCreateAccount.setText(getString(R.string.btn_create_account));
            // Khi load xong, tính toán lại trạng thái nút dựa trên text hiện tại
            updateCreateAccountButtonState();
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedBirthdate = String.format("%02d/%02d/%04d", day, month + 1, year);
                    // etBirthdate.setText(selectedBirthdate);
                    updateCreateAccountButtonState(); // Cập nhật lại nút khi chọn xong ngày
                },
                cal.get(Calendar.YEAR) - 18,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void togglePasswordVisibility(EditText targetEdt, ImageButton btnToggle) {
        // Kiểm tra xem hiện tại nội dung đang ẩn hay hiện dựa vào TransformationMethod
        boolean isCurrentlyHidden = targetEdt.getTransformationMethod() instanceof PasswordTransformationMethod;

        if (isCurrentlyHidden) {
            targetEdt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnToggle.setImageResource(R.drawable.ic_visibility_on);
        } else {
            targetEdt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnToggle.setImageResource(R.drawable.ic_visibility_off);
        }
        targetEdt.setSelection(targetEdt.getText().length());
    }

    private void navigateToNotify()
    {
        Intent intent = new Intent(this, RSPassNoti.class);
        intent.putExtra("user_email",etEmail.getText().toString());
        startActivity(intent);
    }
}