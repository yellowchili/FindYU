package com.example.navermapex_2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraSearchActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private ImageView result_img;

    private final int REQUEST_IMAGE_CAPTURE = 672;
    private final int REQUEST_IMAGE_GALLERY = 1;

    private Uri photoUri;
    private String imageFilePath;
    private Bitmap result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //액션바 제거
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_search);

        result_img = findViewById(R.id.camera_result_img);

        askPermisson(); // 카메라, 갤러리 접근 권한 요청

        bottomNavigationView = findViewById(R.id.cameraNavi);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_bar_camera:
                        openCamera();
                        break;

                    case R.id.action_bar_gallery:
                        openGallery();
                        break;
                }

                return true;
            }
        });

        result_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "TEST", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openCamera() {
        if (TedPermission.isDenied(getApplicationContext(), Manifest.permission.CAMERA)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_r), Toast.LENGTH_SHORT).show();

            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {

            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        imageFilePath = image.getAbsolutePath();

        return image;
    }

    private void openGallery() {
        if (TedPermission.isDenied(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_r), Toast.LENGTH_SHORT).show();

            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) { // 카메라 접근 요청
                Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
                ExifInterface exif = null;

                try {
                    exif = new ExifInterface(imageFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int exifOrientation;
                int exifDegree;

                if (exif != null) {
                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    exifDegree = exifOrientationToDegreess(exifOrientation);
                } else {
                    exifDegree = 0;
                }

                result = rotate(bitmap, exifDegree);

                result_img.setImageBitmap(result);
            } else if (requestCode == REQUEST_IMAGE_GALLERY) { // 갤러리 접근 요청
                photoUri = data.getData();

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                result = bitmap;

                result_img.setImageBitmap(bitmap);

            }
        }
    }

    // 화면 돌려서 촬영 시 rotate 시켜주기
    private int exifOrientationToDegreess(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } else {
            return 0;
        }
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        return Bitmap.createBitmap(bitmap,
                0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true);
    }


    ///////////////////////////////////////////////////////////////////////////////
    // 카메라, 갤러리 접근 권한 요청
    private void askPermisson() {
        TedPermission.with(getApplicationContext())
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .setDeniedMessage(getResources().getString(R.string.permission_d))
                .check();
    }

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() { // 승인
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) { // 거절
        }
    };
}