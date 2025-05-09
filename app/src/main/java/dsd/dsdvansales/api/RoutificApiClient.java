package dsd.dsdvansales.api;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.util.concurrent.TimeUnit;

import dsd.dsdvansales.model.RoutificJob;
import dsd.dsdvansales.model.RoutificResponse;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by silve on 10/30/2016.
 */
public class RoutificApiClient {

    public static String API_ROOT = "https://api.routific.com/";

    private static OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient();

        client.setConnectTimeout(30, TimeUnit.MINUTES);
        client.setReadTimeout(30, TimeUnit.MINUTES);
        return client;
    }

    public static ApiInterface getApiClient() {
        Retrofit retrofit = (new Retrofit.Builder()).baseUrl(API_ROOT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getClient())
                .build();

        ApiInterface adapter = retrofit.create(ApiInterface.class);
        return adapter;
    }

    public interface ApiInterface {

        @POST("v1/vrp-long")
        Call<RoutificJob> vrp(@Header("Authorization") String token, @Body RequestBody body);

        @GET("jobs/{id}")
        Call<RoutificResponse> getJobStatus(@Path("id") String strJobID);

        /*@GET("jobs/{id}")
        Call<ResponseBody> getJobStatusAsText(@Path("id") String strJobID);*/
    }
}
