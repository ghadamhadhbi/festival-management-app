package com.example.festiv.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.festiv.Models.Client;
import com.example.festiv.R;
import com.example.festiv.api.ApiService;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.0.187:9090/";
    private Button createAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        createAccountBtn = findViewById(R.id.create_account_button);

        createAccountBtn.setOnClickListener(v -> {
            String firstName = ((EditText) findViewById(R.id.firstnameText)).getText().toString().trim();
            String lastName = ((EditText) findViewById(R.id.lastnameText)).getText().toString().trim();
            String email = ((EditText) findViewById(R.id.editTextTextEmailAddress2)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.editTextTextPassword2)).getText().toString().trim();
            String tel = "21628799685";

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            TextView alreadyHaveAccountText = findViewById(R.id.login_link);
            alreadyHaveAccountText.setOnClickListener(y -> {
                startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
            });

            Client client = new Client(lastName, firstName, tel, email, password);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService service = retrofit.create(ApiService.class);

            Call<ResponseBody> call = service.registerClient(client);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body() != null ? response.body().string() : "";
                            if (responseBody.contains("SUCCESS")) {
                                Toast.makeText(CreateAccountActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                showError(response.code(), "Account creation failed: " + responseBody);
                            }
                        } catch (IOException e) {
                            showError(0, "Response parsing error: " + e.getMessage());
                        }
                    } else {
                        showError(response.code(), "Server error: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    showError(0, "Network error: " + t.getMessage());
                }
            });
        });
    }

    private void showError(int code, String message) {
        String errorMsg = "Error";
        if (code != 0) {
            errorMsg += " " + code;
        }
        errorMsg += ": " + message;
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }


}
