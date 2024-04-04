package com.proj.hostelmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


public class DatabaseConnect extends SQLiteOpenHelper {
    private static final String DB_NAME = "details";

    public static final String roomDetails = "roomDetails";
    public static final String floorDetails = "floorDetails";
    public static final String StudentTable = "student_details";

    public DatabaseConnect(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //create floor database to gather number of floors and its description if necessary
        sqLiteDatabase.execSQL("" +
                "CREATE TABLE IF NOT EXISTS " + floorDetails +
                        "(floorNumber INT PRIMARY KEY, " +
                        "floorDesc TEXT" +
                ")"
        );

        //create and link floors for room database
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + roomDetails +
                        " (floorNumber INT REFERENCES " + floorDetails + "(floorNumber), " +
                        "roomNumber INT PRIMARY KEY, " +
                        "roomCapacity INT NOT NULL," +
                        " roomType TEXT NOT NULL" +
                        ")"
        );

        //create table student_details and link room numbers using admin module
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + StudentTable +
                        "(ID TEXT PRIMARY KEY, " +
                        "student_name TEXT NOT NULL, " +
                        "parent_name TEXT NOT NULL, " +
                        "contact_number TEXT NOT NULL UNIQUE, " +
                        "room_type TEXT NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "room_number INT REFERENCES " + roomDetails + "(roomNumber), " +
                        "fee_payment TEXT DEFAULT 'UNPAID', " +
                        "payment_date TEXT" +
                        ")"
        );

        sqLiteDatabase.execSQL("" +
                "INSERT INTO " + StudentTable + " " +
                "(ID, student_name, parent_name, contact_number, room_type, " +
                "password) VALUES " +
                "('001', 'Admin001', 'Admin', '1234567890', '--', " +
                "'7fcf4ba391c48784edde599889d6e3f1e47a27db36ecc050cc92f259bfac38afad2c68a1ae804d77075e8fb722503f3eca2b2c1006ee6f6c7b7628cb45fffd1d')"
        );

    }

    //insert floors
    public void insertFloors(String floorNumber, String floorDesc) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, floorDetails);
        if (count == 0) {
            for(long i = 1; i <= Integer.parseInt(floorNumber) + count; i++){

                contentValues.put("floorNumber", i);
                contentValues.put("floorDesc", floorDesc);

                sqLiteDatabase.insert(floorDetails, null, contentValues);
            }

        }else {
            for(long i = count; i <= Integer.parseInt(floorNumber) + count; i++) {
                contentValues.put("floorNumber", i);
                contentValues.put("floorDesc", floorDesc);

                sqLiteDatabase.insert(floorDetails, null, contentValues);
            }

        }

        sqLiteDatabase.close();

    }

    public void insertRooms(int floorNumber, int roomCount, int roomCap, String roomType) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String floorSelection = "floorNumber = ?";
        String[] floorArgs = {String.valueOf(floorNumber)};
        long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, roomDetails, floorSelection, floorArgs);

        int floorMultiplier = floorNumber * 100; // Calculate the floor multiplier based on the floor number

        for (int i = 1; i <= roomCount; i++) {
            int roomNumber = floorMultiplier + i + (int) count; // Calculate the room number based on the floor multiplier, iteration, and existing room count
            contentValues.put("floorNumber", floorNumber);
            contentValues.put("roomNumber", roomNumber);
            contentValues.put("roomCapacity", roomCap);
            contentValues.put("roomType", roomType);
            sqLiteDatabase.insert(roomDetails, null, contentValues);
            contentValues.clear(); // Clear the content values for the next iteration
        }

        sqLiteDatabase.close();
    }

    //inserts student data
    public boolean insertStudentData(String id,
                               String studentName, String parentName,
                               String contactNumber, String roomType, String password) {

        //open database in writable mode to insert data
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        //Create contentValues; a string-builder-like function
        ContentValues contentValues = new ContentValues();

        contentValues.put("ID", id);
        contentValues.put("student_name", studentName);
        contentValues.put("parent_name", parentName);
        contentValues.put("contact_number", contactNumber);
        contentValues.put("room_type", roomType);
        contentValues.put("password", password);

        //insert data into table
        long res = sqLiteDatabase.insert(StudentTable, null, contentValues);

        sqLiteDatabase.close();
        return res != -1;

    }

    public Cursor getAllFloors() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + floorDetails;
        return db.rawQuery(query, null);
    }

    public Cursor getAllRoomCount() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM " + roomDetails;
        return sqLiteDatabase.rawQuery(query, null);
    }

    public int getRoomCountPerType(String roomType) {
        SQLiteDatabase sqLiteDatabase = null;
        Cursor cursor = null;
        int roomCount = 0;

        try {
            sqLiteDatabase = this.getReadableDatabase();

            String[] projection = { "COUNT(*)" };
            String selection = "roomType = ? AND roomCapacity >= ?";
            String[] selectionArgs = { roomType, "1" };

            cursor = sqLiteDatabase.query("roomDetails", projection, selection, selectionArgs, null, null, null);

            if (cursor.moveToFirst()) {
                roomCount = cursor.getInt(0);
            }
        } catch (SQLiteException e) {
            // Handle the exception appropriately (e.g., log, throw, or return an error code)
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        }

        return roomCount;
    }

    public int getRoomCountForFloor(String floorNumber) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String[] projection = { "COUNT(*)" };
        String selection = "floorNumber = ?";
        String[] selectionArgs = { floorNumber };

        Cursor cursor = sqLiteDatabase.query("roomDetails", projection, selection, selectionArgs, null, null, null);
        int roomCount = 0;
        if (cursor.moveToFirst()) {
            roomCount = cursor.getInt(0);
        }
        cursor.close();

        return roomCount;
    }

    public Cursor getAvailableRooms() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM " + roomDetails + " WHERE roomCapacity > 0";
        return sqLiteDatabase.rawQuery(query, null);
    }

    //function that counts total number of records
    public long getAllProfilesCount() {

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT student_name from " + StudentTable +
                " WHERE student_name NOT LIKE 'Admin001'", null);

        long count = cursor.getCount();
        cursor.close();
        sqLiteDatabase.close ();
        return count;

    }

    public long passwordMatch(String id, String match) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + StudentTable + " WHERE ID = ? AND password = ? ", new String[]{id, match});
        long count = cursor.getCount();
        cursor.close();
        sqLiteDatabase.close();
        return count;
    }

    public Cursor getStudentCount() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        return sqLiteDatabase.rawQuery("SELECT * FROM " + StudentTable +
                " WHERE room_number IS NOT NULL ", null);
    }

    public Cursor getStudentRecord() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM " + StudentTable +
                " WHERE room_number IS NULL AND student_name NOT LIKE 'Admin001'", null);
    }

    public Cursor getStudentVerified(String id) {
        if (id == null) {
            return null; // or handle the null case appropriately
        }

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT " + StudentTable + ".*, roomDetails.roomNumber, roomDetails.floorNumber FROM " + StudentTable +
                        " INNER JOIN roomDetails ON " + StudentTable + ".room_number = roomDetails.roomNumber" +
                        " WHERE ID = ?",
                new String[]{id});
    }



    public Cursor getStudentRoomRecord() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM " + StudentTable +
                " WHERE room_number IS NOT NULL AND student_name NOT LIKE 'Admin001'", null);
    }
    public Cursor getRoomNumbersByPreference(String roomPreference) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        String[] projection = {"roomNumber"};
        String selection = "roomType LIKE ? AND roomCapacity > 0";
        String[] selectionArgs = {roomPreference};

        return sqLiteDatabase.query(roomDetails, projection, selection, selectionArgs, null, null, "floorNumber");
    }


    public String getStudentName(String id, String password) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String name = null;
        try {
            Cursor c;
            c = sqLiteDatabase.rawQuery("SELECT student_name from " + StudentTable +
                    " WHERE ID = ? AND password = ? ", new String[]{id, password});
            if (c.getCount() == 1) {
                c.moveToFirst();
                name = c.getString(0);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public Cursor getRoomsByFloor(String floorNumber) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {"roomNumber, roomType"};
        String selection = "floorNumber = ?";
        String[] selectionArgs = {floorNumber};

        return db.query(roomDetails, projection, selection, selectionArgs, null, null, null);
    }
    public Cursor getStudentByRoom(int roomNumber) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                "ID",
                "student_name",
                "contact_number"
        };

        String selection = "room_number=?";
        String[] selectionArgs = { String.valueOf(roomNumber) };

        return db.query(StudentTable, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor fetchStudentAccount(String ID, String password) {
//for logins
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        return sqLiteDatabase.query(StudentTable, new String[]{"ID", "password", "student_name"}, "ID = ? AND password = ?", new String[]{ID, password}, null, null, null);
    }

    public void deleteFloor(int floorNumber, Context context) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        // Check if any rooms on the floor have students allotted
        String query = "SELECT COUNT(*) FROM " + StudentTable +
                " INNER JOIN " + roomDetails +
                " ON " + StudentTable + ".room_number = " + roomDetails + ".roomNumber" +
                " WHERE " + roomDetails + ".floorNumber = ?";
        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(floorNumber)});
        int studentCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            studentCount = cursor.getInt(0);
        }
        if (cursor != null) {
            cursor.close();
        }

        if (studentCount > 0) {
            // Students are allotted rooms on this floor, display toast message
            Toast.makeText(context, "Cannot delete floor. Students are allotted rooms on this floor.", Toast.LENGTH_SHORT).show();
            sqLiteDatabase.close();
            return;
        }

        // Delete the rooms associated with the floor
        sqLiteDatabase.delete(roomDetails, "floorNumber = ?", new String[]{String.valueOf(floorNumber)});

        // Delete the floor
        sqLiteDatabase.delete(floorDetails, "floorNumber = ?", new String[]{String.valueOf(floorNumber)});

        sqLiteDatabase.close();
    }

    public long allotRoom(String studentID, int roomNumber) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        // Update the room number for the student
        ContentValues contentValues = new ContentValues();
        contentValues.put("room_number", roomNumber);
        int rowsAffected = sqLiteDatabase.update(StudentTable, contentValues, "ID = ?", new String[]{studentID});

        // Decrease the roomCapacity in the roomDetails table
        Cursor roomCursor = sqLiteDatabase.rawQuery("SELECT roomCapacity FROM " + roomDetails + " WHERE roomNumber = ?", new String[]{String.valueOf(roomNumber)});
        if (roomCursor != null && roomCursor.moveToFirst()) {
            int roomCapacity = roomCursor.getInt(roomCursor.getColumnIndexOrThrow("roomCapacity"));
            roomCapacity--;

            ContentValues capacityValues = new ContentValues();
            capacityValues.put("roomCapacity", roomCapacity);
            sqLiteDatabase.update(roomDetails, capacityValues, "roomNumber = ?", new String[]{String.valueOf(roomNumber)});
        }

        if (roomCursor != null) {
            roomCursor.close();
        }

        sqLiteDatabase.close();
        return rowsAffected;
    }
    public boolean deAllotRoom(String studentID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = null;
        Cursor roomCursor = null;
        int roomNumber = -1;

        try {
            // Get the current room number for the student
            String roomNumberQuery = "SELECT room_number FROM " + StudentTable + " WHERE ID = ?";
            cursor = sqLiteDatabase.rawQuery(roomNumberQuery, new String[]{studentID});
            if (cursor != null && cursor.moveToFirst()) {
                roomNumber = cursor.getInt(cursor.getColumnIndexOrThrow("room_number"));
            }

            // Update the room number for the student to null
            ContentValues contentValues = new ContentValues();
            contentValues.put("room_number", (String) null);
            int rowsAffected = sqLiteDatabase.update(StudentTable, contentValues, "ID = ?", new String[]{studentID});

            // Decrease the roomCapacity in the roomDetails table
            if (roomNumber != -1) {
                roomCursor = sqLiteDatabase.rawQuery("SELECT * FROM " + roomDetails + " WHERE roomNumber = " + roomNumber, null);
                int roomCapacity = 0;
                if (roomCursor != null && roomCursor.moveToFirst()) {
                    roomCapacity = roomCursor.getInt(roomCursor.getColumnIndexOrThrow("roomCapacity"));
                }
                ContentValues capacityValues = new ContentValues();
                capacityValues.put("roomCapacity", roomCapacity + 1);
                sqLiteDatabase.update(roomDetails, capacityValues, "roomNumber = ?", new String[]{String.valueOf(roomNumber)});
            }

            return rowsAffected > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (roomCursor != null) {
                roomCursor.close();
            }
            sqLiteDatabase.close();
        }
    }

    public Cursor getStudentInfo(String id) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM " + StudentTable + " WHERE ID = ? ", new String[]{id});
    }

    public void updateFeePayments(String id) {
        Date currentDate = new Date();

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("fee_payment", "PAID");
        contentValues.put("payment_date", formattedDate);
        sqLiteDatabase.update(StudentTable, contentValues, "ID = ? ", new String[]{id});
    }

    public void updatePassword(String id, String old_password, String new_password) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("password", new_password);
        sqLiteDatabase.update(StudentTable, contentValues, "ID = ? AND password = ?", new String[]{id, old_password});
    }

    public void updateStudentDetails(String id, String name, String contact) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("student_name", name);
        contentValues.put("contact_number", contact);
        sqLiteDatabase.update(StudentTable, contentValues, "ID = ? ", new String[]{id});
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + StudentTable);
        db.execSQL("DROP TABLE IF EXISTS " + roomDetails);
        db.execSQL("DROP TABLE IF EXISTS " + floorDetails);

        onCreate(db);

    }

    public Cursor rawQuery(String query, String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(query, selectionArgs);
    }
}
