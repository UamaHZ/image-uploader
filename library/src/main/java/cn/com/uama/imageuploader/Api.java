package cn.com.uama.imageuploader;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface Api {
    @Multipart
    @POST
    Call<UploadResultBean> upload(@Url String uploadUrl, @Part List<MultipartBody.Part> partList);

    @Multipart
    @POST
    Observable<UploadResultBean> uploadObservable(@Url String uploadUrl, @Part List<MultipartBody.Part> partList);
}
