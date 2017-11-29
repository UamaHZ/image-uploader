package cn.com.uama.imageuploader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
