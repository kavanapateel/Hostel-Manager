package com.proj.hostelmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ImageButton closeNotice;
    ConstraintLayout mainLayout, blurLayout;
    LinearLayout feeOverViewer;
    TableLayout profileData;
    TextView noticeView, noticeView2, spanText, bank_info, paymentDateView;
    EditText studentID, sNameEdit, contact, feeStatus;
    Button logoutButton, paymentButton, saveProfileButton;
    String student_id, studentName;
    DatabaseConnect databaseConnect;
    Cursor cursor;

    String defaultTextForRoomLabel = "Complete the verification process\n to get a room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.MainLlayout);
        blurLayout = findViewById(R.id.blur_layout);
        profileData =  findViewById(R.id.profileTable);
        feeOverViewer = findViewById(R.id.feeOverView);

        noticeView = findViewById(R.id.noticeView);
        noticeView2 = findViewById(R.id.noticeView2);
        spanText = findViewById(R.id.TextView);
        bank_info = findViewById(R.id.bankInfo);
        paymentDateView = findViewById(R.id.paymentDateView);

        studentID = findViewById(R.id.studentID);
        sNameEdit = findViewById(R.id.studentName);
        contact = findViewById(R.id.contactNumber);
        feeStatus = findViewById(R.id.feeStatus);

        closeNotice = findViewById(R.id.close_notice);

        logoutButton = findViewById(R.id.logout_button);
        paymentButton = findViewById(R.id.paymentButton);
        saveProfileButton = findViewById(R.id.saveProfile_button);

        databaseConnect = new DatabaseConnect(getApplicationContext());

        blurLayout.setVisibility(View.VISIBLE);
        feeOverViewer.setVisibility(View.GONE);
        noticeView2.setVisibility(View.GONE);
        spanText.setVisibility(View.GONE);
        profileData.setVisibility(View.GONE);
        paymentDateView.setVisibility(View.GONE);

        student_id = getIntent().getStringExtra("id");
        studentName = getIntent().getStringExtra("name");

        SpannableString spannableString = new SpannableString("Welcome " + studentName);
        spannableString.setSpan(new UnderlineSpan(), 8, 8 + studentName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.rgb(12, 178, 220)), 8, 8 + studentName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spanText.setText(spannableString);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                profileData.setVisibility(View.VISIBLE);
            }
        };

        spannableString.setSpan(clickableSpan, 8, 8 + studentName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spanText.setText(spannableString);
        spanText.setMovementMethod(LinkMovementMethod.getInstance());

        mainLayout.setOnClickListener(v -> {
            if(profileData.getVisibility() == View.VISIBLE) {
                profileData.setVisibility(View.GONE);
            }
        });

        cursor = databaseConnect.getStudentInfo(student_id);
        if (cursor != null && cursor.moveToFirst()) {
            if (cursor.getString(cursor.getColumnIndexOrThrow("fee_payment")).equalsIgnoreCase("UNPAID")) {
                bank_info.setText("To complete the allotment process, the fees need to be paid through online banking.\n The user needs to print the payment confirmation and produce it to the hostel administrator. ");
            } else if("PAID".equals(cursor.getString(cursor.getColumnIndexOrThrow("fee_payment")))) {
                String feeStatement = "Fee has been collected. \n" +
                        "Ensure to collect your room keys and happy staying. \n" +
                        "\n" +
                        "Also, this application acts as your Hostel ID. ";
                bank_info.setText(feeStatement);
                String payment_date = "Payment date: " + cursor.getString(cursor.getColumnIndexOrThrow("payment_date"));
                paymentDateView.setText(payment_date);
                paymentDateView.setVisibility(View.VISIBLE);
                paymentButton.setVisibility(View.GONE);
            }
            studentID.setText(cursor.getString(cursor.getColumnIndexOrThrow("ID")));
            sNameEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
            contact.setText(cursor.getString(cursor.getColumnIndexOrThrow("contact_number")));
            feeStatus.setText(cursor.getString(cursor.getColumnIndexOrThrow("fee_payment")));
        }

        StringBuilder notice = new StringBuilder();
        String FirstLetter = studentName.substring(0,1);
        studentName = studentName.substring(1).toLowerCase(Locale.ROOT);
        studentName = FirstLetter + studentName;
        notice
                .append("Hey ")
                .append(studentName)
                .append("!\n");

        cursor = databaseConnect.getStudentVerified(student_id);
        if(cursor != null && cursor.moveToFirst()){
            int roomNumber = cursor.getInt(cursor.getColumnIndexOrThrow("roomNumber"));

            StringBuilder notice2 = new StringBuilder();
            notice2.append("You're assigned room ")
                    .append(roomNumber).append(" on floor ")
                    .append(cursor.getString(cursor.getColumnIndexOrThrow("floorNumber")))
                    .append(". ");
            noticeView2.setText(notice2);

            if(closeNotice.getVisibility() == View.GONE){
                noticeView2.setVisibility(View.VISIBLE);
            }

            notice.append(notice2);
        }else {
            notice.append("Please verify the documents with \nthe Hostel Administrator. ");
            noticeView2.setText(defaultTextForRoomLabel);
        }
        cursor.close();
        noticeView.setText(notice);

        paymentButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
            intent.putExtra("id", student_id);
            intent.putExtra("name", studentName);
            startActivity(intent);
        });

        closeNotice.setOnClickListener(v -> {
            blurLayout.setVisibility(View.GONE);
            noticeView2.setVisibility(View.VISIBLE);
            spanText.setVisibility(View.VISIBLE);
            feeOverViewer.setVisibility(View.VISIBLE);
        });

        saveProfileButton.setOnClickListener(v -> {
            databaseConnect.updateStudentDetails(
                    studentID.getText().toString().trim(),
                    sNameEdit.getText().toString().trim(),
                    contact.getText().toString().trim()
            );
            Toast.makeText(getApplicationContext(), "Please login for changes to take place. ", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            Intent redirect = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(redirect);
        });

        TextWatcher updateWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = sNameEdit.getText().toString().trim();
                String contactNumber = contact.getText().toString().trim();

                saveProfileButton.setEnabled((!name.isEmpty() && !contactNumber.isEmpty()));
                if (saveProfileButton.isEnabled()) {
                    saveProfileButton.setBackgroundColor(Color.rgb(64, 196, 255));
                } else {
                    saveProfileButton.setBackgroundColor(Color.rgb(51, 51, 51));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        sNameEdit.addTextChangedListener(updateWatcher);
        contact.addTextChangedListener(updateWatcher);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}