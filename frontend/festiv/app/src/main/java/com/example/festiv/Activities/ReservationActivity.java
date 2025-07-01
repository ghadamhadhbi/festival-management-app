package com.example.festiv.Activities;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.festiv.Models.Client;
import com.example.festiv.Models.Performance;
import com.example.festiv.Models.Reservation;
import com.example.festiv.Models.ReservationRequest;
import com.example.festiv.Models.ReservationResponse;
import com.example.festiv.Models.Spectacle;
import com.example.festiv.Models.UserSession;
import com.example.festiv.R;
import com.example.festiv.api.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ReservationActivity extends AppCompatActivity {

    private static final String TAG = "ReservationActivity";  // Add TAG for logging
    private static final long GUEST_USER_ID = 0L; // Default ID for anonymous users
    private ScrollView paymentFormContainer;

    private EditText goldQuantity, silverQuantity, bronzeQuantity;
    private RadioButton goldTicket, silverTicket, bronzeTicket;
    private Button reserveButton;
    private TextView totalPrice;
    private ApiService apiService;
    private Performance selectedPerformance;
    private ImageView seatingPreview;

    private LinearLayout cardInfoContainer;
    private LinearLayout paypalInfoContainer;
    private LinearLayout tunisianPaymentContainer;
    private RadioGroup paymentMethod;

    // Add default values for prices in case they're null in the Spectacle object
    private static final BigDecimal DEFAULT_GOLD_PRICE = new BigDecimal("100.00");
    private static final BigDecimal DEFAULT_SILVER_PRICE = new BigDecimal("50.00");
    private static final BigDecimal DEFAULT_BRONZE_PRICE = new BigDecimal("30.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        Log.d(TAG, "Activity created");
        setupBottomNavigation();
        initializeViews();
        initializeRetrofit();
        getPerformanceFromIntent();
        setupListeners();

        // Initially update the price display
        updatePrice();

        // Debug log prices from spectacle
        if (selectedPerformance != null && selectedPerformance.getSpectacle() != null) {
            Spectacle spectacle = selectedPerformance.getSpectacle();
            Log.d(TAG, "Spectacle prices: " +
                    "Gold=" + (spectacle.getPrixGold() != null ? spectacle.getPrixGold() : "null") + ", " +
                    "Silver=" + (spectacle.getPrixSilver() != null ? spectacle.getPrixSilver() : "null") + ", " +
                    "Normal=" + (spectacle.getPrixNormal() != null ? spectacle.getPrixNormal() : "null"));
        } else {
            Log.e(TAG, "Cannot access spectacle or its prices");
        }
    }

    private void initializeViews() {
        goldQuantity = findViewById(R.id.goldQuantity);
        silverQuantity = findViewById(R.id.silverQuantity);
        bronzeQuantity = findViewById(R.id.bronzeQuantity);


        goldTicket = findViewById(R.id.goldTicket);
        silverTicket = findViewById(R.id.silverTicket);
        bronzeTicket = findViewById(R.id.bronzeTicket);


        reserveButton = findViewById(R.id.reserveButton);
        totalPrice = findViewById(R.id.totalPrice);
        seatingPreview = findViewById(R.id.seatingPreview);
        paymentFormContainer = findViewById(R.id.paymentFormContainer);

        // Initialize payment method containers
        cardInfoContainer = findViewById(R.id.cardInfoContainer);
        paypalInfoContainer = findViewById(R.id.paypalInfoContainer);
        tunisianPaymentContainer = findViewById(R.id.tunisianPaymentContainer);
        paymentMethod = findViewById(R.id.paymentMethod);

        // Initialize with default values
        goldQuantity.setText("0");
        silverQuantity.setText("0");
        bronzeQuantity.setText("0");

        // Disable all quantity fields initially until a radio button is selected
        goldQuantity.setEnabled(false);
        silverQuantity.setEnabled(false);
        bronzeQuantity.setEnabled(false);
    }

    private void initializeRetrofit() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.7.181:9090/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_saved) {
                navigateToSaved();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                navigateToProfile();
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void navigateToSaved() {
        if (UserSession.isLoggedIn(this)) {
            Intent savedIntent = new Intent(this, SavedActivity.class);
            startActivity(savedIntent);
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.login_required)
                    .setMessage(R.string.saved_items_login_prompt)
                    .setPositiveButton(R.string.login, (dialog, which) -> {
                        Intent loginIntent = new Intent(this, LoginActivity.class);
                        loginIntent.putExtra("redirect_to_saved", true);
                        startActivity(loginIntent);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void navigateToProfile() {
        if (UserSession.isLoggedIn(this)) {
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            startActivity(profileIntent);
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.login_required)
                    .setMessage(R.string.profile_login_prompt)
                    .setPositiveButton(R.string.login, (dialog, which) -> {
                        Intent loginIntent = new Intent(this, LoginActivity.class);
                        loginIntent.putExtra("redirect_to_profile", true);
                        startActivity(loginIntent);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void setupListeners() {
        // Add info button listeners
        ImageButton goldInfo = findViewById(R.id.goldInfo);
        goldInfo.setOnClickListener(v -> showCategoryInfo("Gold", "Premium seating with the best view"));

        ImageButton silverInfo = findViewById(R.id.silverInfo);
        silverInfo.setOnClickListener(v -> showCategoryInfo("Silver", "Great seating with excellent view"));

        ImageButton bronzeInfo = findViewById(R.id.bronzeInfo);
        bronzeInfo.setOnClickListener(v -> showCategoryInfo("Normal", "Standard seating with good view"));

        // Radio button listeners to make sure only one ticket type can be selected
        goldTicket.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If gold is checked, uncheck others
                silverTicket.setChecked(false);
                bronzeTicket.setChecked(false);

                // Enable gold quantity field, disable others
                goldQuantity.setEnabled(true);
                silverQuantity.setEnabled(false);
                bronzeQuantity.setEnabled(false);

                // Clear other quantities
                silverQuantity.setText("0");
                bronzeQuantity.setText("0");

                // If gold quantity is empty, set to 1
                if (goldQuantity.getText().toString().trim().isEmpty() ||
                        goldQuantity.getText().toString().equals("0")) {
                    goldQuantity.setText("1");
                }

                updatePrice();
            } else if (!silverTicket.isChecked() && !bronzeTicket.isChecked()) {
                // If nothing else is checked, disable the gold quantity field
                goldQuantity.setEnabled(false);
            }
        });

        silverTicket.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If silver is checked, uncheck others
                goldTicket.setChecked(false);
                bronzeTicket.setChecked(false);

                // Enable silver quantity field, disable others
                goldQuantity.setEnabled(false);
                silverQuantity.setEnabled(true);
                bronzeQuantity.setEnabled(false);

                // Clear other quantities
                goldQuantity.setText("0");
                bronzeQuantity.setText("0");

                // If silver quantity is empty, set to 1
                if (silverQuantity.getText().toString().trim().isEmpty() ||
                        silverQuantity.getText().toString().equals("0")) {
                    silverQuantity.setText("1");
                }

                updatePrice();
            } else if (!goldTicket.isChecked() && !bronzeTicket.isChecked()) {
                // If nothing else is checked, disable the silver quantity field
                silverQuantity.setEnabled(false);
            }
        });

        bronzeTicket.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If bronze is checked, uncheck others
                goldTicket.setChecked(false);
                silverTicket.setChecked(false);

                // Enable bronze quantity field, disable others
                goldQuantity.setEnabled(false);
                silverQuantity.setEnabled(false);
                bronzeQuantity.setEnabled(true);

                // Clear other quantities
                goldQuantity.setText("0");
                silverQuantity.setText("0");

                // If bronze quantity is empty, set to 1
                if (bronzeQuantity.getText().toString().trim().isEmpty() ||
                        bronzeQuantity.getText().toString().equals("0")) {
                    bronzeQuantity.setText("1");
                }

                updatePrice();
            } else if (!goldTicket.isChecked() && !silverTicket.isChecked()) {
                // If nothing else is checked, disable the bronze quantity field
                bronzeQuantity.setEnabled(false);
            }
        });

        TextWatcher quantityWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Quantity changed: " + s.toString());
                updatePrice();
            }
        };

        goldQuantity.addTextChangedListener(quantityWatcher);
        silverQuantity.addTextChangedListener(quantityWatcher);
        bronzeQuantity.addTextChangedListener(quantityWatcher);

        reserveButton.setOnClickListener(v -> attemptReservation());
        setupPaymentMethodListeners();
    }

    private void showCategoryInfo(String category, String description) {
        new AlertDialog.Builder(this)
                .setTitle(category + " Ticket")
                .setMessage(description)
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupPaymentMethodListeners() {
        // Handle payment method changes
        paymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            // Show/hide appropriate payment details based on selection
            cardInfoContainer.setVisibility(checkedId == R.id.creditCard ? View.VISIBLE : View.GONE);
            paypalInfoContainer.setVisibility(checkedId == R.id.paypal ? View.VISIBLE : View.GONE);
            tunisianPaymentContainer.setVisibility(
                    (checkedId == R.id.mobiflouss || checkedId == R.id.tunisie_net) ?
                            View.VISIBLE : View.GONE
            );
        });

        // Back button in payment form
        findViewById(R.id.backButton).setOnClickListener(v -> {
            paymentFormContainer.setVisibility(View.GONE);
        });

        // Confirm payment button
        findViewById(R.id.confirmPaymentButton).setOnClickListener(v -> {
            // Process payment - implement payment validation logic here
            // For now, just show success message
            handlePaymentConfirmation();
        });
    }


    private void attemptReservation() {
        if (!validateInput()) return;

        String category = getSelectedCategory();
        int quantity = getSelectedQuantity();

        if (!validateCapacity(category, quantity)) return;

        // Check if user is logged in
        if (UserSession.isLoggedIn(this)) {
            // User is logged in, proceed normally
            makeReservation(UserSession.getUserId(this), null);
            paymentFormContainer.setVisibility(View.VISIBLE);

        } else {
            // User is not logged in, show guest user info form
            showGuestUserForm();
        }
    }

    private void handlePaymentConfirmation() {
        // Implement payment processing logic here
        // For now, just create the reservation
        UserSession session = new UserSession(this);
        Long userId = session.getUserId(this);

        // Make reservation for logged-in user (guest users are handled separately)
        if (userId != null && userId > 0) {
            makeReservation(userId, null);
        }
    }


    private void showGuestUserForm() {
        // Inflate the custom layout
        View formView = LayoutInflater.from(this).inflate(R.layout.dialog_guest_user_form, null);

        // Get references to form fields
        TextInputEditText nameInput = formView.findViewById(R.id.nameInput);
        TextInputEditText lastnameInput = formView.findViewById(R.id.lastnameInput);
        TextInputEditText phoneInput = formView.findViewById(R.id.phoneInput);
        TextInputEditText emailInput = formView.findViewById(R.id.emailInput);

        // Create and show dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Guest Information")
                .setMessage("Please provide your information to complete the reservation")
                .setView(formView)
                .setPositiveButton("Continue", null) // Will set listener later to prevent auto-dismiss
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Override the positive button click to validate input before dismissing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Get values
            String firstName = nameInput.getText().toString().trim();
            String lastName = lastnameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            // Validate inputs
            boolean isValid = true;

            if (firstName.isEmpty()) {
                nameInput.setError("Required");
                isValid = false;
            }

            if (lastName.isEmpty()) {
                lastnameInput.setError("Required");
                isValid = false;
            }

            if (phone.isEmpty() || phone.length() < 8) {
                phoneInput.setError("Valid phone number required");
                isValid = false;
            }

            // Email validation with regex
            if (email.isEmpty() || !isValidEmail(email)) {
                emailInput.setError("Valid email required");
                isValid = false;
            }

            if (isValid) {
                // Create client object
                Client guestClient = new Client(lastName, firstName, phone, email, "");

                // Make reservation with guest client info
                makeReservation(GUEST_USER_ID, guestClient);

                // Dismiss the dialog
                dialog.dismiss();
            }
        });
    }
    // Method to validate email format using regex
    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailPattern);
        return pattern.matcher(email).matches();
    }

    private boolean validateInput() {
        int quantity = getSelectedQuantity();
        if (quantity <= 0) {
            Toast.makeText(this, "Please select at least one ticket", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!goldTicket.isChecked() && !silverTicket.isChecked() && !bronzeTicket.isChecked()) {
            Toast.makeText(this, "Please select a ticket category", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private int getSelectedQuantity() {
        try {
            if (goldTicket.isChecked()) {
                String qty = goldQuantity.getText().toString().trim();
                return qty.isEmpty() ? 0 : Integer.parseInt(qty);
            } else if (silverTicket.isChecked()) {
                String qty = silverQuantity.getText().toString().trim();
                return qty.isEmpty() ? 0 : Integer.parseInt(qty);
            } else if (bronzeTicket.isChecked()) {
                String qty = bronzeQuantity.getText().toString().trim();
                return qty.isEmpty() ? 0 : Integer.parseInt(qty);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing quantity: " + e.getMessage());
            return 0;
        }
        return 0;
    }



    private boolean validateCapacity(String category, int quantity) {
        if (selectedPerformance == null) return false;

        switch (category.toLowerCase()) {
            case "gold":
                if (selectedPerformance.getCapaciteGold() < quantity) {
                    showError("Not enough Gold tickets available. Only " +
                            selectedPerformance.getCapaciteGold() + " remaining.");
                    return false;
                }
                break;
            case "silver":
                if (selectedPerformance.getCapaciteSilver() < quantity) {
                    showError("Not enough Silver tickets available. Only " +
                            selectedPerformance.getCapaciteSilver() + " remaining.");
                    return false;
                }
                break;
            default:
                if (selectedPerformance.getCapaciteNormal() < quantity) {
                    showError("Not enough Normal tickets available. Only " +
                            selectedPerformance.getCapaciteNormal() + " remaining.");
                    return false;
                }
                break;
        }

        return true;
    }

    private double calculateTotalAmount() {
        if (selectedPerformance == null) {
            Log.e(TAG, "Cannot calculate total: Performance is null");
            return 0.0;
        }

        Spectacle spectacle = selectedPerformance.getSpectacle();
        if (spectacle == null) {
            Log.e(TAG, "Cannot calculate total: Spectacle is null");
            return 0.0;
        }

        int quantity = getSelectedQuantity();
        String category = getSelectedCategory();

        BigDecimal pricePerTicket = BigDecimal.ZERO;
        Log.d(TAG, "Calculating price for category: " + category);

        // IMPROVEMENT: Debug logging to show what we're working with
        Log.d(TAG, "Spectacle price info - Gold: " + spectacle.getPrixGold() +
                ", Silver: " + spectacle.getPrixSilver() +
                ", Normal: " + spectacle.getPrixNormal());

        // Use safe getters to handle null prices and provide defaults
        switch (category.toLowerCase()) {
            case "gold":
                pricePerTicket = getPriceWithDefault(spectacle.getPrixGold(), DEFAULT_GOLD_PRICE);
                Log.d(TAG, "Gold price used: " + pricePerTicket);
                break;
            case "silver":
                pricePerTicket = getPriceWithDefault(spectacle.getPrixSilver(), DEFAULT_SILVER_PRICE);
                Log.d(TAG, "Silver price used: " + pricePerTicket);
                break;
            default: // Bronze/Normal
                pricePerTicket = getPriceWithDefault(spectacle.getPrixNormal(), DEFAULT_BRONZE_PRICE);
                Log.d(TAG, "Normal price used: " + pricePerTicket);
                break;
        }

        double total = pricePerTicket.doubleValue() * quantity;
        Log.d(TAG, "Total amount calculated: " + total + " (price=" + pricePerTicket + " * quantity=" + quantity + ")");
        return total;
    }

    // Helper method to safely get price with a default value if null
    private BigDecimal getPriceWithDefault(BigDecimal price, BigDecimal defaultPrice) {
        // More verbose logging to help diagnose issues
        if (price == null) {
            Log.w(TAG, "Price is null, using default: " + defaultPrice);
            return defaultPrice;
        } else if (price.compareTo(BigDecimal.ZERO) <= 0) {
            Log.w(TAG, "Price is zero or negative (" + price + "), using default: " + defaultPrice);
            return defaultPrice;
        } else {
            Log.d(TAG, "Using actual price from API: " + price);
            return price;
        }
    }


    private void updatePrice() {
        try {
            // First, check if we have all the necessary objects
            if (selectedPerformance == null) {
                Log.e(TAG, "Selected performance is null in updatePrice");
                totalPrice.setText("Total: 0.00 DT");
                return;
            }

            Spectacle spectacle = selectedPerformance.getSpectacle();
            if (spectacle == null) {
                Log.e(TAG, "Spectacle is null in updatePrice");
                totalPrice.setText("Total: 0.00 DT");
                return;
            }

            // IMPROVEMENT: Log price information for debugging
            Log.d(TAG, "updatePrice with spectacle prices - Gold: " + spectacle.getPrixGold() +
                    ", Silver: " + spectacle.getPrixSilver() +
                    ", Normal: " + spectacle.getPrixNormal());

            // Debug current selection state
            Log.d(TAG, "updatePrice: Gold checked=" + goldTicket.isChecked() +
                    ", Silver checked=" + silverTicket.isChecked() +
                    ", Bronze checked=" + bronzeTicket.isChecked());

            // Make sure at least one category is selected
            if (!goldTicket.isChecked() && !silverTicket.isChecked() && !bronzeTicket.isChecked()) {
                Log.d(TAG, "No category selected in updatePrice");
                totalPrice.setText("Total: 0.00 DT");
                return;
            }

            // Calculate the total amount
            double totalAmount = calculateTotalAmount();

            // Update UI
            totalPrice.setText(String.format(Locale.getDefault(), "Total: %.2f DT", totalAmount));
            Log.d(TAG, "Updated price display to: " + totalAmount);

        } catch (Exception e) {
            Log.e(TAG, "Error updating price: " + e.getMessage(), e);
            totalPrice.setText("Total: 0.00 DT");
        }
    }

    private void makeReservation(Long clientId, Client guestClient) {
        if (!validateInput()) return;

        String category = getSelectedCategory();
        int quantity = getSelectedQuantity();

        if (!validateCapacity(category, quantity)) return;

        // Log spectacle information for debugging
        if (selectedPerformance != null && selectedPerformance.getSpectacle() != null) {
            Spectacle spectacle = selectedPerformance.getSpectacle();
            Log.d(TAG, "Making reservation with spectacle prices - Gold: " + spectacle.getPrixGold() +
                    ", Silver: " + spectacle.getPrixSilver() +
                    ", Normal: " + spectacle.getPrixNormal());
        }

        // Calculate total amount
        double totalAmount = calculateTotalAmount();
        Log.d(TAG, "Making reservation with total amount: " + totalAmount);
        ReservationRequest request;

        boolean isGuest = (clientId == GUEST_USER_ID && guestClient != null);

        // Create the reservation request based on the structure needed
        request = new ReservationRequest();
        request.setPerformanceId(selectedPerformance.getIdPerformance());
        request.setMontantTotal(totalAmount);
        request.setStatus("Confirmée");
        request.setCategorie(category);
        request.setNbBillets(quantity);

        // Handle guest vs regular user
        if (isGuest) {
            // Set guest flag and information for guest users
            request.setGuest(true);
            request.setGuestName(guestClient.getNomclt());
            request.setGuestEmail(guestClient.getEmail());
            Log.d(TAG, "Creating GUEST reservation with email: " + guestClient.getEmail());
        } else {
            // For regular users, set the client ID
            request.setClientId(clientId);
            request.setGuest(false);
        }

        // Log the request for debugging
        Log.d(TAG, "Reservation request: clientId=" + request.getClientId() +
                ", performanceId=" + request.getPerformanceId() +
                ", category=" + request.getCategorie() +
                ", nbBillets=" + request.getNbBillets() +
                ", amount=" + request.getMontantTotal() +
                ", isGuest=" + request.isGuest() +
                (isGuest ? ", guestEmail=" + request.getGuestEmail() : ""));

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing reservation...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Use a single endpoint for both guest and regular reservations
        Call<ReservationResponse> call = apiService.createReservation(request);

        // Execute the API call
        call.enqueue(new Callback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Reservation successful: " + response.body().getId());
                    ReservationResponse reservationResponse = response.body();
                    String qrContent = "Reservation ID: " + reservationResponse.getId() +
                            "\nCategory: " + request.getCategorie() +
                            "\nQuantity: " + request.getNbBillets();

                    try {
                        Bitmap qrBitmap = generateQRCode(qrContent);
                        saveQRCodeToGallery(qrBitmap, "Reservation_" + reservationResponse.getId());
                    } catch (WriterException e) {
                        Log.e(TAG, "QR generation failed: " + e.getMessage());
                    }


                    // Handle the successful response based on whether it's a guest reservation
                    if (isGuest) {
                        showGuestSuccess(reservationResponse, guestClient.getEmail());
                    } else {
                        showSuccess(reservationResponse);
                    }
                } else {
                    String errorMsg = "Reservation failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        errorMsg += "\nError reading error body";
                    }
                    Log.e(TAG, errorMsg);
                    showError(errorMsg);
                }

            }

            @Override
            public void onFailure(Call<ReservationResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private Bitmap generateQRCode(String content) throws WriterException {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 400, 400);
        return new BarcodeEncoder().createBitmap(bitMatrix);
    }
    private void saveQRCodeToGallery(Bitmap qrBitmap, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Reservations");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(this, "QR Code saved to gallery", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Error saving QR code: " + e.getMessage());
            }
        }
    }





    // Show success message for guest users
    private void showGuestSuccess(ReservationResponse response, String email) {
        String msg = "Reservation successful!\n" +
                "Reservation ID: " + response.getId() + "\n" +
                "Category: " + getSelectedCategory() + "\n" +
                "Tickets: " + getSelectedQuantity() + "\n" +
                "Total: " + response.getFormattedAmount() + "\n\n" +
                "A confirmation email has been sent to: " + email;

        new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage(msg)
                .setPositiveButton("OK", (dialog, which) -> {

                })
                .show();
    }

    // Update the showSuccess method to use ReservationResponse
    private void showSuccess(ReservationResponse response) {
        String msg = "Reservation successful!\n" +
                "Reservation ID: " + response.getId() + "\n" +
                "Category: " + getSelectedCategory() + "\n" +
                "Tickets: " + getSelectedQuantity() + "\n" +
                "Total: " + response.getFormattedAmount();

        new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage(msg)
                .setPositiveButton("OK", (dialog, which) -> {

                })
                .show();
    }

    private String getSelectedCategory() {
        if (goldTicket.isChecked()) {
            return "Gold";
        } else if (silverTicket.isChecked()) {
            return "Silver";
        }
        return "Normal";
    }

    private void getPerformanceFromIntent() {
        selectedPerformance = (Performance) getIntent().getSerializableExtra("performance");
        if (selectedPerformance == null) {
            Toast.makeText(this, "No performance selected", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No performance selected in intent");
            finish();
            return;
        }

        Log.d(TAG, "Performance received from intent: ID=" + selectedPerformance.getIdPerformance());

        // Check if performance has complete spectacle information
        if (selectedPerformance.getSpectacle() == null) {
            Log.w(TAG, "Performance has null spectacle object, fetching complete data");
            fetchCompletePerformanceData(selectedPerformance.getIdPerformance());
        } else if (selectedPerformance.getSpectacle().getTitre() == null ||
                selectedPerformance.getSpectacle().getIdSpec() == null) {
            Log.w(TAG, "Performance has incomplete spectacle information, fetching complete data");
            fetchCompletePerformanceData(selectedPerformance.getIdPerformance());
        } else {
            // Continue with existing flow using the complete data we already have
            Log.d(TAG, "Using spectacle data from intent: " +
                    "ID=" + selectedPerformance.getSpectacle().getIdSpec() +
                    ", Title=" + selectedPerformance.getSpectacle().getTitre());

            // Debug prices
            logSpectaclePrices(selectedPerformance.getSpectacle());
            fetchCompletePerformanceData(selectedPerformance.getIdPerformance());

            // Initialize UI with loaded data
            updatePrice();
        }
    }

    private void logSpectaclePrices(Spectacle spectacle) {
        if (spectacle == null) {
            Log.e(TAG, "Cannot log prices: Spectacle is null");
            return;
        }

        Log.d(TAG, "Spectacle prices: " +
                "Gold=" + (spectacle.getPrixGold() != null ? spectacle.getPrixGold() : "null") + ", " +
                "Silver=" + (spectacle.getPrixSilver() != null ? spectacle.getPrixSilver() : "null") + ", " +
                "Normal=" + (spectacle.getPrixNormal() != null ? spectacle.getPrixNormal() : "null"));
    }



    private void fetchCompletePerformanceData(Long performanceId) {
        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading performance information...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // IMPROVEMENT: Use the specialized endpoint and log the entire response for debugging
        apiService.getPerformanceWithSpectacle(performanceId).enqueue(new Callback<Performance>() {
            @Override
            public void onResponse(Call<Performance> call, Response<Performance> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Performance performance = response.body();

                    // Log the complete response to see exactly what prices we're getting
                    Log.d(TAG, "API Response: " + new Gson().toJson(performance));

                    handleFetchedPerformance(performance, progressDialog);
                } else {
                    // If the first endpoint fails, fall back to the regular getPerformanceById
                    Log.w(TAG, "getPerformanceWithSpectacle failed with code " + response.code() +
                            ", trying fallback method");
                    fallbackFetchPerformanceById(performanceId, progressDialog);
                }
            }

            @Override
            public void onFailure(Call<Performance> call, Throwable t) {
                Log.e(TAG, "getPerformanceWithSpectacle failed: " + t.getMessage() +
                        ", trying fallback method", t);
                fallbackFetchPerformanceById(performanceId, progressDialog);
            }
        });
    }


    private void fallbackFetchPerformanceById(Long performanceId, ProgressDialog progressDialog) {
        // Fallback method using standard endpoint
        apiService.getPerformanceById(performanceId).enqueue(new Callback<Performance>() {
            @Override
            public void onResponse(Call<Performance> call, Response<Performance> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Performance performance = response.body();

                    // Check if we have a spectacle with ID
                    if (performance.getSpectacle() != null && performance.getSpectacle().getIdSpec() != null) {
                        // Now fetch the complete spectacle
                        fetchSpectacleById(performance.getSpectacle().getIdSpec(), performance, progressDialog);
                    } else {
                        // If there's still no spectacle, create a default one to prevent crashes
                        handleMissingSpectacle(performance, progressDialog);
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ReservationActivity.this,
                            "Error loading performance data: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Performance> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Network error loading performance: " + t.getMessage(), t);
                Toast.makeText(ReservationActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchSpectacleById(Long spectacleId, Performance performance, ProgressDialog progressDialog) {
        apiService.getSpectacleById(spectacleId).enqueue(new Callback<Spectacle>() {
            @Override
            public void onResponse(Call<Spectacle> call, Response<Spectacle> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Spectacle spectacle = response.body();

                    // Update the performance with the full spectacle information
                    performance.setSpectacle(spectacle);
                    handleFetchedPerformance(performance, progressDialog);
                } else {
                    // If spectacle fetch fails, create a default spectacle to prevent crashes
                    handleMissingSpectacle(performance, progressDialog);
                }
            }

            @Override
            public void onFailure(Call<Spectacle> call, Throwable t) {
                Log.e(TAG, "Network error loading spectacle: " + t.getMessage(), t);

                // If spectacle fetch fails, create a default spectacle to prevent crashes
                handleMissingSpectacle(performance, progressDialog);
            }
        });
    }

    private void handleFetchedPerformance(Performance performance, ProgressDialog progressDialog) {
        progressDialog.dismiss();

        // Store the updated performance
        selectedPerformance = performance;

        // Check if we have a spectacle now
        if (performance.getSpectacle() != null) {
            Spectacle spectacle = performance.getSpectacle();
            Log.d(TAG, "Complete performance data loaded with spectacle: " + spectacle.getTitre());

            // IMPROVEMENT: More detailed price logging
            Log.d(TAG, "Received spectacle prices - Gold: " + spectacle.getPrixGold() +
                    ", Silver: " + spectacle.getPrixSilver() +
                    ", Normal: " + spectacle.getPrixNormal());

            // IMPROVEMENT: Attempt to address potential null or zero prices
            if (spectacle.getPrixGold() == null && spectacle.getPrixSilver() == null && spectacle.getPrixNormal() == null) {
                Log.w(TAG, "All spectacle prices are null - API might not be returning price data correctly");

                // IMPROVEMENT: Fetch the spectacle separately to see if we can get prices
                fetchSpectacleById(spectacle.getIdSpec(), performance, null);
            } else {
                // Update UI with the complete data
                updatePrice();
            }
        } else {
            // Create a default spectacle if we still don't have one
            handleMissingSpectacle(performance, null);
        }
    }


    private void handleMissingSpectacle(Performance performance, ProgressDialog progressDialog) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        // Create a default spectacle to prevent crashes
        Spectacle defaultSpectacle = new Spectacle();
        defaultSpectacle.setIdSpec(0L);  // Use a placeholder ID
        defaultSpectacle.setTitre("Unknown spectacle");

        // Set default prices
        defaultSpectacle.setPrixGold(DEFAULT_GOLD_PRICE);
        defaultSpectacle.setPrixSilver(DEFAULT_SILVER_PRICE);
        defaultSpectacle.setPrixNormal(DEFAULT_BRONZE_PRICE);

        // Attach the default spectacle to the performance
        performance.setSpectacle(defaultSpectacle);
        selectedPerformance = performance;

        Log.w(TAG, "Using default spectacle due to missing spectacle information");

        // Show a warning to the user but allow them to continue
        Toast.makeText(ReservationActivity.this,
                "Some spectacle information is missing. Default pricing will be used.",
                Toast.LENGTH_LONG).show();

        // Update the UI with default values
        updatePrice();
    }



    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    private void showSuccess(Reservation response) {
        String msg = "Reservation successful!\n" +
                "Performance: " + response.getPerformance().getFormattedDateTime() + "\n" +
                "Category: " + response.getCategorie() + "\n" +
                "Tickets: " + response.getNbBillets() + "\n" +
                "Total: " + response.getMontantTotal() + " DT";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}