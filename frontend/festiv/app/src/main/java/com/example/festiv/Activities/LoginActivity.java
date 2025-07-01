package com.example.festiv.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.festiv.Models.Client;
import com.example.festiv.Models.UserSession;
import com.example.festiv.R;
import com.example.festiv.api.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.7.181:9090/"; // Update for localhost testing
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        Button loginButton = findViewById(R.id.button2);
        TextView createAccountText = findViewById(R.id.textView2);



        loginButton.setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.editTextTextEmailAddress)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.editTextTextPassword)).getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest loginRequest = new LoginRequest(email, password);
            Call<Client> call = apiService.login(loginRequest);

            call.enqueue(new Callback<Client>() {
                @Override
                public void onResponse(Call<Client> call, Response<Client> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Client client = response.body();

                        // Save the ID properly
                        UserSession.saveUserId(LoginActivity.this, client.getIdclt());

                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        boolean redirectToProfile = getIntent().getBooleanExtra("redirect_to_profile", false);

                        if (redirectToProfile) {
                            Intent profileIntent = new Intent(LoginActivity.this, ProfileActivity.class);
                            startActivity(profileIntent);
                        } else {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Client> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        createAccountText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });
    }
}
