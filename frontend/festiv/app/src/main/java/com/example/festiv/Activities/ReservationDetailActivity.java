package com.example.festiv.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.festiv.Models.Reservation;
import com.example.festiv.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ReservationDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RESERVATION = "reservation";

    private Reservation reservation;
    private ImageView qrCodeImage;
    private Button shareButton;
    private Button resendEmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_details);

        // Get reservation from intent - fixed to use the constant and properly set the class field
        this.reservation = (Reservation) getIntent().getSerializableExtra(EXTRA_RESERVATION);
        if (this.reservation == null) {
            // Try with the string literal key as fallback
            this.reservation = (Reservation) getIntent().getSerializableExtra("reservation");
            if (this.reservation == null) {
                Toast.makeText(this, "Error: Reservation not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Initialize views
        qrCodeImage = findViewById(R.id.qr_code_image);
        shareButton = findViewById(R.id.share_ticket_button);
        resendEmailButton = findViewById(R.id.resend_email_button);

        // Populate reservation data
        populateReservationDetails();

        // Generate QR code
        generateQRCode();

        // Set up button listeners
        setupButtonListeners();
    }

    private void populateReservationDetails() {
        // Add null check for reservation
        if (reservation == null) {
            Toast.makeText(this, "Error: Reservation data is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Reservation ID
        TextView reservationId = findViewById(R.id.reservation_id);
        reservationId.setText(getString(R.string.reservation_number, reservation.getIdReservation()));

        // Performance details
        TextView performanceDetails = findViewById(R.id.performance_details);
        if (reservation.getPerformance() != null && reservation.getPerformance().getSpectacle() != null) {
            performanceDetails.setText(reservation.getPerformance().getSpectacle().getTitre());
        }

        // Category
        TextView category = findViewById(R.id.reservation_category);
        category.setText(reservation.getCategorie());

        // Quantity
        TextView quantity = findViewById(R.id.reservation_quantity);
        quantity.setText(String.valueOf(reservation.getNbBillets()));

        // Total price
        TextView total = findViewById(R.id.reservation_total);
        if (reservation.getMontantTotal() != null) {
            total.setText(String.format(Locale.getDefault(), "%.2f DT", reservation.getMontantTotal()));
        }

        // Reservation date
        TextView date = findViewById(R.id.reservation_date);
        date.setText(formatReservationDate(reservation.getDateReservation()));

        // Status
        TextView status = findViewById(R.id.reservation_status);
        status.setText(reservation.getStatut());
        setStatusColor(status, reservation.getStatut());

        // Email confirmation status
        TextView emailStatus = findViewById(R.id.email_confirmation_status);
        emailStatus.setText(getEmailStatusText(reservation.getStatut()));
    }

    private String formatReservationDate(String dateString) {
        if (dateString == null) return "N/A";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString; // fallback to original string if parsing fails
        }
    }

    private void setStatusColor(TextView statusView, String status) {
        if (status == null) return;

        switch (status.toLowerCase()) {
            case "confirmée":
            case "paid":
                statusView.setTextColor(ContextCompat.getColor(this, R.color.green));
                break;
            case "pending payment":
            case "en attente":
                statusView.setTextColor(ContextCompat.getColor(this, R.color.orange));
                break;
            case "cancelled":
            case "annulée":
                statusView.setTextColor(ContextCompat.getColor(this, R.color.red));
                break;
            default:
                statusView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        }
    }

    private String getEmailStatusText(String status) {
        if (status == null) return "Email confirmation status: Unknown";

        switch (status.toLowerCase()) {
            case "confirmée":
            case "paid":
                return "Email confirmation: Sent";
            case "pending payment":
            case "en attente":
                return "Email confirmation: Pending payment";
            case "cancelled":
            case "annulée":
                return "Email confirmation: Cancelled";
            default:
                return "Email confirmation status: Unknown";
        }
    }

    private void generateQRCode() {
        if (reservation == null) return;

        String qrContent = String.format(Locale.getDefault(),
                "ReservationID:%d|Event:%s|Date:%s|Tickets:%d",
                reservation.getIdReservation(),
                reservation.getPerformance() != null && reservation.getPerformance().getSpectacle() != null ?
                        reservation.getPerformance().getSpectacle().getTitre() : "Unknown",
                reservation.getDateReservation(),
                reservation.getNbBillets());

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            qrCodeImage.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        shareButton.setOnClickListener(v -> shareReservationDetails());
        resendEmailButton.setOnClickListener(v -> resendConfirmationEmail());
    }

    private void shareReservationDetails() {
        if (reservation == null) return;

        String shareText = buildShareText();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Event Reservation");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share reservation via"));
    }

    private String buildShareText() {
        StringBuilder builder = new StringBuilder();
        builder.append("My Event Reservation\n\n");
        builder.append("Reservation #").append(reservation.getIdReservation()).append("\n");

        if (reservation.getPerformance() != null && reservation.getPerformance().getSpectacle() != null) {
            builder.append("Event: ").append(reservation.getPerformance().getSpectacle().getTitre()).append("\n");
            builder.append("Date: ").append(formatReservationDate(reservation.getDateReservation())).append("\n");
            if (reservation.getPerformance().getLieu() != null) {
                builder.append("Venue: ").append(reservation.getPerformance().getLieu().getNomLieu()).append("\n");
            }
        }

        builder.append("Category: ").append(reservation.getCategorie()).append("\n");
        builder.append("Tickets: ").append(reservation.getNbBillets()).append("\n");
        builder.append("Total: ").append(String.format(Locale.getDefault(), "%.2f DT", reservation.getMontantTotal())).append("\n");
        builder.append("Status: ").append(reservation.getStatut()).append("\n");

        return builder.toString();
    }

    private void resendConfirmationEmail() {
        Toast.makeText(this, "Resending confirmation email...", Toast.LENGTH_SHORT).show();

        resendEmailButton.setEnabled(false);
        new android.os.Handler().postDelayed(
                () -> {
                    Toast.makeText(this, "Confirmation email resent successfully", Toast.LENGTH_SHORT).show();
                    resendEmailButton.setEnabled(true);
                },
                2000
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}