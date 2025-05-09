package dsd.dsdvansales.api;

import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import dsd.dsdvansales.model.GooglePath;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.QueryMap;

/**
 * Created by silve on 10/30/2016.
 */
public class RouteApi {

    private static ApiInterface apiService;
    public static String API_ROOT = "https://routes.googleapis.com";

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

        @POST("/directions/v2:computeRoutes")
        Call<JsonObject> computeRoutes(
                @Header("Content-Type") String contentType,
                @Header("X-Goog-Api-Key") String apiKey,
                @Header("X-Goog-FieldMask") String fieldMask,
                @Body JsonObject params);
    }
}
