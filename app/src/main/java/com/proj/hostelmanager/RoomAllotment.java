package com.proj.hostelmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RoomAllotment extends AppCompatActivity {
    EditText displayID, displayName, displayRoomType;
    Spinner roomSpinner;
    ImageView backButton;
    Button updateButton;
    DatabaseConnect databaseConnect;
    Cursor studentCursor, roomCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_allotment);

        displayID = findViewById(R.id.idDisplay);
        displayName = findViewById(R.id.nameDisplay);
        displayRoomType = findViewById(R.id.typeDisplay);

        roomSpinner = findViewById(R.id.roomSpinner);

        backButton = findViewById(R.id.returnButton);
        updateButton = findViewById(R.id.deallotButton);

        databaseConnect = new DatabaseConnect(this);

        studentCursor = databaseConnect.getStudentRecord();

        if (studentCursor != null && studentCursor.moveToFirst()) {
            displayID.setText(String.valueOf(studentCursor.getInt(studentCursor.getColumnIndexOrThrow("ID"))));
            displayName.setText(studentCursor.getString(studentCursor.getColumnIndexOrThrow("student_name")));
            displayRoomType.setText(studentCursor.getString(studentCursor.getColumnIndexOrThrow("room_type")));

            updateButton.setEnabled(true);

            studentCursor.close();
        }else
            updateButton.setEnabled(false);

        roomCursor = databaseConnect.getRoomNumbersByPreference(displayRoomType.getText().toString());
        List<String> roomNumbersList = new ArrayList<>();
        if (roomCursor != null && roomCursor.moveToFirst()) {
            do {
                roomNumbersList.add(roomCursor.getString(0));
            } while (roomCursor.moveToNext());
            roomCursor.close();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, roomNumbersList);
        roomSpinner.setAdapter(arrayAdapter);

        backButton.setOnClickListener(v -> onBackPressed());

        updateButton.setOnClickListener(v -> {
            String studentID = displayID.getText().toString().trim();
            int selectedRoomNumber = Integer.parseInt(roomSpinner.getSelectedItem().toString());
            long rowsAffected = databaseConnect.allotRoom(studentID, selectedRoomNumber);

            if (rowsAffected > 0) {
                Toast.makeText(getApplicationContext(), "Room updated successfully.", Toast.LENGTH_SHORT).show();

                // Close the previous studentCursor before retrieving the next record
                if (studentCursor != null) {
                    studentCursor.close();
                }

                // Retrieve the next record
                studentCursor = databaseConnect.getStudentRecord();

                if (studentCursor != null && studentCursor.moveToFirst()) {
                    displayID.setText(String.valueOf(studentCursor.getInt(studentCursor.getColumnIndexOrThrow("ID"))));
                    displayName.setText(studentCursor.getString(studentCursor.getColumnIndexOrThrow("student_name")));
                    displayRoomType.setText(studentCursor.getString(studentCursor.getColumnIndexOrThrow("room_type")));
                } else {
                    // No more records, show a message or perform any desired action
                    Toast.makeText(getApplicationContext(), "No more records.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), AdminWorkspace.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Failed to update room.", Toast.LENGTH_SHORT).show();
            }

        });



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), AdminWorkspace.class);
        startActivity(intent);
    }
}