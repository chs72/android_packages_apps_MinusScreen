package com.okcaros.minusscreen.api.request;

import com.okcaros.minusscreen.api.model.Weather;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;

public interface WebserviceApi {
    @GET("weather")
    Observable<Weather> getWeatherInfo();
}
