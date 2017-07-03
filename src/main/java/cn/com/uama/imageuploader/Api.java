package cn.com.uama.imageuploader;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {
    @Multipart
    @POST("upload")
    Call<UploadResultBean> upload(@Part List<MultipartBody.Part> partList);
}
