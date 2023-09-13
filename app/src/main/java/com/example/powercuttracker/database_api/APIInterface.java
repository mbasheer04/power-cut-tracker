package com.example.powercuttracker.database_api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface APIInterface {

    @GET("/getuser/{id}")
    Call<User> getUserById(@Path("id") Long id);


}
