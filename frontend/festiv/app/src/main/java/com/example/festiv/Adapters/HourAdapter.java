package com.example.festiv.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.festiv.Models.Performance;
import com.example.festiv.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder> {

    private List<String> hoursList;
    private int selectedPosition = -1;
    private final OnHourClickListener listener;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private List<Performance> performances;

    public void setPerformanceList(List<Performance> performances) {
        this.performances = performances;
        notifyDataSetChanged();
    }

    public interface OnHourClickListener {
        void onHourClick(String hour, int position);
    }

    public HourAdapter(OnHourClickListener listener) {
        this.hoursList = new ArrayList<>();
        this.performances = new ArrayList<>();
        this.listener = listener;
    }

    public void setHours(List<String> hours) {
        this.hoursList = hours;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;
        if (previousSelected >= 0) {
            notifyItemChanged(previousSelected);
        }
        if (selectedPosition >= 0) {
            notifyItemChanged(selectedPosition);
        }
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hour, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        // Set time text
        String timeText = hoursList.get(position);
        holder.hourTextView.setText(timeText);

        // Set location text if available
        if (position < performances.size()) {
            Performance performance = performances.get(position);
            // Debug performance venue info
            Log.d("HourAdapter", "Performance at position " + position +
                    ", ID: " + performance.getIdPerformance());

            // Always make location text visible
            holder.locationTextView.setVisibility(View.VISIBLE);

            if (performance != null && performance.getLieu() != null) {
                String ville = performance.getLieu().getVille();

                // Always show the city name if available
                if (ville != null && !ville.isEmpty() &&
                        !ville.equals("Loading...") &&
                        !ville.equals("Location TBD")) {
                    holder.locationTextView.setText(ville);
                } else {
                    holder.locationTextView.setText("Location TBD");
                }
            } else {
                holder.locationTextView.setText("Location TBD");
            }
        } else {
            holder.locationTextView.setVisibility(View.GONE);
        }

        // Set selection state
        int bgColor = position == selectedPosition ?
                R.color.colorAccent : R.color.card_background;
        holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), bgColor));

        // Set click listener with error handling
        holder.itemView.setOnClickListener(v -> {
            try {
                setSelectedPosition(position);
                listener.onHourClick(timeText, position);
            } catch (Exception e) {
                Log.e("HourAdapter", "Error on hour click: " + e.getMessage());
                e.printStackTrace();
                // Show error toast to provide feedback to user
                Toast.makeText(holder.itemView.getContext(),
                        "Error selecting time slot. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return hoursList.size();
    }

    static class HourViewHolder extends RecyclerView.ViewHolder {
        TextView hourTextView;
        TextView locationTextView;
        CardView cardView;

        HourViewHolder(View itemView) {
            super(itemView);
            hourTextView = itemView.findViewById(R.id.hourTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            cardView = (CardView) itemView;
        }
    }
}