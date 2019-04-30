package cn.com.uama.imageuploader.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Collections;

import cn.com.uama.imageuploader.LMImageUploader;
import cn.com.uama.imageuploader.SimpleConfig;
import cn.com.uama.imageuploader.UploadListener;
import cn.com.uama.imageuploader.UploadType;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LMImageUploader.init(new SimpleConfig() {
            @Override
            public String uploadUrl() {
                return "";
            }
        });
        LMImageUploader.compressAndUpload(this, Collections.<String>emptyList(),
                UploadType.COMMUNITY,
                new UploadListener() {
                    @Override
                    public void onSuccess(String s) {

                    }

                    @Override
                    public void onError(String s, String s1) {

                    }
                });
    }
}
