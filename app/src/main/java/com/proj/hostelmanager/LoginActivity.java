package com.proj.hostelmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    // Declare layout elements
    ConstraintLayout constraintLayout;
    LinearLayout loginLayout, registerLayout;

    TextView registerView, login_view, loginValidation;

    EditText IDNumber, IDPassword,  // Login text editors
            IDEdit, nameEdit, parentEdit, contactEdit, passwordEdit; // Register text editors

    Button loginButton, registerButton;
    ToggleButton roomToggleButton;

    String id, password, idNumber, studentName, parentName, contactNumber, roomType;
    boolean doubleBackToExitPressedOnce = false;
    MessageDigest messageDigest;

    DatabaseConnect databaseConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database connection
        databaseConnect = new DatabaseConnect(this);

        // Define all layout elements
        constraintLayout = findViewById(R.id.constraintBG);
        registerLayout = findViewById(R.id.registerLayout);
        loginLayout = findViewById(R.id.loginLayout);

        registerView = findViewById(R.id.register_view);
        login_view = findViewById(R.id.login_view);
        // Validations for login
        loginValidation = findViewById(R.id.login_validator);

        IDNumber = findViewById(R.id.loginID);
        IDPassword = findViewById(R.id.loginPassword);
        IDEdit = findViewById(R.id.idEditor);
        nameEdit = findViewById(R.id.studentNameEditor);
        parentEdit = findViewById(R.id.parentNameEditor);
        contactEdit = findViewById(R.id.contact);
        passwordEdit = findViewById(R.id.passwordEditor);

        loginButton = findViewById(R.id.login_btn);
        registerButton = findViewById(R.id.registerButton);
        roomToggleButton = findViewById(R.id.room_toggle);

        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        registerView.setOnClickListener(v -> {
            IDNumber.setText("");
            IDPassword.setText("");

            // Check if at least one record is inserted
            long count = databaseConnect.getAllProfilesCount();
            long id_count = 1001;
            if (count < 1) {
                IDEdit.setText(String.valueOf(id_count));
            } else {
                id_count += count;
                IDEdit.setText(String.valueOf(id_count));
            }

            loginLayout.setVisibility(View.GONE);
            registerLayout.setVisibility(View.VISIBLE);
        });

        login_view.setOnClickListener(v -> switchView());

        // Verify account in SQLite and redirect
        loginButton.setOnClickListener(v -> {
            // Verify whether account exists
            idNumber = IDNumber.getText().toString().trim();
            password = IDPassword.getText().toString().trim();

            // Convert password to SHA-512
            byte[] digest = messageDigest.digest(password.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : digest) {
                stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            password = stringBuilder.toString();

            // Cursor cannot fetch data
            Cursor cursor = databaseConnect.fetchStudentAccount(idNumber, password);
            long count = cursor.getCount();
            String studentName = databaseConnect.getStudentName(idNumber, password);

            if (count > 0) {
                Intent sendData;
                if (!studentName.equals("Admin001")) {

                    // Redirect to next page on success
                    sendData = new Intent(LoginActivity.this, MainActivity.class);
                    sendData.putExtra("name", studentName);
                    sendData.putExtra("id", idNumber);
                } else {
                    sendData = new Intent(LoginActivity.this, AdminWorkspace.class);
                }
                startActivity(sendData);
            } else {
                IDPassword.setText("");
                IDPassword.requestFocus();
                loginValidation.setVisibility(View.VISIBLE);
            }
        });

        registerButton.setOnClickListener(v -> {
            idNumber = IDEdit.getText().toString().trim();
            studentName = nameEdit.getText().toString().toUpperCase().trim();
            parentName = parentEdit.getText().toString().toUpperCase().trim();
            contactNumber = contactEdit.getText().toString().trim();
            roomType = roomToggleButton.getText().toString().toUpperCase().trim();
            password = passwordEdit.getText().toString().trim();

            // Convert password to SHA-512
            byte[] digest = messageDigest.digest(password.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : digest) {
                stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            password = stringBuilder.toString();

            int roomTypeCounter = databaseConnect.getRoomCountPerType(roomType);
            if (roomTypeCounter >= 1) {
                if (!(Objects.equals(studentName, parentName)) && !(Objects.equals(studentName.toUpperCase(), "ADMIN"))
                        && (isFullName(studentName)) && (isFullName(parentName))
                        && (password.length() >= 8) && (contactNumber.length() == 10)) {

                    boolean insert = databaseConnect.insertStudentData(
                            idNumber, studentName, parentName, contactNumber, roomType, password
                    );
                    if (insert) {
                        Toast.makeText(LoginActivity.this, "Details Registered", Toast.LENGTH_SHORT).show();
                        switchView();
                    } else {
                        Toast.makeText(LoginActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                } else if (password.length() < 8) {
                    Toast.makeText(getApplicationContext(), "Password should be 8 characters long", Toast.LENGTH_SHORT).show();
                } else if (contactNumber.length() != 10) {
                    Toast.makeText(getApplicationContext(), "Please provide a correct phone number", Toast.LENGTH_SHORT).show();
                } else if (Objects.equals(studentName, parentName)) {
                    Toast.makeText(getApplicationContext(), "Parent name cannot be same as student name", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Internal error occurred. Please restart the application", Toast.LENGTH_SHORT).show();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(roomType + " rooms currently unavailable. ")
                        .setMessage("Causes: " + roomType + " Rooms are full\nPlease contact hostel administrator for more details.")
                        .setPositiveButton("Yes, I understand.", (dialog, which) -> dialog.dismiss());

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        TextWatcher loginWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                id = IDNumber.getText().toString().trim();
                password = IDPassword.getText().toString().trim();
                loginValidation.setVisibility(View.INVISIBLE);
                loginButton.setEnabled(!id.isEmpty() && !password.isEmpty());
                if (loginButton.isEnabled()) {
                    loginButton.setBackgroundColor(Color.rgb(64, 196, 255));
                } else {
                    loginButton.setBackgroundColor(Color.rgb(51, 51, 51));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        IDNumber.addTextChangedListener(loginWatcher);
        IDPassword.addTextChangedListener(loginWatcher);

        TextWatcher registerWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                studentName = nameEdit.getText().toString().trim();
                parentName = parentEdit.getText().toString().trim();
                contactNumber = contactEdit.getText().toString().trim();
                password = passwordEdit.getText().toString().trim();

                registerButton.setEnabled(
                        !studentName.isEmpty() && !parentName.isEmpty() && !contactNumber.isEmpty() && !password.isEmpty()
                );

                if (registerButton.isEnabled()) {
                    registerButton.setBackgroundColor(Color.rgb(68, 138, 255));
                } else {
                    registerButton.setBackgroundColor(Color.rgb(51, 51, 51));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        nameEdit.addTextChangedListener(registerWatcher);
        parentEdit.addTextChangedListener(registerWatcher);
        contactEdit.addTextChangedListener(registerWatcher);
        passwordEdit.addTextChangedListener(registerWatcher);

    }

    public void switchView() {
        nameEdit.setText("");
        parentEdit.setText("");
        contactEdit.setText("");
        passwordEdit.setText("");
        roomToggleButton.setChecked(false);

        loginLayout.setVisibility(View.VISIBLE);
        registerLayout.setVisibility(View.GONE);
    }

    public static boolean isFullName(String str) {
        String expression = "^[a-zA-Z\\s]+";
        return str.matches(expression);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(getApplicationContext(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}
