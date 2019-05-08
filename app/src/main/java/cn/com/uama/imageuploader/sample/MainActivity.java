package cn.com.uama.imageuploader.sample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.util.Collections;
import java.util.List;

import cn.com.uama.imageuploader.LMImageUploader;
import cn.com.uama.imageuploader.SimpleConfig;
import cn.com.uama.imageuploader.UploadListener;
import cn.com.uama.imageuploader.UploadType;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE_IMAGE = 1;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;

    TextView selectedImagePathView;
    TextView uploadResultView;
    EditText uploadUrlEditText;
    ProgressBar progressBar;

    private String selectedImagePath;
    private UploadListener uploadListener = new UploadListener() {
        @Override
        public void onSuccess(String imageUrls) {
            onUploadComplete();
            uploadResultView.setText("上传成功，" + imageUrls);
        }

        @Override
        public void onError(String errorCode, String errorMessage) {
            onUploadComplete();
            uploadResultView.setText("上传失败，errorCode：" + errorCode + "，errorMessage：" + errorMessage);
        }
    };
    private CompositeDisposable disposables = new CompositeDisposable();
    private Observer<String> uploadObserver = new Observer<String>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposables.add(d);
        }

        @Override
        public void onNext(String s) {
            uploadResultView.setText("上传成功，" + s);
        }

        @Override
        public void onError(Throwable e) {
            uploadResultView.setText("上传失败，errorMessage：" + e.getMessage());
            onUploadComplete();
        }

        @Override
        public void onComplete() {
            onUploadComplete();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedImagePathView = findViewById(R.id.tv_selected_image_path);
        uploadResultView = findViewById(R.id.tv_upload_result);
        uploadUrlEditText = findViewById(R.id.et_upload_url);
        progressBar = findViewById(R.id.progress_bar);
        findViewById(R.id.button_choose_image).setOnClickListener(this);
        findViewById(R.id.button_upload).setOnClickListener(this);
        findViewById(R.id.button_compress_and_upload).setOnClickListener(this);
        findViewById(R.id.button_upload_observable).setOnClickListener(this);
        findViewById(R.id.button_compress_and_upload_observable).setOnClickListener(this);

        if (!LMImageUploader.isInit()) {
            showInitDialog();
        }
    }

    /**
     * 显示初始化对话框，输入上传地址
     */
    private void showInitDialog() {
        final EditText editText = new EditText(this);
        editText.setHint("请输入上传地址");
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this)
                .setTitle("初始化")
                .setView(editText)
                .setCancelable(false)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String uploadUrl = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(uploadUrl)) {
                            showInitDialog();
                            return;
                        }
                        LMImageUploader.init(new SimpleConfig() {
                            @Override
                            public String uploadUrl() {
                                return uploadUrl;
                            }
                        });
                    }
                })
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_choose_image:
                requestStoragePermission();
                break;
            case R.id.button_upload:
                upload();
                break;
            case R.id.button_compress_and_upload:
                compressAndUpload();
                break;
            case R.id.button_upload_observable:
                uploadObservable();
                break;
            case R.id.button_compress_and_upload_observable:
                compressAndUploadObservable();
                break;
        }
    }

    /**
     * 请求存储权限
     */
    private void requestStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            chooseImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
                chooseImage();
            }
        }
    }

    private void chooseImage() {
        Matisse.from(this)
                .choose(MimeType.ofImage())
                .forResult(REQUEST_CODE_CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE) {
            if (data != null) {
                List<String> pathResult = Matisse.obtainPathResult(data);
                if (pathResult != null && !pathResult.isEmpty()) {
                    selectedImagePath = pathResult.get(0);
                    selectedImagePathView.setText(selectedImagePath);
                }
            }
        }
    }

    /**
     * 获取自定义上传地址
     */
    private String getCustomUploadUrl() {
        return uploadUrlEditText.getText().toString().trim();
    }

    /**
     * 不压缩上传
     */
    private void upload() {
        if (selectedImagePath == null) {
            Toast.makeText(this, "先选择一张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        onUploadStart();
        String customUploadUrl = getCustomUploadUrl();
        if (TextUtils.isEmpty(customUploadUrl)) {
            LMImageUploader.upload(Collections.singletonList(selectedImagePath),
                    UploadType.COMMUNITY,
                    uploadListener);
        } else {
            LMImageUploader.upload(customUploadUrl,
                    Collections.singletonList(selectedImagePath),
                    UploadType.COMMUNITY,
                    uploadListener);
        }
    }

    /**
     * 压缩后上传
     */
    private void compressAndUpload() {
        if (selectedImagePath == null) {
            Toast.makeText(this, "先选择一张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        onUploadStart();
        String customUploadUrl = getCustomUploadUrl();
        if (TextUtils.isEmpty(customUploadUrl)) {
            LMImageUploader.compressAndUpload(this,
                    Collections.singletonList(selectedImagePath),
                    UploadType.COMMUNITY,
                    uploadListener);
        } else {
            LMImageUploader.compressAndUpload(customUploadUrl, this,
                    Collections.singletonList(selectedImagePath),
                    UploadType.COMMUNITY,
                    uploadListener);
        }
    }

    /**
     * RxJava 不压缩上传
     */
    private void uploadObservable() {
        if (selectedImagePath == null) {
            Toast.makeText(this, "先选择一张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        onUploadStart();
        String customUploadUrl = getCustomUploadUrl();
        Observable<String> observable;
        if (TextUtils.isEmpty(customUploadUrl)) {
            observable = LMImageUploader.uploadObservable(Collections.singletonList(selectedImagePath),
                    UploadType.COMMUNITY);
        } else {
            observable = LMImageUploader.uploadObservable(customUploadUrl,
                    Collections.singletonList(selectedImagePath),
                    UploadType.COMMUNITY);
        }
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(uploadObserver);
    }

    /**
     * RxJava 压缩后上传
     */
    private void compressAndUploadObservable() {
        if (selectedImagePath == null) {
            Toast.makeText(this, "先选择一张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        onUploadStart();
        String customUploadUrl = getCustomUploadUrl();
        Observable<String> observable;
        if (TextUtils.isEmpty(customUploadUrl)) {
            observable = LMImageUploader.compressAndUploadObservable(this,
                    Collections.singletonList(selectedImagePath),
                    UploadType.COMMUNITY);
        } else {
            observable = LMImageUploader.compressAndUploadObservable(customUploadUrl,
                    this,
                    Collections.singletonList(selectedImagePath),
                    UploadType.COMMUNITY);
        }
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(uploadObserver);
    }

    private void onUploadStart() {
        progressBar.setVisibility(View.VISIBLE);
        uploadResultView.setVisibility(View.GONE);
    }

    private void onUploadComplete() {
        progressBar.setVisibility(View.GONE);
        uploadResultView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
    }
}
