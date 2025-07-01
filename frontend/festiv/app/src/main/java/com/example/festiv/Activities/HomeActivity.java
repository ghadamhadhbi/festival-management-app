package com.example.festiv.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.festiv.Adapters.FestivalAdapter;
import com.example.festiv.Models.Spectacle;
import com.example.festiv.Models.Bookmark;
import com.example.festiv.Models.Lieu;
import com.example.festiv.Models.Performance;
import com.example.festiv.Models.Reservation;
import com.example.festiv.Models.UserSession;
import com.example.festiv.R;
import com.example.festiv.api.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity implements FestivalAdapter.OnSaveClickListener {

    private RecyclerView recyclerView;
    private FestivalAdapter adapter;
    private List<Spectacle> spectacleList = new ArrayList<>();
    private SearchView searchView;
    private ImageButton dateFilterButton;
    private ApiService apiService;
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private boolean isStartDateSet = false;
    private boolean isEndDateSet = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private TextView translationToggle;
    private boolean isEnglish = true; // Default language is English
    private Translator frenchEnglishTranslator;
    private Translator englishFrenchTranslator;
    private SharedPreferences sharedPreferences;
    private Set<String> bookmarkedIds = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPreferences = getSharedPreferences("BookmarkPrefs", MODE_PRIVATE);
        bookmarkedIds = sharedPreferences.getStringSet("bookmarked_spectacles", new HashSet<>());

        initializeViews();
        setupRecyclerView();
        setupRetrofit();
        setupUIComponents();
        loadAllSpectacles();
        setupBottomNavigation();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.festivalsRecyclerView);
        searchView = findViewById(R.id.searchView);
        dateFilterButton = findViewById(R.id.dateFilterButton);
        translationToggle = findViewById(R.id.translationToggle);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FestivalAdapter(spectacleList, this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.7.181:9090/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void setupUIComponents() {
        setupSearchView();
        setupDateFilter();
        setupTranslators();

        translationToggle.setOnClickListener(v -> toggleLanguage());
    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                // Already on home (ReservationActivity), do nothing or refresh
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

        // Highlight the current tab if needed
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



    private void setupDateFilter() {
        dateFilterButton.setOnClickListener(v -> {
            Log.d("HomeActivity", "Date filter button clicked");
            isStartDateSet = false;
            isEndDateSet = false;
            showDatePickerDialog(true);
        });
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCalendar : endDateCalendar;
        String title = isStartDate ? "Select Start Date" : "Select End Date";

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (isStartDate) {
                        isStartDateSet = true;
                        showDatePickerDialog(false);
                    } else {
                        isEndDateSet = true;
                        performDateRangeSearch();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setTitle(title);
        datePickerDialog.show();
    }

    private void performDateRangeSearch() {
        if (isStartDateSet && isEndDateSet) {
            if (endDateCalendar.before(startDateCalendar)) {
                showToast("End date cannot be before start date");
                return;
            }

            String startDate = dateFormat.format(startDateCalendar.getTime());
            String endDate = dateFormat.format(endDateCalendar.getTime());
            searchView.setQuery(startDate + " to " + endDate, false);
            searchByDateRange(startDate, endDate);
        }
    }

    private void setupTranslators() {
        TranslatorOptions frToEnOptions = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.FRENCH)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build();
        frenchEnglishTranslator = Translation.getClient(frToEnOptions);

        TranslatorOptions enToFrOptions = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.FRENCH)
                .build();
        englishFrenchTranslator = Translation.getClient(enToFrOptions);

        frenchEnglishTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> showToast("French-English translator ready"))
                .addOnFailureListener(e -> showToast("Error downloading French-English model: " + e.getMessage()));

        englishFrenchTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> showToast("English-French translator ready"))
                .addOnFailureListener(e -> showToast("Error downloading English-French model: " + e.getMessage()));
    }

    private void toggleLanguage() {
        isEnglish = !isEnglish;
        translationToggle.setText(isEnglish ? "EN/FR" : "FR/EN");
        showToast("Switched to " + (isEnglish ? "English" : "French"));
        translateUIContent();
    }

    private void translateUIContent() {
        for (int i = 0; i < spectacleList.size(); i++) {
            final Spectacle spectacle = spectacleList.get(i);
            final int position = i;
            Translator translator = isEnglish ? frenchEnglishTranslator : englishFrenchTranslator;

            // Translate title
            translator.translate(spectacle.getTitre())
                    .addOnSuccessListener(translatedText -> {
                        if (spectacle.getOriginalTitle() == null) {
                            spectacle.setOriginalTitle(spectacle.getTitre());
                        }
                        spectacle.setTitre(translatedText);
                        adapter.notifyItemChanged(position);
                    })
                    .addOnFailureListener(e -> showToast("Translation failed: " + e.getMessage()));

            // Translate description
            if (spectacle.getDescription() != null && !spectacle.getDescription().isEmpty()) {
                translator.translate(spectacle.getDescription())
                        .addOnSuccessListener(translatedText -> {
                            if (spectacle.getOriginalDescription() == null) {
                                spectacle.setOriginalDescription(spectacle.getDescription());
                            }
                            spectacle.setDescription(translatedText);
                            adapter.notifyItemChanged(position);
                        })
                        .addOnFailureListener(e -> Log.e("Translation", "Failed to translate description: " + e.getMessage()));
            }

        }

        translateTextView(findViewById(R.id.searchView));

    }


    private void translateTextView(final TextView textView) {
        if (textView == null || textView.getText() == null) return;

        String originalText = textView.getText().toString();
        if (originalText.isEmpty()) return;

        if (textView.getTag() == null) {
            textView.setTag(originalText);
        }

        Translator translator = isEnglish ? frenchEnglishTranslator : englishFrenchTranslator;
        translator.translate(originalText)
                .addOnSuccessListener(textView::setText)
                .addOnFailureListener(e -> Log.e("Translation", "Failed to translate text: " + e.getMessage()));
    }


    private void navigateToProfile() {
        if (UserSession.isLoggedIn(this)) {
            // User is logged in - proceed to profile
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            startActivity(profileIntent);
        } else {
            // Show login dialog with better UX
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.login_required)
                    .setMessage(R.string.profile_login_prompt)
                    .setPositiveButton(R.string.login, (dialog, which) -> {
                        Intent loginIntent = new Intent(this, LoginActivity.class);
                        // Add flag to return to profile after successful login
                        loginIntent.putExtra("redirect_to_profile", true);
                        startActivity(loginIntent);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }



    private void loadAllSpectacles() {
        Call<List<Spectacle>> call = apiService.getAllSpectacles();
        call.enqueue(new Callback<List<Spectacle>>() {
            @Override
            public void onResponse(Call<List<Spectacle>> call, Response<List<Spectacle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spectacleList.clear();
                    spectacleList.addAll(response.body());
                    Log.d("Spectacles", "Loaded " + spectacleList.size() + " items");

                    for (Spectacle spectacle : spectacleList) {
                        if (spectacle.getDescription() == null || spectacle.getDescription().isEmpty()) {
                            spectacle.setDescription(getString(R.string.no_description_available));
                        }

                        // Mark spectacles that are bookmarked
                        if (bookmarkedIds.contains(String.valueOf(spectacle.getIdSpec()))) {
                            spectacle.setBookmarked(true);
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("Spectacles", "Response failed: " + response.code());
                    showToast("Failed to load spectacles.");
                }
            }

            @Override
            public void onFailure(Call<List<Spectacle>> call, Throwable t) {
                Log.e("Spectacles", "Error loading spectacles", t);
                showToast("Error: " + t.getMessage());
            }
        });
    }
    // Fixed onSaveClick method in HomeActivity
    @Override
    public void onSaveClick(Spectacle spectacle, int position) {
        if (!UserSession.isLoggedIn(this)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.login_required)
                    .setMessage(R.string.bookmark_login_prompt)
                    .setPositiveButton(R.string.login, (dialog, which) -> {
                        Intent loginIntent = new Intent(this, LoginActivity.class);
                        startActivity(loginIntent);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }

        Long userId = UserSession.getUserId(this);
        Long spectacleId = spectacle.getIdSpec();

        boolean newStatus = !spectacle.isBookmarked();
        spectacle.setBookmarked(newStatus);
        adapter.notifyItemChanged(position);

        String spectacleIdStr = String.valueOf(spectacleId);
        if (newStatus) {
            bookmarkedIds.add(spectacleIdStr);
        } else {
            bookmarkedIds.remove(spectacleIdStr);
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("bookmarked_spectacles", bookmarkedIds);
        editor.apply();

        // Make API call to toggle bookmark on server
        Call<Boolean> call = apiService.toggleBookmark(userId, spectacleId);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (!response.isSuccessful()) {
                    // Revert UI and local storage if server call fails
                    spectacle.setBookmarked(!newStatus);
                    adapter.notifyItemChanged(position);

                    if (!newStatus) {
                        bookmarkedIds.add(spectacleIdStr);
                    } else {
                        bookmarkedIds.remove(spectacleIdStr);
                    }

                    editor.putStringSet("bookmarked_spectacles", bookmarkedIds);
                    editor.apply();

                    showToast("Failed to update bookmark");
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                // Revert UI and local storage if network call fails
                spectacle.setBookmarked(!newStatus);
                adapter.notifyItemChanged(position);

                if (!newStatus) {
                    bookmarkedIds.add(spectacleIdStr);
                } else {
                    bookmarkedIds.remove(spectacleIdStr);
                }

                editor.putStringSet("bookmarked_spectacles", bookmarkedIds);
                editor.apply();

                showToast("Network error");
            }
        });
    }

    // Add this method to HomeActivity.java - Load bookmarks when user logs in
    @Override
    protected void onResume() {
        super.onResume();

        // Synchronize bookmarks with server when activity resumes
        if (UserSession.isLoggedIn(this)) {
            Long userId = UserSession.getUserId(this);
            loadBookmarksForUser(userId);
        }
    }
    private void loadBookmarksForUser(Long userId) {
        if (userId == null) return;

        apiService.getUserBookmarks(userId).enqueue(new Callback<List<Bookmark>>() {
            @Override
            public void onResponse(Call<List<Bookmark>> call, Response<List<Bookmark>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bookmarkedIds.clear();
                    for (Bookmark bookmark : response.body()) {
                        bookmarkedIds.add(String.valueOf(bookmark.getSpectacleId()));
                    }
                    markBookmarkedSpectacles();
                }
            }

            @Override
            public void onFailure(Call<List<Bookmark>> call, Throwable t) {
                Log.e("Bookmark", "Failed to load bookmarks", t);
            }
        });
    }

    // Replace saveBookmarkToServer with this improved version:
    private void saveBookmarkToServer(Long spectacleId, int position) {
        Log.d("Bookmark", "Attempting to save bookmark with ID: " + spectacleId);

        Call<Bookmark> call = apiService.bookmarkSpectacle(spectacleId);
        call.enqueue(new Callback<Bookmark>() {
            @Override
            public void onResponse(Call<Bookmark> call, Response<Bookmark> response) {
                if (response.isSuccessful()) {
                    // Success case: bookmark saved
                    Log.d("Bookmark", "Bookmark saved successfully");
                    showToast("Spectacle saved successfully");

                    // Update the local storage
                    bookmarkedIds.add(String.valueOf(spectacleId));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putStringSet("bookmarked_spectacles", bookmarkedIds);
                    editor.apply();
                } else {
                    // Error case: server returned an unsuccessful response
                    Log.e("Bookmark", "Failed to save bookmark: " + response.code());
                    showToast("Failed to save. Please try again.");

                    // Revert UI change since server operation failed
                    if (position < spectacleList.size()) {
                        Spectacle spectacle = spectacleList.get(position);
                        spectacle.setBookmarked(false);
                        adapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onFailure(Call<Bookmark> call, Throwable t) {
                // Failure case: network error or unexpected exception
                Log.e("Bookmark", "Network error when saving bookmark: " + t.getMessage(), t);
                showToast("Network error. Please check your connection.");

                // Revert UI change since network operation failed
                if (position < spectacleList.size()) {
                    Spectacle spectacle = spectacleList.get(position);
                    spectacle.setBookmarked(false);
                    adapter.notifyItemChanged(position);
                }
            }
        });
    }
    // Replace removeBookmarkFromServer with this improved version:
    private void removeBookmarkFromServer(Long spectacleId, int position) {
        Log.d("Bookmark", "Attempting to remove bookmark with ID: " + spectacleId);

        Call<Void> call = apiService.unbookmarkSpectacle(spectacleId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Bookmark", "Bookmark removed successfully");
                    showToast("Removed from saved items");

                    // Update the local storage
                    bookmarkedIds.remove(String.valueOf(spectacleId));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putStringSet("bookmarked_spectacles", bookmarkedIds);
                    editor.apply();
                } else {
                    Log.e("Bookmark", "Failed to remove bookmark: " + response.code());
                    showToast("Failed to remove saved item. Please try again.");

                    // Revert UI change since server operation failed
                    if (position < spectacleList.size()) {
                        Spectacle spectacle = spectacleList.get(position);
                        spectacle.setBookmarked(true);
                        adapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Bookmark", "Network error when removing bookmark: " + t.getMessage());
                showToast("Network error. Please check your connection.");

                // Revert UI change since network operation failed
                if (position < spectacleList.size()) {
                    Spectacle spectacle = spectacleList.get(position);
                    spectacle.setBookmarked(true);
                    adapter.notifyItemChanged(position);
                }
            }
        });
    }

    private void markBookmarkedSpectacles() {
        // This should be called after loading spectacles
        for (Spectacle spectacle : spectacleList) {
            if (bookmarkedIds.contains(String.valueOf(spectacle.getIdSpec()))) {
                spectacle.setBookmarked(true);
            } else {
                spectacle.setBookmarked(false);
            }
        }
    }


    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.trim();
                if (query.contains("to") && query.split("to").length == 2) {
                    String[] dates = query.split("to");
                    searchByDateRange(dates[0].trim(), dates[1].trim());
                } else if (containsNumbers(query)) {
                    searchByLocation(query);
                } else {
                    searchByTitle(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadAllSpectacles();
                }
                return false;
            }
        });
    }

    private boolean containsNumbers(String query) {
        return query.matches(".*\\d+.*");
    }

    private void searchByTitle(String title) {
        apiService.searchSpectacles(title).enqueue(new Callback<List<Spectacle>>() {
            @Override
            public void onResponse(Call<List<Spectacle>> call, Response<List<Spectacle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateResults(response.body());
                } else {
                    showToast("No spectacles found for title.");
                }
            }

            @Override
            public void onFailure(Call<List<Spectacle>> call, Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void searchByDateRange(String startDate, String endDate) {
        apiService.getSpectaclesByDateRange(startDate, endDate).enqueue(new Callback<List<Spectacle>>() {
            @Override
            public void onResponse(Call<List<Spectacle>> call, Response<List<Spectacle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateResults(response.body());
                } else {
                    showToast("No spectacles found in this date range.");
                }
            }

            @Override
            public void onFailure(Call<List<Spectacle>> call, Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void searchByLocation(String location) {
        // Log the search query
        Log.d("Search", "Searching location: " + location);

        apiService.getSpectaclesByLocation(location).enqueue(new Callback<List<Spectacle>>() {
            @Override
            public void onResponse(Call<List<Spectacle>> call, Response<List<Spectacle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Spectacle> results = response.body();
                    Log.d("Search", "Found " + results.size() + " spectacles for location: " + location);
                    updateResults(results);
                } else {
                    Log.e("Search", "No spectacles found for location: " + location + ", code: " + response.code());
                    showToast("No spectacles found for location.");
                }
            }

            @Override
            public void onFailure(Call<List<Spectacle>> call, Throwable t) {
                Log.e("Search", "Error searching by location: " + t.getMessage());
                showToast("Error: " + t.getMessage());
            }
        });
    }
    private void updateResults(List<Spectacle> results) {
        spectacleList.clear();
        spectacleList.addAll(results);
        adapter.notifyDataSetChanged();
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (frenchEnglishTranslator != null) {
            frenchEnglishTranslator.close();
        }
        if (englishFrenchTranslator != null) {
            englishFrenchTranslator.close();
        }
    }
}