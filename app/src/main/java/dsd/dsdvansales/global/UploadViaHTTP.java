package dsd.dsdvansales.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.api.UploadApiClient;
import dsd.dsdvansales.model.UploadStatus;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadViaHTTP {

    public int _file_handler = 0;
    public boolean _error = false;

    public boolean uploadFileToFTPServerUsingHttp(String strCompanyName, String strServerIP, String strUserName, String strLocalFilePath, String strServerFilePath) {
        if (strLocalFilePath.length() == 0 || strServerFilePath.length() == 0 || Utils.isFileExist(strLocalFilePath) == false) {
            return false;
        }
        try {
            long max_buffer_size = 1000000;
            long fileSize = Utils.getFileSize(strLocalFilePath);
            long offset = 0;
            File localFile = new File(strLocalFilePath);
            FileInputStream fileInputStream = new FileInputStream(localFile);
            File tmpFile = new File(Global.ROOT_LOCATION +  localFile.getName() + System.currentTimeMillis() + ".tmp");
            byte[] max_buffer = new byte[(int) max_buffer_size];
            boolean status = true;
            _file_handler = 0;
            while (offset < fileSize && status) {
                byte[] buffer;
                if (fileSize - offset < max_buffer_size) {
                    long size = fileSize - offset;
                    buffer = new byte[(int) size];
                } else {
                    buffer = max_buffer;
                }
                fileInputStream.read(buffer);
                FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
                fileOutputStream.write(buffer);
                fileOutputStream.close();

                Map<String, Object> params = new HashMap<>();
                params.put("file_handler", String.valueOf(_file_handler));
                params.put("ftp_user_name", strUserName);
                params.put("ftp_ip_address", strServerIP);
                params.put("company_name", strCompanyName);
                params.put("target_file", Utils.getFileName(strServerFilePath));
                params.put("target_folder", Utils.getFolderPath(strServerFilePath));
                params.put("file_size", String.valueOf(fileSize));

                TypedFile typedFile = new TypedFile("*/*", tmpFile);
                params.put("source_file", typedFile);
                try {

                    final CountDownLatch latch = new CountDownLatch(1);
                    _error = false;
                    RequestBody file_handler = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(_file_handler));
                    RequestBody ftp_user_name = RequestBody.create(MediaType.parse("multipart/form-data"), strUserName);
                    RequestBody ftp_ip_address = RequestBody.create(MediaType.parse("multipart/form-data"), strServerIP);
                    RequestBody company_name = RequestBody.create(MediaType.parse("multipart/form-data"), strCompanyName);
                    RequestBody target_file = RequestBody.create(MediaType.parse("multipart/form-data"), Utils.getFileName(strServerFilePath));
                    RequestBody target_folder = RequestBody.create(MediaType.parse("multipart/form-data"), Utils.getFolderPath(strServerFilePath));
                    RequestBody file_size = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(fileSize));
                    RequestBody requestFile =
                            RequestBody.create(MediaType.parse("multipart/form-data"), tmpFile);
                    MultipartBody.Part source_file =
                            MultipartBody.Part.createFormData("source_file", tmpFile.getName(), requestFile);

                    UploadApiClient.getMainApiClient().uploadChunk(file_handler, ftp_user_name,
                            ftp_ip_address, company_name, target_file, target_folder,
                            file_size, source_file).enqueue(new Callback<UploadStatus>() {
                        @Override
                        public void onResponse(Call<UploadStatus> call, Response<UploadStatus> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().result.equals("1")) {
                                _file_handler = response.body().file_handler;
                            } else {
                                _error = true;
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Call<UploadStatus> call, Throwable t) {
                            _error = true;
                            latch.countDown();
                        }

                    });
                    latch.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (_error) {
                    status = false;
                    break;
                }
                offset += buffer.length;
            }

            fileInputStream.close();

            if (status == true) {

				/*File file = new File(strLocalFilePath);
				if (strLocalFilePath.startsWith(Global.ROOT_LOCATION)) {
					file.renameTo(new File(strLocalFilePath + "_"));
				}
				if (strLocalFilePath.endsWith(".pdf")) {
					file = new File(strLocalFilePath.substring(0, strLocalFilePath.length() - 4) + "_.pdf");
					file.delete();
				}*/
            }
            Utils.printDebug(null, "uploadFile - status = " + status);
            if(Utils.isFileExist(tmpFile.getAbsolutePath()))
            {
                Utils.fileDelete(tmpFile.getAbsolutePath());
            }
            return status;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
