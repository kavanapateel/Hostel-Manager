package com.proj.hostelmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorViewHolder> {
    private Cursor cursor;
    private final DatabaseConnect databaseConnect;
    private final Context context;
    public FloorAdapter(Context context) {
        databaseConnect = new DatabaseConnect(context);
        cursor = databaseConnect.getAllFloors();
        this.context = context;
    }

    @NonNull
    @Override
    public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.floor_item, parent, false);
        return new FloorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            String floorNumber = cursor.getString(cursor.getColumnIndexOrThrow("floorNumber"));
            StringBuilder floorName = new StringBuilder();
            floorName.append("Floor ").append(floorNumber);

            // Get the room count for the current floor
            int roomCount = databaseConnect.getRoomCountForFloor(floorNumber);
            floorName.append(": ").append(roomCount).append(" rooms");

            holder.floorNumberTextView.setText(floorName.toString());

            holder.floorNumberTextView.setOnClickListener(v -> {
                boolean isExpanded = holder.expandedOptionsLayout.getVisibility() == View.VISIBLE;
                holder.expandedOptionsLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            });

            holder.editButton.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, GenerateRoom.class);
                intent.putExtra("FLOOR_ID", floorNumber);
                context.startActivity(intent);

            });

            holder.deleteButton.setOnClickListener(v -> {
                databaseConnect.deleteFloor(Integer.parseInt(floorNumber), context);
                refreshCursor();

            });
        }
    }



    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshCursor() {
        Cursor newCursor = databaseConnect.getAllFloors();
        swapCursor(newCursor);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public static class FloorViewHolder extends RecyclerView.ViewHolder {
        Button editButton, deleteButton;
        TextView floorNumberTextView;
        ViewGroup expandedOptionsLayout;

        public FloorViewHolder(@NonNull View itemView) {
            super(itemView);
            floorNumberTextView = itemView.findViewById(R.id.floorNumberTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            expandedOptionsLayout = itemView.findViewById(R.id.expandedOptionsLayout);
        }
    }
}
