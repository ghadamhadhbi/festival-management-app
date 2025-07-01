package com.example.festiv.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.festiv.Adapters.FestivalAdapter;
import com.example.festiv.Models.Bookmark;
import com.example.festiv.Models.Lieu;
import com.example.festiv.Models.Performance;
import com.example.festiv.Models.Spectacle;
import com.example.festiv.Models.UserSession;
import com.example.festiv.R;
import com.example.festiv.api.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SavedActivity extends AppCompatActivity implements FestivalAdapter.OnSaveClickListener {

    private static final String TAG = "SavedActivity";
    private static final String PREFS_TRANSLATION_STATE = "translation_state";

    private RecyclerView recyclerView;
    private FestivalAdapter adapter;
    private List<Spectacle> spectacleList = new ArrayList<>();
    private TextView translationToggle;
    private boolean isEnglish = true;
    private Translator frenchEnglishTranslator;
    private Translator englishFrenchTranslator;
    private SharedPreferences sharedPreferences;
    private Set<String> bookmarkedIds = new HashSet<>();
    private ApiService apiService;
    private View emptyStateView;
    private CircularProgressIndicator progressIndicator;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View browseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        sharedPreferences = getSharedPreferences("BookmarkPrefs", MODE_PRIVATE);
        bookmarkedIds = new HashSet<>(sharedPreferences.getStringSet("bookmarked_spectacles", new HashSet<>()));
        isEnglish = sharedPreferences.getBoolean(PREFS_TRANSLATION_STATE, true);

        initializeViews();
        setupRecyclerView();
        setupRetrofit();
        setupUIComponents();
        setupSwipeRefresh();
        loadSavedSpectacles();
        setupBottomNavigation();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.festivalsRecyclerView);
        translationToggle = findViewById(R.id.translationToggle);
        emptyStateView = findViewById(R.id.emptyStateView);
        progressIndicator = findViewById(R.id.progressIndicator);
        emptyStateText = findViewById(R.id.emptyStateText);
        browseButton = findViewById(R.id.browseButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        translationToggle.setText(isEnglish ? "EN/FR" : "FR/EN");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FestivalAdapter(spectacleList, this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadSavedSpectacles();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupUIComponents() {
        setupTranslators();
        translationToggle.setOnClickListener(v -> toggleLanguage());
        browseButton.setOnClickListener(v -> navigateToHome());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                navigateToHome();
                return true;
            } else if (itemId == R.id.navigation_saved) {
                return true;
            } else if (itemId == R.id.navigation_profile) {
                handleProfileNavigation();
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.navigation_saved);
    }

    private void navigateToHome() {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    private void handleProfileNavigation() {
        if (UserSession.isLoggedIn(this)) {
            startActivity(new Intent(this, ProfileActivity.class));
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

    private void setupRetrofit() {
        String baseUrl = "http://192.168.7.181:9090/";

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if (UserSession.isLoggedIn(this)) {
            final Long userId = UserSession.getUserId(this);
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("User-ID", String.valueOf(userId))
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            });
        }

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(loggingInterceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedSpectacles();
    }

    private void loadSavedSpectacles() {
        if (!UserSession.isLoggedIn(this)) {
            showLoginRequiredDialog();
            return;
        }

        showLoading(true);
        loadBookmarksFromServer();
    }

    private void showLoginRequiredDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.login_required)
                .setMessage(R.string.saved_items_login_prompt)
                .setPositiveButton(R.string.login, (dialog, which) -> {
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.putExtra("redirect_to_saved", true);
                    startActivity(loginIntent);
                    finish();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> navigateToHome())
                .setCancelable(false)
                .show();
    }

    private void loadBookmarksFromServer() {
        Long userId = UserSession.getUserId(this);
        if (userId == null) {
            showLoading(false);
            showEmptyState(true);
            return;
        }

        apiService.getUserBookmarks(userId).enqueue(new Callback<List<Bookmark>>() {
            @Override
            public void onResponse(Call<List<Bookmark>> call, Response<List<Bookmark>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulBookmarkResponse(response.body());
                } else {
                    handleBookmarkLoadingError(response);
                }
            }

            @Override
            public void onFailure(Call<List<Bookmark>> call, Throwable t) {
                handleNetworkError(t, "loading bookmarks");
            }
        });
    }

    private void handleSuccessfulBookmarkResponse(List<Bookmark> bookmarks) {
        List<Spectacle> spectacles = new ArrayList<>();
        bookmarkedIds.clear();

        for (Bookmark bookmark : bookmarks) {
            if (bookmark.getSpectacle() != null) {
                Spectacle spectacle = bookmark.getSpectacle();
                spectacle.setBookmarked(true);
                spectacles.add(spectacle);
                bookmarkedIds.add(String.valueOf(spectacle.getIdSpec()));
            }
        }

        saveBookmarkedIdsToPreferences();
        updateResults(spectacles);
        showEmptyState(spectacles.isEmpty());

        if (!isEnglish) {
            translateUIContent();
        }
    }

    private void saveBookmarkedIdsToPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("bookmarked_spectacles", new HashSet<>(bookmarkedIds));
        editor.apply();
    }

    private void handleBookmarkLoadingError(Response<List<Bookmark>> response) {
        Log.e(TAG, "Server error: " + response.code());
        showToast("Unable to load saved spectacles");
        showEmptyState(true);

        if (!bookmarkedIds.isEmpty()) {
            loadSpectaclesFromLocalIds();
        }
    }

    private void loadSpectaclesFromLocalIds() {
        List<Long> ids = new ArrayList<>();
        for (String idStr : bookmarkedIds) {
            try {
                ids.add(Long.parseLong(idStr));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid ID format: " + idStr);
            }
        }

        if (ids.isEmpty()) {
            showEmptyState(true);
            showLoading(false);
            return;
        }

        apiService.getSpectaclesByIds(ids).enqueue(new Callback<List<Spectacle>>() {
            @Override
            public void onResponse(Call<List<Spectacle>> call, Response<List<Spectacle>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Spectacle> results = response.body();
                    for (Spectacle spectacle : results) {
                        spectacle.setBookmarked(true);
                    }
                    updateResults(results);
                    showEmptyState(results.isEmpty());
                } else {
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<List<Spectacle>> call, Throwable t) {
                showLoading(false);
                showEmptyState(true);
            }
        });
    }

    private void handleNetworkError(Throwable t, String operation) {
        Log.e(TAG, "Network error when " + operation, t);
        showLoading(false);
        showEmptyState(true);

        if (!bookmarkedIds.isEmpty() && operation.equals("loading bookmarks")) {
            loadSpectaclesFromLocalIds();
        }
    }

    private void showLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean isEmpty) {
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        emptyStateText.setText(getString(R.string.no_saved_spectacles));
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        browseButton.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
                .addOnFailureListener(e -> Log.e(TAG, "FR-EN model download failed", e));

        englishFrenchTranslator.downloadModelIfNeeded()
                .addOnFailureListener(e -> Log.e(TAG, "EN-FR model download failed", e));
    }

    private void toggleLanguage() {
        isEnglish = !isEnglish;
        translationToggle.setText(isEnglish ? "EN/FR" : "FR/EN");
        showToast(isEnglish ? getString(R.string.switch_to_english) : getString(R.string.switch_to_french));

        // Save translation state
        sharedPreferences.edit()
                .putBoolean(PREFS_TRANSLATION_STATE, isEnglish)
                .apply();

        translateUIContent();
    }

    private void translateUIContent() {
        for (int i = 0; i < spectacleList.size(); i++) {
            final Spectacle spectacle = spectacleList.get(i);
            final int position = i;

            translateTitle(spectacle, position);
            translateDescription(spectacle, position);
            translateVenues(spectacle, position);
        }
    }

    private void translateTitle(Spectacle spectacle, int position) {
        if (spectacle.getTitre() == null) return;

        String textToTranslate = spectacle.getOriginalTitle() != null ?
                spectacle.getOriginalTitle() : spectacle.getTitre();

        if (spectacle.getOriginalTitle() == null) {
            spectacle.setOriginalTitle(spectacle.getTitre());
        }

        getCurrentTranslator().translate(textToTranslate)
                .addOnSuccessListener(translatedText -> {
                    spectacle.setTitre(translatedText);
                    adapter.notifyItemChanged(position);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Title translation failed", e));
    }

    private void translateDescription(Spectacle spectacle, int position) {
        if (spectacle.getDescription() == null || spectacle.getDescription().isEmpty()) return;

        String descToTranslate = spectacle.getOriginalDescription() != null ?
                spectacle.getOriginalDescription() : spectacle.getDescription();

        if (spectacle.getOriginalDescription() == null) {
            spectacle.setOriginalDescription(spectacle.getDescription());
        }

        getCurrentTranslator().translate(descToTranslate)
                .addOnSuccessListener(translatedText -> {
                    spectacle.setDescription(translatedText);
                    adapter.notifyItemChanged(position);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Description translation failed", e));
    }

    private void translateVenues(Spectacle spectacle, int position) {
        if (spectacle.getPerformances() == null) return;

        for (Performance performance : spectacle.getPerformances()) {
            if (performance.getLieu() != null && performance.getLieu().getNomLieu() != null) {
                Lieu lieu = performance.getLieu();
                String lieuNameToTranslate = lieu.getOriginalNomLieu() != null ?
                        lieu.getOriginalNomLieu() : lieu.getNomLieu();

                if (lieu.getOriginalNomLieu() == null) {
                    lieu.setOriginalNomLieu(lieu.getNomLieu());
                }

                getCurrentTranslator().translate(lieuNameToTranslate)
                        .addOnSuccessListener(translatedText -> {
                            lieu.setNomLieu(translatedText);
                            adapter.notifyItemChanged(position);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Venue translation failed", e));
            }
        }
    }

    private Translator getCurrentTranslator() {
        return isEnglish ? frenchEnglishTranslator : englishFrenchTranslator;
    }

    private void updateResults(List<Spectacle> results) {
        spectacleList.clear();
        spectacleList.addAll(results);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveClick(Spectacle spectacle, int position) {
        // We shouldn't reach here if not bookmarked, but add a safety check
        if (!spectacle.isBookmarked()) {
            Log.e(TAG, "Attempting to unbookmark an item that is not bookmarked");
            return;
        }

        // Update the item's visual state first (toggle bookmark icon) but don't remove from list yet
        spectacle.setBookmarked(false);
        adapter.updateBookmarkStatus(position, false);

        // Show a toast indicating we're processing the unbookmark
        showToast(getString(R.string.removing_from_bookmarks));

        // Then remove from server
        removeBookmarkFromServer(spectacle.getIdSpec(), new BookmarkOperationCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    // Now we can actually remove the item from the list
                    spectacleList.remove(position);
                    adapter.notifyItemRemoved(position);

                    // Update local storage
                    bookmarkedIds.remove(String.valueOf(spectacle.getIdSpec()));
                    saveBookmarkedIdsToPreferences();

                    // Check if the list is now empty
                    showEmptyState(spectacleList.isEmpty());

                    showToast(getString(R.string.removed_from_saved, spectacle.getTitre()));
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    // Revert the visual bookmark state since operation failed
                    spectacle.setBookmarked(true);
                    adapter.updateBookmarkStatus(position, true);
                    showToast(getString(R.string.failed_to_remove, error));
                });
            }
        });
    }

    private void removeBookmarkFromServer(Long spectacleId, BookmarkOperationCallback callback) {
        Long userId = UserSession.getUserId(this);
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        apiService.deleteBookmarkByUserAndSpectacle(userId, spectacleId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("Server error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onFailure("Network error: " + t.getMessage());
                    }
                });
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

    interface BookmarkOperationCallback {
        void onSuccess();
        void onFailure(String error);
    }
}