package dsd.dsdvansales.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dsd.dsdvansales.global.TypedFile;

import dsd.dsdvansales.model.FileInfoStatus;
import dsd.dsdvansales.model.GeneralResponse;
import dsd.dsdvansales.model.ListDirectoryResponse;
import dsd.dsdvansales.model.UploadStatus;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;

import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by silve on 10/30/2016.
 */
public class UploadApiClient {

    public static String API_ROOT = "http://54.153.250.85";
    private static ApiInterface apiMainService;

    public static ApiInterface getMainApiClient() {
        if (apiMainService == null) {

            okhttp3.OkHttpClient client = getUnsafeOkHttpClient();

            Gson gson = new GsonBuilder()
                    .create();
            retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(API_ROOT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
            apiMainService = retrofit.create(ApiInterface.class);
        }

        return apiMainService;
    }

    public static okhttp3.OkHttpClient getUnsafeOkHttpClient() {

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

            okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
            builder.readTimeout(300, TimeUnit.SECONDS);
            builder.connectTimeout(10, TimeUnit.SECONDS);
            builder.writeTimeout(300, TimeUnit.SECONDS);
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    if (API_ROOT.contains(hostname)) {
                        return true;
                    }
                    return false;
                }
            });

            okhttp3.OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public interface ApiInterface {

        @Multipart
        @POST("/dsd/index.php/Backend/uploadChunk")
        Call<UploadStatus> uploadChunk(@Part("file_handler") RequestBody file_handler, @Part("ftp_user_name") RequestBody ftp_user_name,
                                       @Part("ftp_ip_address") RequestBody ftp_ip_address, @Part("company_name") RequestBody company_name,
                                       @Part("target_file") RequestBody target_file, @Part("target_folder") RequestBody targetFolder,
                                       @Part("file_size") RequestBody file_size, @Part MultipartBody.Part source_file);

        @GET("/dsd/index.php/Backend/getFileInfo")
        Call<FileInfoStatus> getFileInfo(@Query("ftp_user_name") String ftp_user_name,
                                         @Query("ftp_ip_address") String ftp_ip_address, @Query("company_name") String company_name,
                                         @Query("target_folder") String target_folder, @Query("target_file") String target_file);

        @GET("/dsd/index.php/Backend/registerDownloadSuccess")
        Call<GeneralResponse> registerDownloadSuccess(@Query("ftp_user_name") String ftp_user_name,
                                                      @Query("ftp_ip_address") String ftp_ip_address, @Query("company_name") String company_name,
                                                      @Query("target_folder") String target_folder, @Query("target_file") String target_file);

        @GET("/dsd/index.php/Backend/getDirectoryFiles")
        Call<ListDirectoryResponse> getDirectoryFiles(@Query("ftp_user_name") String ftp_user_name,
                                                      @Query("ftp_ip_address") String ftp_ip_address, @Query("company_name") String company_name,
                                                      @Query("target_folder") String target_folder);
    }
}
