package dsd.dsdvansales.api;

import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import dsd.dsdvansales.model.GooglePath;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.QueryMap;

/**
 * Created by silve on 10/30/2016.
 */
public class DirectionApi {

    private static ApiInterface apiService;
    public static String API_ROOT = "https://maps.googleapis.com";

    private static OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(30, TimeUnit.MINUTES);
        client.setReadTimeout(30, TimeUnit.MINUTES);
        return client;
    }

    public static ApiInterface getApiClient() {
        Retrofit retrofit = (new Retrofit.Builder()).baseUrl(API_ROOT).
                addConverterFactory(GsonConverterFactory.create()).client(getClient()).build();

        ApiInterface adapter = retrofit.create(ApiInterface.class);
        return adapter;
    }

    public interface ApiInterface {

        @GET("/maps/api/directions/json")
        Call<JsonObject> getDirection(@QueryMap Map<String, String> options);
    }
}
