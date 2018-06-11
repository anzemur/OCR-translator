package com.example.murg.googleocrtranslator;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by MurG on 11-Jun-18.
 */

public interface ApiClient {

    /* Heroku API URL */
    String BASE_URL = "https://ocr-google-translate-api.herokuapp.com/api/v1/";

    @FormUrlEncoded
    @POST("translate")
    Call<TranslatedText> translateString(@Field("text") String textToTranslate, @Field("language") String languageTo);



}
