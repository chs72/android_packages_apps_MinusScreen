package com.okcaros.minusscreen.api.converter;


import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.okcaros.minusscreen.api.model.Weather;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class WebserviceConvert extends Converter.Factory {
    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (type == Weather.class) {
            return new Converter<ResponseBody, Weather>() {
                @Nullable
                @Override
                public Weather convert(ResponseBody value) throws IOException {
                    String bodyStr = value.string();
                    Weather res = JSON.parseObject(bodyStr, Weather.class, Feature.DisableSpecialKeyDetect);
                    if (res.weather == null) {
                        throw new IOException("");
                    }
                    return res;
                }
            };
        }
        return null;
    }
}
