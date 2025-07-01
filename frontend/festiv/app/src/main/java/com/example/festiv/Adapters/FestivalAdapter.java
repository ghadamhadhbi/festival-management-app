package com.example.festiv.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.festiv.R;
import com.example.festiv.Activities.FestivalDetailActivity;
import com.example.festiv.Models.Spectacle;

import java.util.ArrayList;
import java.util.List;

public class FestivalAdapter extends RecyclerView.Adapter<FestivalAdapter.FestivalViewHolder> {

    private final List<Spectacle> spectacles;
    private final Context context;
    private final OnSaveClickListener onSaveClickListener;

    public interface OnSaveClickListener {
        void onSaveClick(Spectacle spectacle, int position);
    }

    public FestivalAdapter(List<Spectacle> spectacles, Context context, OnSaveClickListener listener) {
        this.spectacles = spectacles;
        this.context = context;
        this.onSaveClickListener = listener;
    }

    @NonNull
    @Override
    public FestivalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_festival, parent, false);
        return new FestivalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FestivalViewHolder holder, int position, List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.get(0) instanceof Boolean) {
            // Just update the bookmark icon
            updateBookmarkIcon(holder.btnSave, (Boolean) payloads.get(0));
        } else {
            // Full bind
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FestivalViewHolder holder, int position) {
        Spectacle spectacle = spectacles.get(position);

        // Set performance dates range
        String startDate = spectacle.getFirstPerformanceDateFormatted();
        String endDate = spectacle.getLastPerformanceDateFormatted();

        if (startDate.equals(endDate)) {
            holder.dates.setText(startDate); // Single date
        } else {
            holder.dates.setText(startDate + " - " + endDate); // Date range
        }

        // Set bookmark icon
        updateBookmarkIcon(holder.btnSave, spectacle.isBookmarked());

        // Set festival title
        holder.title.setText(spectacle.getTitre());

        // Handle image loading
        String imagePath = spectacle.getImageUrl();
        if (imagePath != null && !imagePath.isEmpty()) {
            // Try to get the resource ID
            int imageResId = context.getResources().getIdentifier(
                    imagePath, "drawable", context.getPackageName());

            if (imageResId != 0) {
                // Load image using Glide
                Glide.with(context)
                        .load(imageResId)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(holder.image);
            } else {
                // If it's a URL, load directly
                Glide.with(context)
                        .load(imagePath)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(holder.image);
            }
        } else {
            // Fallback to placeholder
            Glide.with(context)
                    .load(R.drawable.placeholder_image)
                    .into(holder.image);
        }

        // Set button click listener for details
        holder.btnDetails.setOnClickListener(v -> {
            try {
                if (spectacle != null) {
                    // Pass essential data (consider using Parcelable instead of Serializable for better performance)
                    // In FestivalAdapter.java - simplify intent passing
                    Intent intent = new Intent(context, FestivalDetailActivity.class);
                    intent.putExtra("spectacle_id", spectacle.getIdSpec()); // Just pass the ID

// OR if you want to keep passing the whole object:
                    intent.putExtra("spectacle", spectacle);
// But don't pass performances separately

                    // Only pass performances if they exist
                    if (spectacle.getPerformances() != null) {
                        intent.putExtra("performances", new ArrayList<>(spectacle.getPerformances()));
                    }

                    // Add flags for clean activity launch
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    Log.e("FestivalAdapter", "Spectacle at position " + position + " is null");
                }
            } catch (Exception e) {
                Log.e("FestivalAdapter", "Error starting FestivalDetailActivity", e);
            }
        });
        // Set click listener for bookmark button
        final int adapterPosition = holder.getAdapterPosition();
        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapterPosition != RecyclerView.NO_POSITION && onSaveClickListener != null) {
                    Spectacle spectacle = spectacles.get(adapterPosition);
                    boolean newBookmarkState = !spectacle.isBookmarked();
                    spectacle.setBookmarked(newBookmarkState);
                    updateBookmarkIcon(holder.btnSave, newBookmarkState);
                    onSaveClickListener.onSaveClick(spectacle, adapterPosition);

                    // Add log to verify the click is registered
                    Log.d("FestivalAdapter", "Bookmark clicked for: " + spectacle.getTitre()
                            + " - New state: " + newBookmarkState);
                }
            }
        });
    }

    private void updateBookmarkIcon(ImageButton button, boolean isBookmarked) {
        if (isBookmarked) {
            button.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            button.setImageResource(R.drawable.ic_bookmark);
        }
    }

    public void updateBookmarkStatus(int position, boolean isBookmarked) {
        if (position >= 0 && position < spectacles.size()) {
            notifyItemChanged(position, isBookmarked);
        }
    }

    @Override
    public int getItemCount() {
        return spectacles.size();
    }

    public void updateData(List<Spectacle> newSpectacles) {
        spectacles.clear();
        spectacles.addAll(newSpectacles);
        notifyDataSetChanged();
    }

    static class FestivalViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, dates;
        Button btnDetails;
        ImageButton btnSave;

        public FestivalViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.festivalImage);
            title = itemView.findViewById(R.id.festivalTitle);
            dates = itemView.findViewById(R.id.festivalDates);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}