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

import dsd.dsdvansales.model.AttachmentInfo;
import dsd.dsdvansales.model.LoginInfo;
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


/**
 * Created by silve on 10/30/2016.
 */
public class AttachmentApiClient {

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
            builder.readTimeout(300, TimeUnit.SECONDS);
            builder.connectTimeout(300, TimeUnit.SECONDS);
            builder.writeTimeout(300, TimeUnit.SECONDS);
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
        public Call<LoginInfo> login(@FieldMap Map<String, String> options);

        @GET("api/customer-note-attachments")
        public Call<List<AttachmentInfo>> getAttachment(@Query("noteId") String noteId);
    }
}
