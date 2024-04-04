package com.proj.hostelmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

public class FeeStatusActivity extends AppCompatActivity {
    ImageButton returnButton, searchButton;
    ImageView filterViewButton;
    EditText searchText;
    TableRow checkBoxGroup;
    CheckBox paidCheck, unPaidCheck, acCheck, nonACCheck;
    TextView allRecordView;

    boolean isPaidChecked = false;
    boolean isUnPaidChecked = false;
    boolean isACChecked = false;
    boolean isNonACChecked = false;

    DatabaseConnect databaseConnect;
    StringBuilder filterQuery;
    String defaultText = "No records found. ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_status);

        databaseConnect = new DatabaseConnect(getApplicationContext());

        returnButton = findViewById(R.id.returnHomeButton);
        filterViewButton = findViewById(R.id.filterButton);

        searchText = findViewById(R.id.searchText);
        searchButton = findViewById(R.id.searchButton);

        checkBoxGroup = findViewById(R.id.checkboxGroup);

        paidCheck = findViewById(R.id.paidCheck);
        unPaidCheck = findViewById(R.id.unPaidCheck);
        acCheck = findViewById(R.id.acCheck);
        nonACCheck = findViewById(R.id.nonACCheck);
        allRecordView = findViewById(R.id.allRecordView);

        filterViewButton.setOnClickListener(v -> {
            if (checkBoxGroup.getVisibility() == View.GONE) {
                checkBoxGroup.setVisibility(View.VISIBLE);
            } else {
                checkBoxGroup.setVisibility(View.GONE);
            }
        });

        paidCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isPaidChecked = isChecked;

