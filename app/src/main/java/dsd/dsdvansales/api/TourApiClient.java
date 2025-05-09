package dsd.dsdvansales.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dsd.dsdvansales.model.InstallerVersionInfo;
import dsd.dsdvansales.model.TourInfoResult;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by silve on 10/30/2016.
 */
public class TourApiClient {

    private static ApiInterface apiService;
    public static String TOUR_API_ROOT = "https://installer.dsdassist.com/";
    public static ArrayList<String> allowedHostnames = new ArrayList<String>();

    public static OkHttpClient getUnsafeOkHttpClient() {

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    if (TOUR_API_ROOT.contains(hostname)) {
                        return true;
                    }
                    return false;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static ApiInterface getApiClient() {
        /*Retrofit retrofit = (new Retrofit.Builder()).baseUrl(API_ROOT).
                addConverterFactory(GsonConverterFactory.create()).
                client(getClient()).build();
        ApiInterface adapter = retrofit.create(ApiInterface.class);
        return adapter;*/

        OkHttpClient client = getUnsafeOkHttpClient();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TOUR_API_ROOT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        ApiInterface apiMainService = retrofit.create(ApiInterface.class);
        return apiMainService;
    }

    public interface ApiInterface {

        @GET("dsd/index.php/Product/getTourInfo")
        Call<TourInfoResult> getTourInfo(@QueryMap Map<String, String> options);

        @GET("dsd/index.php/Product/getLatestInstallerVersion")
        Call<InstallerVersionInfo> getLatestInstallerVersion(@QueryMap Map<String, String> options);
    }
}
