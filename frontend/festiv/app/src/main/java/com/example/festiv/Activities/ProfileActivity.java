package com.example.festiv.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.festiv.Adapters.ReservationAdapter;
import com.example.festiv.Models.Client;
import com.example.festiv.Models.Reservation;
import com.example.festiv.Models.UserSession;
import com.example.festiv.R;
import com.example.festiv.api.ApiService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvReservationsCount;
    private Button btnEditProfile, btnLogout;
    private ImageView profileIcon;
    private RecyclerView rvReservations;
    private ReservationAdapter reservationAdapter;
    private ApiService apiService;
    private Client currentClient;
    private static final int PAYMENT_REQUEST_CODE = 1001;

    // Define constant for intent extra keys
    public static final String EXTRA_RESERVATION = "reservation";
    public static final String EXTRA_RESERVATION_ID = "reservation_id";
    public static final String EXTRA_AMOUNT = "amount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check login first
        if (!UserSession.isLoggedIn(this)) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_profile);
        initializeViews();
        initializeRetrofit();
        loadClientData();
    }

    private void initializeRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        System.out.println("DEBUG: API base URL: http://192.168.7.181:9090/");
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.7.181:9090/") // Change the URL to match your API
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void initializeViews() {
        tvUsername = findViewById(R.id.tv_username);
        System.out.println("DEBUG: tvUsername initialized: " + (tvUsername != null ? "Successfully" : "FAILED"));

        tvEmail = findViewById(R.id.tv_email);
        tvReservationsCount = findViewById(R.id.tv_reservations_count);
        btnLogout = findViewById(R.id.btn_logout);
        profileIcon = findViewById(R.id.profile_icon);
        rvReservations = findViewById(R.id.rv_reservations);

        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        tvUsername.setText("Test Username");
        tvEmail.setText("test@email.com");
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadClientData() {
        Long clientId = UserSession.getUserId(this);
        System.out.println("DEBUG: Retrieved client ID: " + clientId);
        if (clientId == null || clientId <= 0L) {
            redirectToLogin();
            return;
        }

        Call<Client> call = apiService.getClient(clientId);
        call.enqueue(new Callback<Client>() {
            @Override
            public void onResponse(Call<Client> call, Response<Client> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentClient = response.body();
                    runOnUiThread(() -> {
                        loadUserData();
                        loadClientReservations();
                    });
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<Client> call, Throwable t) {
                Toast.makeText(ProfileActivity.this,
                        "Failed to load client data: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load user data into the views
    private void loadUserData() {
        System.out.println("DEBUG: loadUserData called, currentClient: " + (currentClient != null ? "not null" : "NULL"));
        if (currentClient != null) {
            System.out.println("DEBUG: Setting name: " + currentClient.getPrenomclt() + " " + currentClient.getNomclt());
            System.out.println("DEBUG: Setting email: " + currentClient.getEmail());

            tvUsername.setText(currentClient.getPrenomclt() + " " + currentClient.getNomclt());
            tvEmail.setText(currentClient.getEmail());

            // Check if TextView exists and is accessible
            if (tvUsername != null) {
                System.out.println("DEBUG: tvUsername after setText: " + tvUsername.getText());
            } else {
                System.out.println("DEBUG: tvUsername is NULL");
            }
        }
    }

    private void loadClientReservations() {
        if (currentClient == null || currentClient.getIdclt() == null) {
            Toast.makeText(this, "Client information not available", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show progress bar

        Call<List<Reservation>> call = apiService.getClientReservations(currentClient.getIdclt());
        call.enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Reservation> reservations = response.body();
                    if (reservations.isEmpty()) {
                        // Handle empty state
                        tvReservationsCount.setText("0");
                    } else {
                        updateReservationsUI(reservations);
                    }
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this,
                        "Failed to load reservations: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReservationsUI(List<Reservation> reservations) {
        if (reservations != null && !reservations.isEmpty()) {
            tvReservationsCount.setText(String.valueOf(reservations.size()));

            reservationAdapter = new ReservationAdapter(
                    reservations,
                    reservation -> {
                        // Verify reservation is not null before proceeding
                        if (reservation != null) {
                            Intent intent = new Intent(ProfileActivity.this, ReservationDetailActivity.class);
                            // Use consistently named extras
                            intent.putExtra(EXTRA_RESERVATION, reservation);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ProfileActivity.this,
                                    "Cannot open reservation details: Reservation data is unavailable",
                                    Toast.LENGTH_SHORT).show();
                        }
                    },
                    new ReservationAdapter.OnPaymentActionListener() {
                        @Override
                        public void onPayNowClick(Reservation reservation) {
                            // Handle payment initiation
                            if (reservation != null) {
                                initiatePayment(reservation);
                            } else {
                                Toast.makeText(ProfileActivity.this,
                                        "Cannot process payment: Reservation data is unavailable",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onViewReceiptClick(Reservation reservation) {
                            // Handle receipt viewing
                            if (reservation != null) {
                                viewReceipt(reservation);
                            } else {
                                Toast.makeText(ProfileActivity.this,
                                        "Cannot view receipt: Reservation data is unavailable",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

            rvReservations.setAdapter(reservationAdapter);
        } else {
            tvReservationsCount.setText("0");
            // Optionally show empty state UI
        }
    }

    private void initiatePayment(Reservation reservation) {
        // Implement your payment flow here
        Intent paymentIntent = new Intent(ProfileActivity.this, ReservationDetailActivity.class);
        // Pass the complete reservation object
        paymentIntent.putExtra(EXTRA_RESERVATION, reservation);
        // Also pass individual fields as a backup
        if (reservation.getIdReservation() != null) {
            paymentIntent.putExtra(EXTRA_RESERVATION_ID, reservation.getIdReservation());
        }
        if (reservation.getMontantTotal() != null) {
            paymentIntent.putExtra(EXTRA_AMOUNT, reservation.getMontantTotal());
        }
        paymentIntent.putExtra("action", "payment");
        startActivityForResult(paymentIntent, PAYMENT_REQUEST_CODE);
    }

    private void viewReceipt(Reservation reservation) {
        // Implement receipt viewing
        Intent receiptIntent = new Intent(ProfileActivity.this, ReservationDetailActivity.class);
        // Pass the complete reservation object
        receiptIntent.putExtra(EXTRA_RESERVATION, reservation);
        // Also pass ID as a backup
        if (reservation.getIdReservation() != null) {
            receiptIntent.putExtra(EXTRA_RESERVATION_ID, reservation.getIdReservation());
        }
        receiptIntent.putExtra("action", "receipt");
        startActivity(receiptIntent);
    }

    // Handle payment result if needed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refresh reservations after successful payment
                loadClientReservations();
                Toast.makeText(this, "Payment processed successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.putExtra("redirect_to_profile", true);
        startActivity(loginIntent);
        finish(); // Add this to prevent going back to profile screen
    }

    private void logoutUser() {
        UserSession.clearSession(this);
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void handleApiError(Response<?> response) {
        try {
            String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Toast.makeText(this,
                    "Error: " + response.code() + " - " + error,
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error reading response", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UserSession.isLoggedIn(this)) {
            loadClientData();
        } else {
            redirectToLogin();
        }
    }
}