            applyFilters();
        });

        unPaidCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isUnPaidChecked = isChecked;

            applyFilters();
        });

        acCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isACChecked = isChecked;

            applyFilters();
        });

        nonACCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isNonACChecked = isChecked;

            applyFilters();
        });

        searchButton.setOnClickListener(v -> {
            String searchValue = searchText.getText().toString().trim();
            boolean isNumeric = searchValue.matches("\\d+");

            // Build the base query
            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM student_details");

            // Check if any filters are selected
            boolean isFilterSelected = isPaidChecked || isUnPaidChecked || isACChecked || isNonACChecked;

            if (isFilterSelected) {
                queryBuilder.append(" WHERE ");

                // Add filter conditions
                if (isPaidChecked) {
                    queryBuilder.append("fee_payment = 'PAID' AND ");
                }

                if (isUnPaidChecked) {
                    queryBuilder.append("fee_payment = 'UNPAID' AND ");
                }

                if (isACChecked) {
                    queryBuilder.append("room_type = 'AC' AND ");
                }

                if (isNonACChecked) {
                    queryBuilder.append("room_type = 'NON-AC' AND ");
                }

                // Remove the trailing "AND"
                int queryLength = queryBuilder.length();
                if (queryLength > 0 && queryBuilder.substring(queryLength - 4).equals("AND ")) {
                    queryBuilder.delete(queryLength - 4, queryLength);
                }
            }

            if (!searchValue.isEmpty()) {
                if (isFilterSelected) {
                    queryBuilder.append(" AND ");
                } else {
                    queryBuilder.append(" WHERE ");
                }

                if (isNumeric) {
                    // Search by ID
                    queryBuilder.append("ID = '").append(searchValue).append("'");
                } else {
                    // Search by Name
                    queryBuilder.append("student_name LIKE '%").append(searchValue).append("%'");
                }
            }

            // Execute the query
            Cursor cursor = databaseConnect.rawQuery(queryBuilder.toString(), null);

            if (cursor.moveToFirst()) {
                StringBuilder allRecords = new StringBuilder();
                allRecords.append(String.format("%-8s %-8s %-20s %-12s\n", "ID", "Room", "Name", "Fee Details"));
                do {
                    String recordName = cursor.getString(cursor.getColumnIndexOrThrow("student_name"));
                    if (!recordName.equals("Admin001")) {
                        allRecords
                                .append(cursor.getString(cursor.getColumnIndexOrThrow("ID"))).append(" - ")
                                .append(cursor.getString(cursor.getColumnIndexOrThrow("room_number"))).append(" - ")
                                .append(recordName).append(" - ")
                                .append(cursor.getString(cursor.getColumnIndexOrThrow("fee_payment"))).append(" - ")
                                .append(cursor.getString(cursor.getColumnIndexOrThrow("payment_date")))
                                .append("\n");
                    }
                } while (cursor.moveToNext());

                cursor.close();
                allRecordView.setText(allRecords.toString());
                allRecordView.setVisibility(View.VISIBLE);
            } else {
                cursor.close();
                allRecordView.setText(defaultText); // Set default message
                allRecordView.setVisibility(View.VISIBLE);
            }
        });

        applyFilters();

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminWorkspace.class);
            startActivity(intent);
        });
    }
    private void applyFilters() {
        filterQuery = new StringBuilder("SELECT * FROM student_details WHERE ");

        if (isPaidChecked) {
            filterQuery.append("fee_payment = 'PAID' AND ");
        }

        if (isUnPaidChecked) {
            filterQuery.append("fee_payment = 'UNPAID' AND ");
        }

        if (isACChecked) {
            filterQuery.append("room_type = 'AC' AND ");
        }

        if (isNonACChecked) {
            filterQuery.append("room_type = 'NON-AC' AND ");
        }

        int queryLength = filterQuery.length();
        if (queryLength > 0 && filterQuery.substring(queryLength - 4).equals("AND ")) {
            filterQuery.delete(queryLength - 4, queryLength);
        }

        if (isAnyCheckboxChecked()) {
            Cursor filteredCursor = databaseConnect.rawQuery(filterQuery.toString(), null);

            if (filteredCursor.moveToFirst()) {
                StringBuilder allRecords = new StringBuilder();
                allRecords.append(String.format("%-8s %-8s %-20s %-12s\n", "ID", "Room", "Name", "Fee Details"));

                do {
                    String recordName = filteredCursor.getString(filteredCursor.getColumnIndexOrThrow("student_name"));
                    if (!recordName.equals("Admin001")) {
                        allRecords
                                .append(filteredCursor.getString(filteredCursor.getColumnIndexOrThrow("ID"))).append(" - ")
                                .append(filteredCursor.getString(filteredCursor.getColumnIndexOrThrow("room_number"))).append(" - ")
                                .append(recordName).append(" - ")
                                .append(filteredCursor.getString(filteredCursor.getColumnIndexOrThrow("fee_payment"))).append(" - ")
                                .append(filteredCursor.getString(filteredCursor.getColumnIndexOrThrow("payment_date")))
                                .append("\n");
                    }
                } while (filteredCursor.moveToNext());

                filteredCursor.close();
                allRecordView.setText(allRecords.toString());
                allRecordView.setVisibility(View.VISIBLE);
            } else {
                filteredCursor.close();

                if (isAnyCheckboxChecked()) {
                    allRecordView.setText(defaultText); // Set default message
                    allRecordView.setVisibility(View.VISIBLE);
                } else {
                    // No checkbox is checked and no records found with filters
                    Cursor allRecordsCursor = databaseConnect.rawQuery("SELECT * FROM student_details", null);

                    if (allRecordsCursor.moveToFirst()) {
                        StringBuilder allRecords = new StringBuilder();
                        allRecords.append(String.format("%-8s %-8s %-20s %-12s\n", "ID", "Room", "Name", "Fee Details"));

                        do {
                            String recordName = allRecordsCursor.getString(allRecordsCursor.getColumnIndexOrThrow("student_name"));
                            if (!recordName.equals("Admin001")) {
                                allRecords
                                        .append(allRecordsCursor.getString(allRecordsCursor.getColumnIndexOrThrow("ID"))).append(" - ")
                                        .append(allRecordsCursor.getString(allRecordsCursor.getColumnIndexOrThrow("room_number"))).append(" - ")
                                        .append(recordName).append(" - ")
                                        .append(allRecordsCursor.getString(allRecordsCursor.getColumnIndexOrThrow("fee_payment"))).append(" - ")
                                        .append(allRecordsCursor.getString(allRecordsCursor.getColumnIndexOrThrow("payment_date")))
                                        .append("\n");
                            }
                        } while (allRecordsCursor.moveToNext());

                        allRecordsCursor.close();
                        allRecordView.setText(allRecords.toString());
                        allRecordView.setVisibility(View.VISIBLE);
                    } else {
                        allRecordsCursor.close();
                        allRecordView.setText(defaultText); // Set default message
                        allRecordView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
    private boolean isAnyCheckboxChecked() {
        return isPaidChecked || isUnPaidChecked || isACChecked || isNonACChecked;
    }

}
