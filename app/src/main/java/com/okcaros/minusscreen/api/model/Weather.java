package com.okcaros.minusscreen.api.model;

import androidx.annotation.Keep;

@Keep
public class Weather {
    public String weather;
    public String zhWeather;
    public float temperature;
    public String winddirection;
    public String windpower;
    public float humidity;
    public String reportTime;
    public String country;
    public String countryCode;
    public String cityName;

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public void setZhWeather(String zhWeather) {
        this.zhWeather = zhWeather;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }
}
