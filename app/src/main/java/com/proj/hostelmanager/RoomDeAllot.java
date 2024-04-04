package com.proj.hostelmanager;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class RoomDeAllot extends AppCompatActivity {
    ImageView backButton;
    EditText displayID, displayName, displayRoomNumber;
    Button deAllotButton, prevButton, nextButton;
    DatabaseConnect databaseConnect;
    Cursor studentCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_deallot);

        databaseConnect = new DatabaseConnect(getApplicationContext());

        backButton = findViewById(R.id.returnButton);

        displayID = findViewById(R.id.idDisplay);
        displayName = findViewById(R.id.nameDisplay);
        displayRoomNumber = findViewById(R.id.roomNumberDisplay);

        deAllotButton = findViewById(R.id.deallotButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);

        studentCursor = databaseConnect.getStudentRoomRecord();

        if (studentCursor != null && studentCursor.moveToFirst()) {
            displayStudentData(studentCursor);
            deAllotButton.setEnabled(true);
            prevButton.setEnabled(false); // Disable Previous button initially

            nextButton.setEnabled(studentCursor.getCount() != 1); // Disable Next button if there is only one record
            if (nextButton.isEnabled()) {
                nextButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), com.google.android.material.R.color.design_default_color_error));
            } else {
                nextButton.setBackgroundColor(Color.rgb(81, 81, 81)); // Set disabled color
            }
            prevButton.setBackgroundColor(Color.rgb(81, 81, 81));
        } else {
            deAllotButton.setEnabled(false);
            nextButton.setEnabled(false);
            prevButton.setEnabled(false);

            nextButton.setBackgroundColor(Color.rgb(81, 81, 81)); // Set disabled color
            prevButton.setBackgroundColor(Color.rgb(81, 81, 81)); // Set disabled color
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminWorkspace.class);
            startActivity(intent);
        });

        nextButton.setOnClickListener(v -> {
            if (studentCursor != null && studentCursor.moveToNext()) {
                displayStudentData(studentCursor);
                prevButton.setEnabled(true);
                nextButton.setBackgroundColor(Color.rgb(176, 0, 32)); // Revert to the original button color
            }

            if (studentCursor != null && studentCursor.isLast()) {
                nextButton.setEnabled(false);
                nextButton.setBackgroundColor(Color.rgb(81, 81, 81)); // Set disabled color
            }
        });

        prevButton.setOnClickListener(v -> {
            if (studentCursor != null && studentCursor.moveToPrevious()) {
                displayStudentData(studentCursor);
                nextButton.setEnabled(true);
                prevButton.setBackgroundColor(Color.rgb(176, 0, 32)); // Revert to the original button color
            }
            if (studentCursor != null && studentCursor.isFirst()) {
                prevButton.setEnabled(false);
                prevButton.setBackgroundColor(Color.rgb(81, 81, 81)); // Set disabled color
            }
        });

        deAllotButton.setOnClickListener(v -> {
            if (studentCursor != null && !studentCursor.isClosed()) {
                int studentID = studentCursor.getInt(studentCursor.getColumnIndexOrThrow("ID"));
                boolean deallotted = databaseConnect.deAllotRoom(String.valueOf(studentID));

                if (deallotted) {
                    // Update the cursor with the new query
                    studentCursor = databaseConnect.getStudentRoomRecord();

                    if (studentCursor != null && studentCursor.moveToFirst()) {
                        displayStudentData(studentCursor);
                        deAllotButton.setEnabled(true);
                        prevButton.setEnabled(false);

                        nextButton.setEnabled(studentCursor.getCount() != 1); // Disable Next button if there is only one record
                    } else {
                        deAllotButton.setEnabled(false);
                        displayID.setText("");
                        displayName.setText("");
                        displayRoomNumber.setText("");
                        nextButton.setEnabled(false);
                        prevButton.setEnabled(false);
                    }
                }
            }
        });
    }


    private void displayStudentData(Cursor cursor) {
        displayID.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("ID"))));
        displayName.setText(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
        displayRoomNumber.setText(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), AdminWorkspace.class);
        startActivity(intent);
    }
}
