package com.example.festiv.api;

import com.example.festiv.Activities.LoginRequest;
import com.example.festiv.Models.Bookmark;
import com.example.festiv.Models.Client;
import com.example.festiv.Models.Lieu;
import com.example.festiv.Models.Performance;
import com.example.festiv.Models.Reservation;
import com.example.festiv.Models.ReservationRequest;
import com.example.festiv.Models.ReservationResponse;
import com.example.festiv.Models.Spectacle;
import com.example.festiv.Models.SpectaclePrices;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/clients/login")
    Call<Client> login(@Body LoginRequest loginRequest);

    @POST("/api/clients/register")
    Call<ResponseBody> registerClient(@Body Client client);
    @GET("/api/clients/email/{email}")
    Call<Client> getClientByEmail(@Path("email") String email);

    @GET("api/spectacles")
    Call<List<Spectacle>> getAllSpectacles();

    @GET("api/spectacles/search")
    Call<List<Spectacle>> searchSpectacles(@Query("titre") String query);

    @GET("api/spectacles/date-range")
    Call<List<Spectacle>> getSpectaclesByDateRange(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);

    @GET("api/spectacles/ids")
    Call<List<Spectacle>> getSpectaclesByIds(@Query("ids") List<Long> ids);

    @GET("api/spectacles/{id}")
    Call<Spectacle> getSpectacleById(@Path("id") Long id);

    @GET("api/spectacles/lieu/{lieuId}")
    Call<List<Spectacle>> getSpectaclesByLieu(@Path("lieuId") Long lieuId);
   /* @GET("clients/{clientId}/reservations")
    Call<List<Reservation>> getUserReservations(@Path("clientId") Long clientId);*/


    @POST("api/reservations/guest")
    Call<ReservationResponse> createGuestReservation(@Body ReservationRequest request);

    @GET("api/clients/{clientId}")
    Call<Client> getClient(@Path("clientId") Long clientId);



    // This might need adjustment based on your backend implementation
    @GET("api/spectacles/location/{location}")
    Call<List<Spectacle>> getSpectaclesByLocation(@Path("location") String location);


    @GET("api/reservations/client/{clientId}")
    Call<List<Reservation>> getClientReservations(@Path("clientId") Long clientId);
    // Alternative create bookmark endpoint (simpler)
    @POST("api/bookmarks/add/{spectacleId}")
    Call<Bookmark> bookmarkSpectacle(@Path("spectacleId") Long spectacleId);



    // Delete bookmark by user and spectacle
    @DELETE("api/bookmarks/user/{userId}/spectacle/{spectacleId}")
    Call<Void> deleteBookmarkByUserAndSpectacle(@Path("userId") Long userId,
                                                @Path("spectacleId") Long spectacleId);


    @GET("api/bookmarks/check")
    Call<Boolean> checkBookmark(
            @Query("userId") Long userId,
            @Query("spectacleId") Long spectacleId
    );

    // For toggling bookmark
    @POST("api/bookmarks/toggle")
    Call<Boolean> toggleBookmark(
            @Query("userId") Long userId,
            @Query("spectacleId") Long spectacleId
    );
    @GET("api/bookmarks/user/{userId}")
    Call<List<Bookmark>> getUserBookmarks(@Path("userId") Long userId);




    // Unbookmark a spectacle (alternative endpoint)
    @DELETE("api/bookmarks/remove/{spectacleId}")
    Call<Void> unbookmarkSpectacle(@Path("spectacleId") Long spectacleId);

    @GET("api/performances")
    Call<List<Performance>> getAllPerformances();

    @GET("api/performances/spectacle/{spectacleId}")
    Call<List<Performance>> getPerformancesBySpectacle(@Path("spectacleId") Long spectacleId);

    @GET("api/performances/{id}")
    Call<Performance> getPerformanceById(@Path("id") Long id);

    // Reservations
    @POST("api/reservations")
    Call<ReservationResponse> createReservation(@Body ReservationRequest request);

    @GET("api/reservations/client/{clientId}")
    Call<List<Reservation>> getReservationsByClient(@Path("clientId") Long clientId);

    @GET("api/reservations/{id}")
    Call<Reservation> getReservationById(@Path("id") Long id);

    // Delete reservation
    @DELETE("api/reservations/{id}")
    Call<Void> cancelReservation(@Path("id") Long id);

    // Add to ApiService.java
    @GET("/api/lieux/{id]")
    Call<Lieu> getLieuById(@Path("id") Long  venueId);

    // In ApiService.java
    @GET("api/performances/{performanceId}/lieu")
    Call<Lieu> getLieuByPerformanceId(@Path("performanceId") Long performanceId);


    @GET("api/performances/{id}/with-spectacle")
    Call<Performance> getPerformanceWithSpectacle(@Path("id") Long performanceId);

    @GET("performances/{idPerformance}/prices")
    Call<SpectaclePrices> getSpectaclePrices(@Path("idPerformance") int performanceId);



    // New method to get only upcoming performances for a spectacle
    @GET("performances/upcoming/spectacle/{spectacleId}")
    Call<List<Performance>> getUpcomingPerformancesBySpectacle(@Path("spectacleId") int spectacleId);

}