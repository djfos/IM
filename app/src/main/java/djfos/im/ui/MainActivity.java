package djfos.im.ui;

import androidx.databinding.ViewDataBinding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

import androidx.appcompat.app.AppCompatActivity;
import djfos.im.R;


public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
//    static final Uri mLocationForPhotos;

//    public void capturePhoto(String targetFilename) {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                Uri.withAppendedPath(mLocationForPhotos, targetFilename));
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bitmap thumbnail = data.getp("data");
            // Do other work with full size photo saved in mLocationForPhotos

        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
        }
    }
}
