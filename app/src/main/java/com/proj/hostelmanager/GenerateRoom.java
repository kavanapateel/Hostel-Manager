package com.proj.hostelmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GenerateRoom extends AppCompatActivity {

    TextView setFloor;
    EditText roomCountEditText, roomCapacityEditText;
    ToggleButton roomTypeEditText;
    Button insertButton, cancelButton;
    ImageButton backButton;
    int total_rooms, roomCapacity;
    DatabaseConnect databaseConnect = new DatabaseConnect(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_room);

        setFloor = findViewById(R.id.floorSpanner);

        roomCountEditText = findViewById(R.id.roomTotal);
        roomCapacityEditText = findViewById(R.id.roomCapTotal);
        roomTypeEditText = findViewById(R.id.toggleButton);

        insertButton = findViewById(R.id.insertButton);
        backButton = findViewById(R.id.backButton);
        cancelButton = findViewById(R.id.cancelButton);

        int setFloorNumber = Integer.parseInt(getIntent().getStringExtra("FLOOR_ID"));

        String spanFloorText = "Floor " + setFloorNumber;
        setFloor.setText(spanFloorText);

        insertButton.setOnClickListener(v -> {
            total_rooms = Integer.parseInt(roomCountEditText.getText().toString());
            roomCapacity = Integer.parseInt(roomCapacityEditText.getText().toString());

            String roomType = roomTypeEditText.isChecked() ? "AC" : "NON-AC";

            databaseConnect.insertRooms(setFloorNumber, total_rooms, roomCapacity, roomType);

            Cursor cursor = databaseConnect.getAllRoomCount();
            if (cursor.getCount() == 0) {
                Toast.makeText(getApplicationContext(), "Insert Fail", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), total_rooms + " rooms are inserted into Floor " + setFloorNumber, Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(getApplicationContext(), AdminWorkspace.class);
            startActivity(intent);

        });

        backButton.setOnClickListener(v -> onClick());
        cancelButton.setOnClickListener(v -> onClick());

        TextWatcher roomWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                insertButton.setEnabled(
                        !roomCountEditText.getText().toString().isEmpty()
                                && !roomCapacityEditText.getText().toString().isEmpty());
                if(insertButton.isEnabled()){

                    insertButton.setBackgroundColor(Color.rgb(64, 196, 255));

                }else {

                    insertButton.setBackgroundColor(Color.rgb(51, 51, 51));

                }
            }

            @Override
            public void afterTextChanged(Editable s) {}

        };

        roomCountEditText.addTextChangedListener(roomWatcher);
        roomCapacityEditText.addTextChangedListener(roomWatcher);
    }

    public void onClick() {
        Intent intent = new Intent(getApplicationContext(), AdminWorkspace.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}