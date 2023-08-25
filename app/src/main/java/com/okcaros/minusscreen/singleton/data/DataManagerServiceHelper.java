package com.okcaros.minusscreen.singleton.data;

import android.os.SystemClock;

import com.alibaba.fastjson.JSON;
import com.okcaros.minusscreen.MinusScreenService;
import com.okcaros.minusscreen.api.ApiConst;
import com.okcaros.minusscreen.api.converter.WebserviceConvert;
import com.okcaros.minusscreen.api.model.Weather;
import com.okcaros.minusscreen.api.request.WebserviceApi;
import com.okcaros.minusscreen.logger.OLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class DataManagerServiceHelper {
    public static final String DataManagerServiceHelperTag = "DataManagerServiceHelper";
    public MinusScreenService minusScreenService;
    public DataManager dataManager;
    private Retrofit retrofit;
    private WebserviceApi webserviceApi;
    private long lastWeatherGotTime = 0;
    private static final long THIRTY_MINUTES = 30 * 60 * 1000; // 30 minutes in milliseconds
    public DataManagerServiceHelper(MinusScreenService minusScreenService, DataManager dataManager) {
        this.minusScreenService = minusScreenService;
        this.dataManager = dataManager;

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new HeaderInterceptor());
        retrofit = new Retrofit.Builder()
                .baseUrl(ApiConst.getBackendUrl())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(new WebserviceConvert())
                .client(httpClient.build())
                .build();

        webserviceApi = retrofit.create(WebserviceApi.class);
    }

    public class HeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();

            Request newRequest = originalRequest.newBuilder()
                    .header("tsss", "-okcaros-!_+_!-soracko-")
                    .build();

            return chain.proceed(newRequest);
        }
    }

    public void onCreate() {
        EventBus.getDefault().register(this);

        EventBus.getDefault().post(new DataManagerServiceHelper.ManualRefreshWeather());
    }

    private Observable<Weather> updateWeather() {
        return webserviceApi
                .getWeatherInfo()
                .map(new Function<Weather, Weather>() {
                    @Override
                    public Weather apply(Weather weather) {
                        String prevWeatherStr = dataManager.getWeather() == null ? "" : JSON.toJSONString(dataManager.getWeather());
                        String nowWeatherStr = JSON.toJSONString(weather);
                        dataManager.setWeather(weather);
                        if (!nowWeatherStr.contentEquals(prevWeatherStr)) {
                            EventBus.getDefault().post(new OnWeatherChangeEvent(weather));
                        }
                        return weather;
                    }
                });
    }

    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    /**
     * refresh weather when click weather widget.
     *
     * @param event
     */
    @Subscribe
    public void ManualRefreshWeather(ManualRefreshWeather event) {
        long currentTime = SystemClock.uptimeMillis();

        if (lastWeatherGotTime == 0 || currentTime - lastWeatherGotTime >= THIRTY_MINUTES) {
            Disposable d = updateWeather()
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            s -> {
                                lastWeatherGotTime = currentTime;
                                OLog.d(DataManagerServiceHelperTag, "updateWeather success");
                            },
                            error -> {
                                OLog.e(DataManagerServiceHelperTag, "updateWeather error when ManualRefreshWeather");
                                OLog.e(DataManagerServiceHelperTag, error);
                            }
                    );
        }
    }

    public static class OnWeatherChangeEvent {
        public Weather weather;

        public OnWeatherChangeEvent(Weather weather) {
            this.weather = weather;
        }
    }

    public static class ManualRefreshWeather {

    }
}
