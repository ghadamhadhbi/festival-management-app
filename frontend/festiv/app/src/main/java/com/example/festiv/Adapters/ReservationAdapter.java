package com.example.festiv.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.festiv.Models.Reservation;
import com.example.festiv.R;

import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private final List<Reservation> reservations;
    private final OnReservationClickListener listener;
    private final OnPaymentActionListener paymentListener;

    public interface OnReservationClickListener {
        void onReservationClick(Reservation reservation);
    }

    public interface OnPaymentActionListener {
        void onPayNowClick(Reservation reservation);
        void onViewReceiptClick(Reservation reservation);
    }

    public ReservationAdapter(List<Reservation> reservations,
                              OnReservationClickListener listener,
                              OnPaymentActionListener paymentListener) {
        this.reservations = reservations;
        this.listener = listener;
        this.paymentListener = paymentListener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);

        // Bind performance/event information
        if (reservation.getPerformance() != null && reservation.getPerformance().getSpectacle() != null) {
            holder.tvEventName.setText(reservation.getPerformance().getSpectacle().getTitre());
            holder.tvEventDate.setText(reservation.getPerformance().getFormattedDateTime());

            if (reservation.getPerformance().getLieu() != null) {
                holder.tvEventLocation.setText(reservation.getPerformance().getLieu().getNomLieu());
            }

            // Load event image if available
            if (reservation.getPerformance().getSpectacle().getImageUrl() != null &&
                    !reservation.getPerformance().getSpectacle().getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(reservation.getPerformance().getSpectacle().getImageUrl())
                        .placeholder(R.drawable.ic_event_placeholder)
                        .into(holder.ivEventImage);
            } else {
                holder.ivEventImage.setImageResource(R.drawable.ic_event_placeholder);
            }
        }

        // Bind reservation details
        holder.tvStatus.setText(reservation.getStatut());
        holder.tvStatus.setTextColor(getStatusColor(reservation.getStatut(), holder.itemView));

        if (reservation.getMontantTotal() != null) {
            holder.tvTotalAmount.setText(String.format(Locale.getDefault(),
                    "%.2f DT", reservation.getMontantTotal()));
        }

        // Payment status and actions
        if ("Pending Payment".equalsIgnoreCase(reservation.getStatut())) {
            holder.tvPaymentAction.setVisibility(View.VISIBLE);
            holder.tvPaymentAction.setText("Pay Now");
            holder.tvPaymentAction.setOnClickListener(v -> {
                if (paymentListener != null) {
                    paymentListener.onPayNowClick(reservation);
                }
            });
        } else if ("Paid".equalsIgnoreCase(reservation.getStatut())) {
            holder.tvPaymentAction.setVisibility(View.VISIBLE);
            holder.tvPaymentAction.setText("View Receipt");
            holder.tvPaymentAction.setOnClickListener(v -> {
                if (paymentListener != null) {
                    paymentListener.onViewReceiptClick(reservation);
                }
            });
        } else {
            holder.tvPaymentAction.setVisibility(View.GONE);
        }

        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReservationClick(reservation);
            }
        });
    }

    private int getStatusColor(String status, View view) {
        Context context = view.getContext();
        switch (status.toLowerCase()) {
            case "paid":
                return ContextCompat.getColor(context, R.color.green_success);
            case "pending payment":
                return ContextCompat.getColor(context, R.color.orange_warning);
            case "cancelled":
                return ContextCompat.getColor(context, R.color.red_error);
            default:
                return ContextCompat.getColor(context, android.R.color.white);
        }
    }

    @Override
    public int getItemCount() {
        return reservations != null ? reservations.size() : 0;
    }

    public void updateData(List<Reservation> newReservations) {
        reservations.clear();
        reservations.addAll(newReservations);
        notifyDataSetChanged();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventImage;
        TextView tvEventName, tvEventDate, tvEventLocation;
        TextView tvStatus, tvTotalAmount, tvPaymentAction;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.iv_event_image);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvPaymentAction = itemView.findViewById(R.id.tv_payment_action);
        }
    }
}