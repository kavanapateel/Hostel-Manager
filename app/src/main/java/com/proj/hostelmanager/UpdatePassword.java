package com.proj.hostelmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UpdatePassword extends AppCompatActivity {

    MessageDigest messageDigest;
    EditText oldPassText, newPassText, retypePassText;
    Button confirmButton, cancelButton;

    String oldPassword, newPassword, newPassword2;
    DatabaseConnect databaseConnect;
    long count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        oldPassText = findViewById(R.id.verifyPassText);
        newPassText = findViewById(R.id.setPassword);
        retypePassText = findViewById(R.id.retypeNewPassText);

        confirmButton = findViewById(R.id.changeButton);
        cancelButton = findViewById(R.id.cancelNewPassButton);

        databaseConnect = new DatabaseConnect(getApplicationContext());

        try {

            messageDigest = MessageDigest.getInstance("SHA-512");

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException(e);

        }

        TextWatcher passwordWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                textValues();

                confirmButton.setEnabled(!oldPassword.isEmpty() && !newPassword.isEmpty() && !newPassword2.isEmpty());
                if(confirmButton.isEnabled()){

                    confirmButton.setBackgroundColor(Color.rgb(64, 196, 255));

                }else {

                    confirmButton.setBackgroundColor(Color.rgb(51, 51, 51));

                }
            }

            @Override
            public void afterTextChanged(Editable s) {}

        };

        oldPassText.addTextChangedListener(passwordWatcher);
        newPassText.addTextChangedListener(passwordWatcher);
        retypePassText.addTextChangedListener(passwordWatcher);

        confirmButton.setOnClickListener(v -> {
            textValues();
            oldPassword = passwordGenerate(oldPassword);
            newPassword = passwordGenerate(newPassword);
            newPassword2 = passwordGenerate(newPassword2);

            if(oldPassword.length() < 8) {
                Toast.makeText(getApplicationContext(), "Password must be min. 8 characters", Toast.LENGTH_SHORT).show();
                oldPassText.requestFocus();
            } else if (newPassword.length() < 8) {
                Toast.makeText(getApplicationContext(), "Password must be min. 8 characters", Toast.LENGTH_SHORT).show();
                oldPassText.requestFocus();
            } else if (newPassword2.length() < 8){
                Toast.makeText(getApplicationContext(), "Password must be min. 8 characters", Toast.LENGTH_SHORT).show();
                oldPassText.requestFocus();
            } else if (oldPassword.equals(newPassword)) {
                Toast.makeText(getApplicationContext(), "New password cannot be same as old password", Toast.LENGTH_SHORT).show();
                newPassText.requestFocus();
            } else {
                if (!newPassword.equals(newPassword2)) {
                    Toast.makeText(getApplicationContext(), "Password confirmation mismatch", Toast.LENGTH_SHORT).show();
                    newPassText.requestFocus();
                }else if((count = databaseConnect.passwordMatch("001", oldPassword)) != 1){
                    Toast.makeText(getApplicationContext(), "Old password mismatch", Toast.LENGTH_SHORT).show();
                    oldPassText.requestFocus();
                }else {
                    databaseConnect.updatePassword("001", oldPassword, newPassword);
                    Toast.makeText(getApplicationContext(), "Password changed", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }

        });

        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminWorkspace.class);
            startActivity(intent);
        });
    }

    public void textValues() {
        oldPassword = oldPassText.getText().toString().trim();
        newPassword = newPassText.getText().toString().trim();
        newPassword2 = retypePassText.getText().toString().trim();

    }

    public String passwordGenerate(String password) {
        byte[] digest = messageDigest.digest(password.getBytes());
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : digest)
            stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

        return String.valueOf(stringBuilder);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}