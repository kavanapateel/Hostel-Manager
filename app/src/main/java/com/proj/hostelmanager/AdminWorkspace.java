package com.proj.hostelmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AdminWorkspace extends AppCompatActivity {
    Button addFloorButton, insertNewFloorButton, logoutButton, allotButton, deAllotButton, feeStatusButton;
    ImageButton openMenuDrawer, closeMenuDrawer, addFloorCloseButton;
    RecyclerView recyclerView;
    LinearLayout menuDrawer, detailsLayout;
    ConstraintLayout constraintLayout, addNewFloors;
    TableRow floorRoomDetails;
    Spinner floorSpinner;
    TextView studentRegisterView, roomRegisterView, availableRoomView, studentRoomDetails, changePassword;
    EditText floorTotal, floorDescription;
    String floorCount, floorDesc;
    long count = 0;
    private boolean isMenuDrawerOpen = true;
    DatabaseConnect databaseConnect;
    FloorAdapter adapter;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_workspace);

        constraintLayout = findViewById(R.id.constraintLayout);

        addFloorButton = findViewById(R.id.addFloorButton);
        insertNewFloorButton = findViewById(R.id.insertFloorButton);
        addFloorCloseButton = findViewById(R.id.floorClose);
        logoutButton = findViewById(R.id.logout_button);
        allotButton = findViewById(R.id.allotButton);
        deAllotButton = findViewById(R.id.deleteStudentButton);
        feeStatusButton = findViewById(R.id.feeStatusButton);

        openMenuDrawer = findViewById(R.id.openMenu);
        closeMenuDrawer = findViewById(R.id.close_menu);
        menuDrawer = findViewById(R.id.menuDrawer);
        addNewFloors = findViewById(R.id.addNewFloorLayout);
        detailsLayout = findViewById(R.id.detailsLayout);
        floorRoomDetails = findViewById(R.id.floorRoomDetails);

        openMenuDrawer.setOnClickListener(v -> openDrawer());
        closeMenuDrawer.setOnClickListener(v -> closeDrawer());
        addFloorButton.setOnClickListener(v -> open());
        addFloorCloseButton.setOnClickListener(v -> close());

        studentRegisterView = findViewById(R.id.studentRegisterView);
        roomRegisterView = findViewById(R.id.roomRegisterView);
        availableRoomView = findViewById(R.id.availableRegisterView);
        studentRoomDetails = findViewById(R.id.detailsRoomStudent);
        changePassword = findViewById(R.id.forgotAdminPassword);

        floorTotal = findViewById(R.id.floorCount);
        floorDescription = findViewById(R.id.floordesc);

        recyclerView = findViewById(R.id.floorRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        floorSpinner = findViewById(R.id.floorSpinner);

        databaseConnect = new DatabaseConnect(getApplicationContext());
        adapter = new FloorAdapter(this);
        recyclerView.setAdapter(adapter);

        count = databaseConnect.getAllProfilesCount();
        studentRegisterView.setText(String.valueOf(count));

        cursor = databaseConnect.getAllRoomCount();
        count = cursor.getCount();
        cursor.close();
        roomRegisterView.setText(String.valueOf(count));

        cursor = databaseConnect.getAvailableRooms();
        count = cursor.getCount();
        cursor.close();
        availableRoomView.setText(String.valueOf(count));

        cursor = databaseConnect.getStudentCount();
        int allotted_students = cursor.getCount();
        if(allotted_students > 0) {
            String spanText = "Allotted rooms to \n" + allotted_students + " students";
            allotButton.setText(spanText);
        }
        cursor.close();

        constraintLayout.setOnClickListener(v -> closeDrawer());

        allotButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RoomAllotment.class);
            startActivity(intent);
        });

        deAllotButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RoomDeAllot.class);
            startActivity(intent);
        });

        changePassword.setOnClickListener( v -> {
            Intent intent = new Intent(getApplicationContext(), UpdatePassword.class);
            startActivity(intent);
        });

        feeStatusButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), FeeStatusActivity.class);
            startActivity(intent);
        });



        cursor = databaseConnect.getAllFloors();
        List<String> floorNumbersList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String floorName = "Floor " + cursor.getString(0);
                floorNumbersList.add(floorName);
            } while (cursor.moveToNext());
            cursor.close();
            floorRoomDetails.setVisibility(View.VISIBLE);
        } else {
            floorNumbersList.add("");
            floorRoomDetails.setVisibility(View.GONE);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, floorNumbersList);
        floorSpinner.setAdapter(arrayAdapter);

        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedFloorNumber = floorSpinner.getSelectedItem().toString(); // Get the selected floor number

                if (!selectedFloorNumber.isEmpty()) {
                    // Extract the floor number from the selected item
                    String floorNumber = selectedFloorNumber.replace("Floor ", "");

                    // Retrieve the room number based on the selected floor number
                    Cursor roomCursor = databaseConnect.getRoomsByFloor(floorNumber);

                    if (roomCursor != null && roomCursor.moveToFirst()) {
                        StringBuilder roomNumbers = new StringBuilder();

                        do {
                            int roomNumberColumnIndex = roomCursor.getColumnIndex("roomNumber");
                            int roomNumber = roomCursor.getInt(roomNumberColumnIndex);
                            String roomType = roomCursor.getString(roomCursor.getColumnIndexOrThrow("roomType"));

                            // Retrieve the student details based on the room number
                            Cursor studentCursor = databaseConnect.getStudentByRoom(roomNumber);
                            StringBuilder studentName = new StringBuilder();
                            String studentDetail;
                            if (studentCursor != null && studentCursor.moveToFirst()) {
                                do {
                                    studentDetail = String.format("%-7s %-20s %-10s\n",
                                            Integer.parseInt(studentCursor.getString(studentCursor.getColumnIndexOrThrow("ID"))),
                                            studentCursor.getString(studentCursor.getColumnIndexOrThrow("student_name")).toUpperCase(),
                                            studentCursor.getString(studentCursor.getColumnIndexOrThrow("contact_number"))
                                    );
                                    studentName.append(studentDetail);
                                }while (studentCursor.moveToNext());
                                studentCursor.close();
                            }

                            String roomText = "Room " + roomNumber + " (" + roomType + ")" + " VACANT\n\n";
                            if (studentName.length() > 0) {
                                roomText = "Room " + roomNumber + " (" + roomType + ")" + ":\n" + studentName + "\n\n";
                            }

                            roomNumbers.append(roomText);

                        } while (roomCursor.moveToNext());

                        roomCursor.close();

                        // Display the room numbers with student details in the studentRoomDetails TextView
                        String roomNumbersText = roomNumbers.toString().trim();
                        studentRoomDetails.setText(roomNumbersText);
                    } else {
                        studentRoomDetails.setText(R.string.no_rooms_found_for_this_floor);
                    }
                } else {
                    studentRoomDetails.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case when nothing is selected in the spinner
            }
        });

        insertNewFloorButton.setOnClickListener(v -> {
            floorCount = floorTotal.getText().toString().trim();
            floorDesc = floorDescription.getText().toString().trim();

            databaseConnect.insertFloors(floorCount, floorDesc);
            Toast.makeText(getApplicationContext(), floorDesc, Toast.LENGTH_SHORT).show();

            adapter.swapCursor(databaseConnect.getAllFloors());
            close();
        });

        logoutButton.setOnClickListener(v -> {
            Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(logout);
        });

        TextWatcher floorInsertWatch = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                floorCount = floorTotal.getText().toString().trim();
                insertNewFloorButton.setEnabled(!floorCount.isEmpty());

                if (!insertNewFloorButton.isEnabled()) {
                    insertNewFloorButton.setBackgroundColor(Color.rgb(51, 51, 51));
                    insertNewFloorButton.setTextColor(Color.rgb(12, 12, 12));
                } else {
                    insertNewFloorButton.setBackgroundColor(Color.rgb(64, 196, 255));
                    insertNewFloorButton.setTextColor(Color.rgb(242, 242, 242));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        floorTotal.addTextChangedListener(floorInsertWatch);
    }

    public void openDrawer() {
        if (!isMenuDrawerOpen) {
            menuDrawer.setVisibility(View.VISIBLE);
            openMenuDrawer.setVisibility(View.GONE);

            menuDrawer.post(() -> {
                ObjectAnimator animator = ObjectAnimator.ofFloat(menuDrawer, "translationX", -menuDrawer.getWidth(), 0);
                animator.setDuration(300);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.start();
            });

            detailsLayout.setVisibility(View.GONE);
            isMenuDrawerOpen = true;
        }
    }

    public void closeDrawer() {
        if (isMenuDrawerOpen) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(menuDrawer, "translationX", 0, -menuDrawer.getWidth());
            animator.setDuration(300);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    menuDrawer.setVisibility(View.GONE);
                    openMenuDrawer.setVisibility(View.VISIBLE);
                    isMenuDrawerOpen = false;
                }
            });
            animator.start();
            detailsLayout.setVisibility(View.VISIBLE);
        }
    }

    public void open() {
        addNewFloors.setVisibility(View.VISIBLE);
        menuDrawer.setVisibility(View.GONE);
        detailsLayout.setVisibility(View.GONE);
    }

    public void close() {
        addNewFloors.setVisibility(View.GONE);
        menuDrawer.setVisibility(View.VISIBLE);
        detailsLayout.setVisibility(View.VISIBLE);

        hideKeyboard(getApplicationContext(), addNewFloors);
    }

    public void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}
