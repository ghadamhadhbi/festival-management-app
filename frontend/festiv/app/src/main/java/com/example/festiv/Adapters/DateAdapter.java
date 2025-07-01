package com.example.festiv.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.festiv.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<String> datesList;
    private int selectedPosition = -1;
    private final OnDateClickListener listener;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

    public interface OnDateClickListener {
        void onDateClick(String date, int position);
    }

    public DateAdapter(OnDateClickListener listener) {
        this.datesList = new ArrayList<>();
        this.listener = listener;
    }

    public void setDates(List<String> dates) {
        this.datesList = dates;
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
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String dateString = datesList.get(position);
        try {
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                holder.dayTextView.setText(dayFormat.format(date));
                holder.monthTextView.setText(monthFormat.format(date).toUpperCase());
            }
        } catch (ParseException e) {
            Log.e("DateAdapter", "Error parsing date: " + dateString, e);
            holder.dayTextView.setText("--");
            holder.monthTextView.setText("---");
        }

        // Update appearance based on selection state
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_background));
        }

        holder.itemView.setOnClickListener(v -> {
            setSelectedPosition(position);
            listener.onDateClick(dateString, position);
        });
    }

    @Override
    public int getItemCount() {
        return datesList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;
        TextView monthTextView;
        CardView cardView;

        DateViewHolder(View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            monthTextView = itemView.findViewById(R.id.monthTextView);
            cardView = (CardView) itemView;
        }
    }
}