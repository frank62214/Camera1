package com.example.camera1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    private Button takePhotoBn;
    private ImageView showImage;
    private Uri imageUri;
    //private Uri imageUri_1;
    private Uri contentUri;
    private String filename;
    private Environment Enviroment;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boolean cameraHasGone = checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean externalHasGone = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions;
            if (!cameraHasGone && !externalHasGone) {//如果兩個權限都未取得
                permissions = new String[2];
                permissions[0] = Manifest.permission.CAMERA;
                permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            } else if (!cameraHasGone) {//如果只有相機權限未取得
                permissions = new String[1];
                permissions[0] = Manifest.permission.CAMERA;
            } else if (!externalHasGone) {//如果只有存取權限未取得
                permissions = new String[1];
                permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            } else {
                //tvRes.setText("相機權限已取得\n儲存權限已取得");
                Toast.makeText(MainActivity.this, "相機權限已取得\\n儲存權限已取得\"", Toast.LENGTH_SHORT).show();
                return;
            }
            requestPermissions(permissions, 100);
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

        StrictMode.setVmPolicy(builder.build());

        builder.detectFileUriExposure();


        takePhotoBn = (Button) findViewById(R.id.button1);
        showImage = (ImageView) findViewById(R.id.imageView1);

        takePhotoBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check_Permission();

                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = new Date(System.currentTimeMillis());
                filename = format.format(date);

                File path = Enviroment.getExternalStoragePublicDirectory(Enviroment.DIRECTORY_DCIM);
                File outputImage   = new File(path, filename + ".jpg");
                //File outputImage_1 = new File(path, filename + "-1.jpg");


                try{
                    if(outputImage.exists()){    outputImage.delete(); }
                    //if(outputImage_1.exists()){  outputImage_1.delete(); }


                    outputImage.createNewFile();
                    //outputImage_1.createNewFile();


                    //System.out.println("FYBR");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageUri   = Uri.fromFile(outputImage);
                //imageUri_1 = Uri.fromFile(outputImage_1);
                //String test = outputImage.getPath();
                contentUri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getPackageName() + ".provider", outputImage);
                System.out.println(contentUri);
                //imageUri = Uri.fromFile(new File(filename));
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                //Intent chooser = Intent.createChooser(intentShareFile, "Share File");
                //URI Permission
                List<ResolveInfo> resInfoList = MainActivity.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    MainActivity.this.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    System.out.println("FYBR");
                }
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                   ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    //Check_Permission();
                }else {

                    startActivityForResult(intent, TAKE_PHOTO);
                    //startActivityForResult(intent, CROP_PHOTO);

                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(MainActivity.this, "ActivityResult resultCode:" + resultCode, Toast.LENGTH_SHORT).show();
        System.out.println("ActivityResult resultCode:" + resultCode);
        if(resultCode != RESULT_OK){
            Toast.makeText(MainActivity.this, "ActivityResult resultCode error", Toast.LENGTH_SHORT).show();
            return;
        }
        switch(requestCode){
            case TAKE_PHOTO:
                System.out.println("TAKE_PHOTO");
                Intent intent = new Intent("com.android.camera.action.CROP");
                //intent.setDataAndType(imageUri, "image/*");
                intent.setDataAndType(contentUri, "image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra("outputX", 1000);
                intent.putExtra("outputY", 1000);
                intent.putExtra("return-data", true);
                //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri_1);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                }
                intentBc.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intentBc.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                List<ResolveInfo> resInfoList = MainActivity.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    MainActivity.this.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }


                //intentBc.setData(imageUri);
                intentBc.setData(contentUri);
                this.sendBroadcast(intentBc);
                startActivityForResult(intent, CROP_PHOTO);
                break;
            case CROP_PHOTO:
                System.out.println("CROP_PHOTO");
                try{
                    //Bitmap bitmap = BitmapFactory.decodeStream(
                    //        getContentResolver().openInputStream(imageUri));
                    Bitmap bitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(contentUri));
                    //Toast.makeText(MainActivity.this, imageUri.toString(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, contentUri.toString(), Toast.LENGTH_SHORT).show();



                    //刷新DCIM文件夾，儲存檔案至DCIM文件夾。
                    Intent intentBcc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

                    List<ResolveInfo> resInfoList1 = MainActivity.this.getPackageManager().queryIntentActivities(intentBcc, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList1) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        MainActivity.this.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    //intentBcc.setData(imageUri_1);
                    intentBcc.setData(contentUri);
                    this.sendBroadcast(intentBcc);
                    //FileOutputStream fOut =  new FileOutputStream(String.valueOf(imageUri));
                    //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

                    //lbl_imgpath.setText(imageUri.toString());

                    showImage.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        StringBuffer word = new StringBuffer();
        switch (permissions.length) {
            case 1:
                if (permissions[0].equals(Manifest.permission.CAMERA)) word.append("相機權限");
                else word.append("儲存權限");
                if (grantResults[0] == 0) word.append("已取得");
                else word.append("未取得");
                word.append("\n");
                if (permissions[0].equals(Manifest.permission.CAMERA)) word.append("儲存權限");
                else word.append("相機權限");
                word.append("已取得");

                break;
            case 2:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.CAMERA)) word.append("相機權限");
                    else word.append("儲存權限");
                    if (grantResults[i] == 0) word.append("已取得");
                    else word.append("未取得");
                    if (i < permissions.length - 1) word.append("\n");
                }
                break;
        }
        //tvRes.setText(word.toString());
    }
    private void Check_Permission(){
        if(ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(MainActivity.this, "儲存權限未開啟", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "儲存權限開啟", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
        if(ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                Toast.makeText(MainActivity.this, "相機權限未開啟", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "相機權限開啟", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
            }
        }
    }
}