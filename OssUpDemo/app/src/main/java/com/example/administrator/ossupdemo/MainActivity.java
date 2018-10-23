package com.example.administrator.ossupdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static Uri tempUri;
    private Uri uri;
    private File picture;
    private OssService ossService;
    private String ossPath = "";
    @BindView(R.id.update)
    CircleImageView update;
    @SuppressLint("HandlerLeak")
    private Handler upPicHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 110:
                    //上传成功
                    Toast.makeText(MainActivity.this, "图片上传成功", Toast.LENGTH_SHORT).show();
                    break;
                case 119:
                    //上传失败
                    Toast.makeText(MainActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ossService = Config.initOSS(MainActivity.this,Config.endpoint, Config.bucket);
        requestAllPower();
    }

    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.INTERNET,Manifest.permission.CAMERA}, 1);
            }
        }
    }
    @OnClick({R.id.update})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.update:
                showPop();
                break;
        }
    }
    private void showPop() {
        final Dialog bottomDialog = new Dialog(this, R.style.BottomDialog);
        View view = View.inflate(this, R.layout.photo_item, null);
        TextView pz = view.findViewById(R.id.pz);
        TextView zp = view.findViewById(R.id.zp);
        TextView qx = view.findViewById(R.id.qx);
        qx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
            }
        });
        pz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takephotos(); // 拍照
                bottomDialog.dismiss();
            }
        });
        zp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choicephotos();// 相册()
                bottomDialog.dismiss();
            }
        });
        bottomDialog.setContentView(view);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.setCancelable(true);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        view.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }
    public void takephotos() {
        File file = new File(getExternalCacheDir(), "img.jpg");
        tempUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), "image.jpg"));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(this, "com.example.administrator.ossupdemo.fileprovider", file));
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        }
        startActivityForResult(intent, 1);
    }

    /**
     * 选择图片
     */
    public void choicephotos() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 0);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回码是可以用的
            switch (requestCode) {
                case 1:
                    if (Build.VERSION.SDK_INT > 23) {
                        picture = new File(getExternalCacheDir() + "/img.jpg");
                        uri = FileProvider.getUriForFile(this, "com.example.administrator.ossupdemo.fileprovider", picture);
                        //裁剪照片
                        startPhotoZoom(uri);
                    } else {
                        startPhotoZoom(tempUri);// 开始对图片进行裁剪处理
                    }
                    break;
                case 0:
                    startPhotoZoom(data.getData()); // 开始对图片进行裁剪处理
                    break;
                case 2:
                    if (data != null) {
                        setImageToView(data); // 让刚才选择裁剪得到的图片显示在界面上
                    }
                    break;
            }
        }
    }
    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    protected void startPhotoZoom(Uri uri) {
        if (uri == null) {
            Log.i("tag", "The uri is not exist.");
        }
        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 160);
        intent.putExtra("outputY", 160);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param
     * @param
     */
    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            update.setImageBitmap(photo);
            saveAndUp(photo);
        }
    }

    //保存并上传图片
    private void saveAndUp(Bitmap photo) {
        String imagePath = null;
        imagePath = Utils.savePhoto(photo, Environment
                .getExternalStorageDirectory().getAbsolutePath(), String
                .valueOf(System.currentTimeMillis()));
        if (imagePath != null) {
            //上传OSS路径
            String objectName = "user/avatar/"+ System.currentTimeMillis() + "avatar.jpg";
            ossPath = "https://rjwpublic.oss-cn-qingdao.aliyuncs.com/" + objectName;
            ossService.asyncPutImage(objectName, imagePath, upPicHandler);
        }
    }
}
