package dsd.dsdvansales.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dsd.dsdvansales.model.ArchiveInfo;
import dsd.dsdvansales.model.CustomerNoteResponse;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.EquipmentMaster;
import dsd.dsdvansales.model.LoadHead;
import dsd.dsdvansales.model.LoadHeadInfo;
import dsd.dsdvansales.model.LoadTripResponse;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.RouteVersionDetailInfo;
import dsd.dsdvansales.model.RouteVersionInfo;
import dsd.dsdvansales.model.TripResponse;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;


/**
 * Created by silve on 10/30/2016.
 */
public class ConnectApiClient {

    static String gBaseURL = "";
    public static ApiInterface getMainApiClient(String strBaseUrl, Map<String, String> headers) {

        gBaseURL = strBaseUrl;

        OkHttpClient client = getUnsafeOkHttpClient(headers);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(strBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        ApiInterface apiMainService = retrofit.create(ApiInterface.class);
        return apiMainService;
    }

    public static OkHttpClient getUnsafeOkHttpClient(final Map<String, String> headers) {

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
                    if (gBaseURL.contains(hostname)) {
                        return true;
                    }
                    return false;
                }
            });

            if(headers != null && headers.size() > 0)
            {
                builder.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request original = chain.request();

                        Request.Builder builder = original.newBuilder();
                        Iterator iterator = headers.keySet().iterator();
                        while(iterator.hasNext())
                        {
                            String key = (String)iterator.next();
                            String value = headers.get(key);
                            builder.addHeader(key, value);
                        }
                        builder.method(original.method(), original.body());
                        Request request = builder.build();

                        return chain.proceed(request);
                    }
                });
            }


            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface ApiInterface {

        @FormUrlEncoded
        @POST("api/login")
        Call<LoginInfo> login(@FieldMap Map<String, String> options);

        @GET("api/sales-order")
        Call<List<CustomerOrderInfo>> getSalesOrder(@QueryMap Map<String, String> options);

        @GET("api/sales-order/delay")
        Call<List<CustomerOrderInfo>> delay(@QueryMap Map<String, String> options);

        @GET("api/route-version-names")
        Call<List<RouteVersionInfo>> getRouteVersionNames(@QueryMap Map<String, String> options);

        @GET("api/route-version-details")
        Call<List<RouteVersionDetailInfo>> getRouteVersionDetails(@QueryMap Map<String, String> options);

        @FormUrlEncoded
        @POST("api/customer-notes")
        Call<List<CustomerNoteResponse>> getCustomerNotes(@FieldMap Map<String, String> options);

        @GET("api/document-archive/list")
        Call<List<ArchiveInfo>> getDocumentArchiveList(@Query("customerNumber") int nCustNo, @Query("startDate") String strStartDate, @Query("endDate") String strEndDate);

        @GET("api/load")
        Call<LoadTripResponse> getLoad(@Query("tripBalanceHeaderId") String strTripId);

        @GET("api/trip")
        Call<List<TripResponse>> getTripForToday(@Query("date") String strDate);

        @GET("api/equipment-master")
        Call<List<EquipmentMaster>> getEquipmentMaster(@Query("equipmentIDtype") String strEquipmentIDType, @Query("equipmentNo") String strEquipmentID);

    }
}
