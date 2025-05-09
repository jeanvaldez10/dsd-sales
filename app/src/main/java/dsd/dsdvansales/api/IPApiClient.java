package dsd.dsdvansales.api;

import com.squareup.okhttp.OkHttpClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import dsd.dsdvansales.model.IPAddrInfo;
import dsd.dsdvansales.model.ProductList;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by silve on 10/30/2016.
 */
public class IPApiClient {

    private static ApiInterface apiService;
    public static String API_ROOT = "http://47.89.38.69:80";

    private static OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(30, TimeUnit.MINUTES);
        client.setReadTimeout(30, TimeUnit.MINUTES);
        return client;
    }

    public static ApiInterface getApiClient() {
        Retrofit retrofit = (new Retrofit.Builder()).baseUrl(API_ROOT).addConverterFactory(GsonConverterFactory.create()).client(getClient()).build();
        ApiInterface adapter = retrofit.create(ApiInterface.class);
        return adapter;
    }

    public interface ApiInterface {

        @FormUrlEncoded
        @POST("/ip_address.php")
        Call<IPAddrInfo> getIPAddr(@FieldMap Map<String, String> options);
    }
}
