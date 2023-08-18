package com.okcaros.minusscreen.singleton.data;

import android.content.Context;

import com.okcaros.minusscreen.api.model.Weather;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class DataManager {
    public final String DataManagerTag = "DataManager";

    private Weather weather;

    @Inject
    public DataManager(@ApplicationContext Context context) {

    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }
}
