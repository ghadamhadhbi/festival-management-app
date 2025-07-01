package com.example.festiv.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.festiv.Adapters.DateAdapter;
import com.example.festiv.Adapters.HourAdapter;
import com.example.festiv.Models.Lieu;
import com.example.festiv.Models.Performance;
import com.example.festiv.Models.Spectacle;
import com.example.festiv.Models.SpectaclePrices;
import com.example.festiv.R;
import com.example.festiv.api.ApiService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FestivalDetailActivity extends AppCompatActivity implements DateAdapter.OnDateClickListener, HourAdapter.OnHourClickListener {
    private static final String TAG = "FestivalDetailActivity";

    private static final String BASE_URL = "http://192.168.7.181:9090/"; // Update for localhost testing
    private ApiService apiService;

    private ImageView festivalImage;
    private TextView festivalName;
    private TextView festivalDateRange;
    private TextView festivalDescription;
    private TextView festivalAddress;
    private TextView festivalWebsite;
    private TextView festivalPriceCapacity;
    private Button reserveButton;

    private TextView hoursSelectionTitle;
    private RecyclerView datesRecyclerView;
    private RecyclerView hoursRecyclerView;

    private DateAdapter dateAdapter;
    private HourAdapter hourAdapter;

    private Spectacle spectacle;
    private Map<String, List<Performance>> performancesByDate;
    private String selectedDate;
    private Performance selectedPerformance;
    private SpectaclePrices currentPrices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_festival_detail);

        Log.d(TAG, "onCreate: Starting activity initialization");

        // Initialize views
        initViews();

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Get spectacle from intent **before** anything else
        spectacle = (Spectacle) getIntent().getSerializableExtra("spectacle");
        if (spectacle == null) {
            Toast.makeText(this, "Error: Spectacle not found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Spectacle object is null!");
            finish();
            return;
        }

        Log.d(TAG, "Spectacle loaded: " + spectacle.getTitre());

        if (spectacle.getPerformances() != null) {
            Log.d(TAG, "Performances count: " + spectacle.getPerformances().size());
            for (Performance p : spectacle.getPerformances()) {
                Log.d(TAG, "Performance: " + p.getIdPerformance() +
                        " Date: " + p.getDateHeureDebut() +
                        " Venue: " + (p.getLieu() != null ? p.getLieu().getNomLieu() : "null"));
            }
        } else {
            Log.d(TAG, "Spectacle performances are null or empty");
        }

        processPerformances();
        setupRecyclerViews();
        populateUI();
        setupClickListeners();
    }

    private void initViews() {
        festivalImage = findViewById(R.id.festivalImage);
        festivalName = findViewById(R.id.festivalName);
        festivalDateRange = findViewById(R.id.festivalDateRange);
        festivalDescription = findViewById(R.id.festivalDescription);
        festivalAddress = findViewById(R.id.festivalAddress);
        festivalWebsite = findViewById(R.id.festivalWebsite);
        festivalPriceCapacity = findViewById(R.id.festivalPriceCapacity);
        reserveButton = findViewById(R.id.reserveButton);

        hoursSelectionTitle = findViewById(R.id.hoursSelectionTitle);
        datesRecyclerView = findViewById(R.id.datesRecyclerView);
        hoursRecyclerView = findViewById(R.id.hoursRecyclerView);



    }

    private void processPerformances() {
        performancesByDate = new TreeMap<>();
        Log.d(TAG, "Processing performances");

        if (spectacle == null) {
            Log.e(TAG, "Spectacle is null in processPerformances");
            return;
        }

        List<Performance> performances = spectacle.getPerformances();
        if (performances == null || performances.isEmpty()) {
            Log.e(TAG, "No performances available in spectacle object");
            // Load performances from database for this spectacle
            loadPerformancesForSpectacle();
            return;
        }

        Log.d(TAG, "Found " + performances.size() + " performances in spectacle object");
        // Process the performances that already exist in the spectacle object
        loadPerformancesFromDatabase(performances);
    }

    // New method to fetch performances from API
    private void loadPerformancesForSpectacle() {
        Log.d(TAG, "Loading upcoming performances from API for spectacle ID: " + spectacle.getIdSpec());

        // Use the upcoming performances endpoint instead of all performances
        Call<List<Performance>> call = apiService.getUpcomingPerformancesBySpectacle(spectacle.getIdSpec().intValue());


        call.enqueue(new Callback<List<Performance>>() {
            @Override
            public void onResponse(Call<List<Performance>> call, Response<List<Performance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Performance> performanceList = response.body();
                    Log.d(TAG, "Successfully loaded " + performanceList.size() + " upcoming performances from API");

                    // Check if we found any upcoming performances
                    if (performanceList.isEmpty()) {
                        // Display a message when no upcoming performances are available
                        Toast.makeText(FestivalDetailActivity.this,
                                "No upcoming performances available for this spectacle",
                                Toast.LENGTH_LONG).show();

                        // Disable the reservation button
                        reserveButton.setEnabled(false);
                        reserveButton.setText("No Upcoming Shows");

                        // Clear existing performance data
                        performancesByDate.clear();
                        if (dateAdapter != null) {
                            dateAdapter.setDates(new ArrayList<>());
                        }
                        if (hourAdapter != null) {
                            hourAdapter.setHours(new ArrayList<>());
                        }

                        // Update UI to show no performances
                        hoursSelectionTitle.setText("No upcoming performances available");
                        return;
                    }

                    // Fix any null venues by creating placeholders first
                    for (Performance p : performanceList) {
                        if (p.getLieu() == null) {
                            Log.w(TAG, "API returned performance ID " + p.getIdPerformance() + " with null venue - creating placeholder");

                            // Create a placeholder venue
                            Lieu placeholderLieu = new Lieu();
                            placeholderLieu.setNomLieu("Loading venue...");
                            placeholderLieu.setVille("Loading...");
                            p.setLieu(placeholderLieu);
                        }
                    }

                    // Add performances to spectacle object for future reference
                    spectacle.setPerformances(performanceList);

                    // Process the performances to group by date
                    loadPerformancesFromDatabase(performanceList);

                    // AFTER processing, load detailed venue information
                    for (Performance p : performanceList) {
                        loadVenueForPerformance(p);
                    }
                } else {
                    Log.e(TAG, "API error: " + response.code());
                    Toast.makeText(FestivalDetailActivity.this,
                            "Error loading performances: " + response.message(),
                            Toast.LENGTH_SHORT).show();

                    // Show error in UI
                    hoursSelectionTitle.setText("Unable to load performance schedule");
                    hoursSelectionTitle.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Performance>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                Toast.makeText(FestivalDetailActivity.this,
                        "Failed to connect to server: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Show error in UI
                hoursSelectionTitle.setText("Connection error - Can't load performances");
                hoursSelectionTitle.setVisibility(View.VISIBLE);
            }
        });
    }
    private void loadVenueForPerformance(Performance performance) {
        if (performance == null) {
            Log.e(TAG, "Cannot load venue: Performance is null");
            return;
        }

        Long performanceId = performance.getIdPerformance();
        if (performanceId == null) {
            Log.e(TAG, "Cannot load venue: Performance has no ID");
            return;
        }

        // FIX: Create a placeholder venue if it's null
        if (performance.getLieu() == null) {
            Lieu placeholderLieu = new Lieu();
            placeholderLieu.setNomLieu("Loading venue...");
            placeholderLieu.setVille("Loading...");
            performance.setLieu(placeholderLieu);
        }

        // Call API to get venue by performance ID instead of venue ID
        Call<Lieu> call = apiService.getLieuByPerformanceId(performanceId);
        call.enqueue(new Callback<Lieu>() {
            @Override
            public void onResponse(Call<Lieu> call, Response<Lieu> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Lieu venue = response.body();
                    Log.d(TAG, "Successfully loaded venue for performance " +
                            performanceId + ": " + venue.getNomLieu());

                    // Update the performance with the fetched venue
                    performance.setLieu(venue);

                    // Update UI if needed
                    runOnUiThread(() -> {
                        if (selectedPerformance != null &&
                                selectedPerformance.getIdPerformance().equals(performanceId)) {
                            updateVenueInformation(performance);
                        }
                        // Notify adapter of data change
                        if (hourAdapter != null) {
                            hourAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to load venue data: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Lieu> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    private void loadPerformancesFromDatabase(List<Performance> performanceList) {
        Log.d(TAG, "Loading performances from database...");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Performance performance : performanceList) {
            String dateKey = performance.getDatePerformance();

            if (dateKey != null && !dateKey.isEmpty()) {
                // Debug each performance venue
                if (performance.getLieu() == null) {
                    Log.w(TAG, "Performance ID " + performance.getIdPerformance() + " has null venue");
                } else {
                    Log.d(TAG, "Performance ID " + performance.getIdPerformance() +
                            " venue: " + performance.getLieu().getNomLieu() +
                            ", city: " + performance.getLieu().getVille());
                }

                if (!performancesByDate.containsKey(dateKey)) {
                    performancesByDate.put(dateKey, new ArrayList<>());
                }
                performancesByDate.get(dateKey).add(performance);
                Log.d(TAG, "Added performance on: " + dateKey);
            } else {
                Log.w(TAG, "Performance has invalid date: " + performance.getIdPerformance());
            }
        }

        Log.d(TAG, "Loaded " + performancesByDate.size() + " dates");

        // After loading, setup RecyclerViews
        setupRecyclerViews();
    }

    // Update the setupRecyclerViews method to ensure dates are displayed
    private void setupRecyclerViews() {
        // Set up date adapter
        dateAdapter = new DateAdapter(this);
        LinearLayoutManager dateLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        datesRecyclerView.setLayoutManager(dateLayoutManager);
        datesRecyclerView.setAdapter(dateAdapter);

        // Set up hour adapter **avant** d'appeler onDateClick
        hourAdapter = new HourAdapter(this);
        LinearLayoutManager hourLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        hoursRecyclerView.setLayoutManager(hourLayoutManager);
        hoursRecyclerView.setAdapter(hourAdapter);

        List<String> datesList = new ArrayList<>(performancesByDate.keySet());
        Log.d(TAG, "Setting up dates adapter with " + datesList.size() + " dates");

        if (datesList.isEmpty()) {
            Log.w(TAG, "No dates available for display!");
            Toast.makeText(this, "No performance dates available", Toast.LENGTH_SHORT).show();
        } else {
            dateAdapter.setDates(datesList);
            dateAdapter.setSelectedPosition(0);
            onDateClick(datesList.get(0), 0);  // Maintenant c'est sûr, hourAdapter n'est plus null ici
        }
    }

    private void populateUI() {
        // Load image with error handling
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();

        // Handle image loading
        String imagePath = spectacle.getImageUrl();
        if (imagePath != null && !imagePath.isEmpty()) {
            // Try to get the resource ID first
            int imageResId = getResources().getIdentifier(
                    imagePath,
                    "drawable",
                    getPackageName()
            );

            if (imageResId != 0) {
                // Load from drawable resource
                Log.d(TAG, "Loading image from resources: " + imagePath);
                Glide.with(this)
                        .load(imageResId)
                        .apply(requestOptions)
                        .into(festivalImage);
            } else {
                // Load from URL
                Log.d(TAG, "Loading image from URL: " + imagePath);
                Glide.with(this)
                        .load(imagePath)
                        .apply(requestOptions)
                        .into(festivalImage);
            }
        } else {
            // Fallback to placeholder
            Log.d(TAG, "No image specified, using placeholder");
            Glide.with(this)
                    .load(R.drawable.placeholder_image)
                    .into(festivalImage);
        }

        // Set title
        festivalName.setText(spectacle.getTitre());
        Log.d(TAG, "Set title: " + spectacle.getTitre());

        // Set date range
        String dateRange = spectacle.getFirstPerformanceDateFormatted() + " - " +
                spectacle.getLastPerformanceDateFormatted();
        if (spectacle.getDureeMinutes() != null) {
            dateRange += " • " + spectacle.getDureeMinutes() + " minutes";
        }
        festivalDateRange.setText(dateRange);
        Log.d(TAG, "Set date range: " + dateRange);

        // Set description
        festivalDescription.setText(spectacle.getDescription());

        // Set initial venue information
        String venueInfo = spectacle.getFormattedVenueInfo();
        Log.d(TAG, "Initial venue info: " + venueInfo);
        festivalAddress.setText(venueInfo);

        // Set website
        if (spectacle.getWebsiteUrl() != null && !spectacle.getWebsiteUrl().isEmpty()) {
            festivalWebsite.setText("Official Website");
            festivalWebsite.setVisibility(View.VISIBLE);
        } else {
            festivalWebsite.setVisibility(View.GONE);
        }

        // Display a placeholder for price information until we select a performance
        festivalPriceCapacity.setText("Select a date and time to see prices");
        festivalPriceCapacity.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        // Address click listener - opens Google Maps
        festivalAddress.setOnClickListener(v -> {
            if (selectedPerformance != null && selectedPerformance.getLieu() != null) {
                Lieu venue = selectedPerformance.getLieu();
                String address = "";

                // Build the address string with available information
                if (venue.getNomLieu() != null && !venue.getNomLieu().isEmpty()) {
                    address = venue.getNomLieu();
                }
                if (venue.getAdresse() != null && !venue.getAdresse().isEmpty()) {
                    if (!address.isEmpty()) address += ", ";
                    address += venue.getAdresse();
                }
                if (venue.getVille() != null && !venue.getVille().isEmpty()) {
                    if (!address.isEmpty()) address += ", ";
                    address += venue.getVille();
                }

                if (address.isEmpty()) {
                    Toast.makeText(this, "No address information available", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Searching address in map: " + address);
                searchAddressInMap(address);
            } else {
                Toast.makeText(this, "Please select a performance first", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Map requested but no performance selected");
            }
        });

        // Website click listener - opens browser
        festivalWebsite.setOnClickListener(v -> {
            if (spectacle.getWebsiteUrl() != null && !spectacle.getWebsiteUrl().isEmpty()) {
                Log.d(TAG, "Opening website: " + spectacle.getWebsiteUrl());

                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(spectacle.getWebsiteUrl()));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening website: " + e.getMessage());
                    Toast.makeText(this, "Error opening website", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Reserve button click listener
        reserveButton.setOnClickListener(v -> {
            if (selectedPerformance == null) {
                Toast.makeText(this, "Please select a date and time first", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Reservation attempted without selecting a performance");
                return;
            }

            try {
                String venueInfo = selectedPerformance.getLieu() != null ?
                        selectedPerformance.getLieu().getNomLieu() : "unknown venue";
                Log.d(TAG, "Booking: " + selectedPerformance.getFormattedDateTime() + " at " + venueInfo);

                // Create intent to navigate to ReservationActivity
                Intent intent = new Intent(this, ReservationActivity.class);
                // Pass the performance object to the ReservationActivity
                intent.putExtra("performance", selectedPerformance);
                // Also pass the prices if they have been loaded
                if (currentPrices != null) {
                    intent.putExtra("prixNormal", currentPrices.getPrixNormal());
                    intent.putExtra("prixSilver", currentPrices.getPrixSilver());
                    intent.putExtra("prixGold", currentPrices.getPrixGold());
                }
                // Start the ReservationActivity
                startActivity(intent);

                // Toast to indicate transition
                Toast.makeText(this, "Booking: " + selectedPerformance.getFormattedDateTime() +
                        " at " + venueInfo, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error starting reservation activity: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(this, "Error starting reservation. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAddressInMap(String address) {
        try {
            // Try to open in Google Maps app first
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps"); // Optional: prefer Google Maps

            // Check if Google Maps or any other map app is installed
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback: Open in web browser
                Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);

                if (webIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(webIntent);
                } else {
                    Toast.makeText(this, "No map app or browser available", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "No app to handle maps or web URLs");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening maps: " + e.getMessage());
            Toast.makeText(this, "Error opening maps", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateClick(String date, int position) {
        selectedDate = date;
        List<Performance> performances = performancesByDate.get(date);

        if (performances != null && !performances.isEmpty()) {
            // Create list of formatted times for display
            List<String> displayTimes = new ArrayList<>();
            for (Performance p : performances) {
                displayTimes.add(p.getFormattedTime()); // Use the new formatted time
            }

            hourAdapter.setHours(displayTimes);
            hourAdapter.setPerformanceList(performances); // Make sure this line is working

            // Log for debugging
            Log.d(TAG, "Set performances for hourAdapter: " + performances.size());
            for (Performance p : performances) {
                Log.d(TAG, "Performance venue: " +
                        (p.getLieu() != null ? p.getLieu().getVille() : "null venue"));
            }

            // Show UI elements
            hoursSelectionTitle.setVisibility(View.VISIBLE);
            hoursRecyclerView.setVisibility(View.VISIBLE);
        } else {
            Log.w(TAG, "No performances found for date: " + date);
        }
    }

    @Override
    public void onHourClick(String hourDisplay, int position) {
        Log.d(TAG, "Hour selected: " + hourDisplay + " at position " + position);

        try {
            // Find the selected performance
            if (selectedDate != null) {
                List<Performance> performances = performancesByDate.get(selectedDate);
                if (performances != null && position < performances.size()) {
                    // Use the position directly to get the corresponding performance
                    selectedPerformance = performances.get(position);
                    Log.d(TAG, "Selected performance ID: " + selectedPerformance.getIdPerformance());

                    // Update venue information
                    updateVenueInformation(selectedPerformance);

                    // Load prices for this performance
                    loadPricesForPerformance(selectedPerformance);

                    // Enable the reserve button
                    reserveButton.setEnabled(true);
                } else {
                    Log.w(TAG, "Invalid position or no performances available");
                    Toast.makeText(this, "Cannot select this time slot. Please try another.",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "Hour selected but no date is selected");
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error when selecting hour: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error selecting time slot", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPricesForPerformance(Performance performance) {
        if (performance == null || performance.getIdPerformance() == null) {
            Log.e(TAG, "Cannot load prices: Performance or ID is null");
            festivalPriceCapacity.setText("Prices not available - Invalid performance data");
            return;
        }

        // Show loading in the price text view
        festivalPriceCapacity.setText("Loading prices...");

        // Important: Backend uses Long for IDs, so we should use Long.valueOf() instead of intValue()
        long performanceId = performance.getIdPerformance();
        Log.d(TAG, "Loading prices for performance ID: " + performanceId);

        // Use the correct path parameter name to match the backend: 'id' instead of 'idPerformance'
        Call<SpectaclePrices> call = apiService.getSpectaclePrices((int)performanceId);

        call.enqueue(new Callback<SpectaclePrices>() {
            @Override
            public void onResponse(Call<SpectaclePrices> call, Response<SpectaclePrices> response) {
                Log.d(TAG, "Price API response code: " + response.code() + ", isSuccessful: " + response.isSuccessful());

                if (response.isSuccessful()) {
                    SpectaclePrices prices = response.body();

                    if (prices != null) {
                        currentPrices = prices;

                        // Log full price details including null check
                        Log.d(TAG, "Prices loaded successfully: " +
                                "Normal: " + (prices.getPrixNormal() != null ? "€" + prices.getPrixNormal() : "null") +
                                ", Silver: " + (prices.getPrixSilver() != null ? "€" + prices.getPrixSilver() : "null") +
                                ", Gold: " + (prices.getPrixGold() != null ? "€" + prices.getPrixGold() : "null"));

                        // Update the UI with real prices
                        updatePriceInfo(prices);
                    } else {
                        Log.e(TAG, "Price response body is null despite successful response");
                        festivalPriceCapacity.setText("Prices not available - No data returned");
                    }
                } else {
                    // More detailed error reporting
                    String errorMsg = "Failed to load prices: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }

                    Log.e(TAG, errorMsg);
                    festivalPriceCapacity.setText("Prices not available (Error " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<SpectaclePrices> call, Throwable t) {
                Log.e(TAG, "API call for prices failed: " + t.getMessage(), t);
                festivalPriceCapacity.setText("Failed to load prices - Network error");
            }
        });
    }
    // Update the price display method with better null handling
    private void updatePriceInfo(SpectaclePrices prices) {
        if (prices == null) {
            festivalPriceCapacity.setText("Price information not available");
            return;
        }

        // Check if all prices are null
        if (prices.getPrixNormal() == null && prices.getPrixSilver() == null && prices.getPrixGold() == null) {
            festivalPriceCapacity.setText("No price information available for this performance");
            return;
        }

        StringBuilder priceInfo = new StringBuilder();

        if (prices.getPrixNormal() != null) {
            priceInfo.append("Normal: €").append(formatPrice(prices.getPrixNormal()));
        } else {
            priceInfo.append("Normal: Not available");
        }

        if (prices.getPrixSilver() != null && prices.getPrixSilver().compareTo(BigDecimal.ZERO) > 0) {
            priceInfo.append(" | Silver: €").append(formatPrice(prices.getPrixSilver()));
        }

        if (prices.getPrixGold() != null && prices.getPrixGold().compareTo(BigDecimal.ZERO) > 0) {
            priceInfo.append(" | Gold: €").append(formatPrice(prices.getPrixGold()));
        }

        // Update the TextView on the UI thread
        final String finalPriceInfo = priceInfo.toString();
        runOnUiThread(() -> {
            festivalPriceCapacity.setText(finalPriceInfo);
            festivalPriceCapacity.setVisibility(View.VISIBLE);
        });
    }

    // Helper method to safely format BigDecimal prices
    private String formatPrice(BigDecimal price) {
        try {
            if (price == null) return "N/A";
            return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            Log.e(TAG, "Error formatting price: " + e.getMessage());
            return "Error";
        }
    }

    private void updateVenueInformation(Performance performance) {
        if (performance == null) {
            Log.e(TAG, "updateVenueInformation called with null performance");
            festivalAddress.setText("Venue information not available");
            return;
        }

        Log.d(TAG, "Updating venue information for performance ID: " + performance.getIdPerformance());

        // Debug performance venue details
        if (performance.getLieu() == null) {
            Log.e(TAG, "Performance venue (Lieu) is null");
            festivalAddress.setText("Venue information currently unavailable");
            festivalAddress.setClickable(false);  // Disable map clicking when no venue

            // FIX: Check for null venue before trying to access properties
            // Attempt to load the venue
            loadVenueForPerformance(performance);
            return;
        }

        Lieu venue = performance.getLieu();
        // Debug venue fields
        Log.d(TAG, "Venue details - ID: " + venue.getIdLieu()
                + ", Name: " + venue.getNomLieu()
                + ", Address: " + venue.getAdresse()
                + ", City: " + venue.getVille());

        String locationText = venue.getNomLieu() != null ? venue.getNomLieu() : "Unknown venue";

        if (venue.getAdresse() != null && !venue.getAdresse().isEmpty()) {
            locationText += ", " + venue.getAdresse();

            if (venue.getVille() != null && !venue.getVille().isEmpty()) {
                locationText += ", " + venue.getVille();
            }
        } else if (venue.getVille() != null && !venue.getVille().isEmpty()) {
            locationText += ", " + venue.getVille();
        }

        Log.d(TAG, "Setting venue info to: " + locationText);
        festivalAddress.setText(locationText);
        festivalAddress.setVisibility(View.VISIBLE);
        festivalAddress.setClickable(true);
        festivalAddress.setFocusable(true);
    }




    }