package il.ac.tau.adviplab.androidopencvlab;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //Intent tags
    private static final int SELECT_PICTURE = 1;

    //menu members
    private SubMenu mResolutionMenu;
    private SubMenu mCameraMenu;

    //flags
    private Boolean mSettingsMenuAvailable = false;

    private MenuItem mShowHistMenuItem;

    private MenuItem mShowCumuHistMenuItem;

    private MenuItem mShowEqualizedHistMenuItem;

    private MenuItem mShowMatchedHistMenuItem;

    // menu IDs
    private static final int RESOLUTION_GROUP_ID     = 1;
    private static final int CAMERA_GROUP_ID         = 2;
    private static final int DEFAULT_GROUP_ID        = 3;
    private static final int COLOR_GROUP_ID          = 4;
    private static final int HISTOGRAM_GROUP_ID      = 5;

    private MyJavaCameraView mOpenCvCameraView;
    private final CameraListener mCameraListener = new CameraListener();

    private final String[] mCameraNames = {"Rear", "Front"};
    private final int[] mCameraIDarray = {CameraBridgeViewBase.CAMERA_ID_BACK,
            CameraBridgeViewBase.CAMERA_ID_FRONT};

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = findViewById(R.id.Java_Camera_View);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        mOpenCvCameraView.setCameraPermissionGranted();
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(mCameraListener);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            Log.i(TAG,"onClick event");
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateandTime = sdf.format(new Date());
            String fileName = Environment.getExternalStorageDirectory().getPath() +
                    "/sample_picture_" + currentDateandTime + ".jpg";
            mOpenCvCameraView.takePicture(fileName);
            addImageToGallery(fileName, MainActivity.this);
            Toast.makeText(MainActivity.this, fileName + " saved",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        Log.i(TAG,"OpenCVLoader success");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "Options menu created");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);

        menu.add(DEFAULT_GROUP_ID, CameraListener.VIEW_MODE_DEFAULT, Menu.NONE, "Default");

        Menu settingsMenu = menu.addSubMenu("Settings");
        mResolutionMenu= settingsMenu.addSubMenu("Resolution");
        mCameraMenu = settingsMenu.addSubMenu("Camera");

        Menu colorMenu = menu.addSubMenu("Color");
        colorMenu.add(COLOR_GROUP_ID, CameraListener.VIEW_MODE_RGBA, Menu.NONE, "RGBA");
        colorMenu.add(COLOR_GROUP_ID, CameraListener.VIEW_MODE_GRAYSCALE, Menu.NONE, "Grayscale");

        Menu histogramMenu = menu.addSubMenu("Histogram");
        // Creates toggle button to show and hide histogram
        histogramMenu.add(HISTOGRAM_GROUP_ID,
                CameraListener.VIEW_MODE_SHOW_HIST, Menu.NONE, "Show histogram")
                .setCheckable(true)
                .setChecked(mCameraListener.isShowHistogram());
        histogramMenu.add(HISTOGRAM_GROUP_ID,
                CameraListener.VIEW_MODE_SHOW_CUMUHIST, Menu.NONE, "Show cumulative histogram")
                .setCheckable(true)
                .setChecked(mCameraListener.isShowCumulativeHistogram());
        histogramMenu.add(HISTOGRAM_GROUP_ID,
                CameraListener.VIEW_MODE_HIST_EQUALIZE, Menu.NONE, "Equalize")
                .setCheckable(true)
                .setChecked(mCameraListener.isShowEqualizedHistogram());
        histogramMenu.add(HISTOGRAM_GROUP_ID,
                CameraListener.VIEW_MODE_HIST_MATCH, Menu.NONE, "Match")
                .setCheckable(true)
                .setChecked(mCameraListener.isShowMatchedHistogram());

        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Store the menu items for histogram and cumulative histogram
        mShowHistMenuItem = menu.findItem(CameraListener.VIEW_MODE_SHOW_HIST);
        mShowCumuHistMenuItem = menu.findItem(CameraListener.VIEW_MODE_SHOW_CUMUHIST);
        mShowEqualizedHistMenuItem = menu.findItem(CameraListener.VIEW_MODE_HIST_EQUALIZE);
        mShowEqualizedHistMenuItem = menu.findItem(CameraListener.VIEW_MODE_HIST_MATCH);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (mOpenCvCameraView.isCameraOpen() && !mSettingsMenuAvailable) {
            setResolutionMenu(mResolutionMenu);
            setCameraMenu(mCameraMenu);
            mSettingsMenuAvailable = true;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        int groupId = item.getGroupId();
        int id = item.getItemId();

        switch (groupId) {
            case DEFAULT_GROUP_ID:
                mCameraListener.setViewMode(id);
                return true;

            case COLOR_GROUP_ID:
                mCameraListener.setColorMode(id);
                return true;

            case RESOLUTION_GROUP_ID:
                Camera.Size res = mOpenCvCameraView.getResolutionList().get(id);
                mOpenCvCameraView.setResolution(res);
                res = mOpenCvCameraView.getResolution();
                Toast.makeText(this, res.width + "x" + res.height, Toast.LENGTH_SHORT).show();
                return true;

            case CAMERA_GROUP_ID:
                mOpenCvCameraView.changeCameraIndex(mCameraIDarray[id]);
                String caption = mCameraNames[id] + " camera";
                Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
                setResolutionMenu(mResolutionMenu);
                return true;

            case HISTOGRAM_GROUP_ID:
                switch (id) {
                    case CameraListener.VIEW_MODE_SHOW_HIST:
                        //Toggle button to show/hide histogram
                        mShowCumuHistMenuItem.setChecked(false);
                        item.setChecked(!item.isChecked());
                        mCameraListener.setShowHistogram(item.isChecked());
                        mCameraListener.setShowCumulativeHistogram(false);
                        break;

                    case CameraListener.VIEW_MODE_SHOW_CUMUHIST:
                        //Toggle button to show/hide histogram
                        mShowHistMenuItem.setChecked(false);
                        item.setChecked(!item.isChecked());
                        mCameraListener.setShowCumulativeHistogram(item.isChecked());
                        mCameraListener.setShowHistogram(false);
                        break;

                    case CameraListener.VIEW_MODE_HIST_EQUALIZE:
                        mCameraListener.setViewMode(id);
                        break;
                    case CameraListener.VIEW_MODE_HIST_MATCH:
                        //Open gallery to select image for matching
                        if (mCameraListener.getColorMode() != CameraListener.VIEW_MODE_GRAYSCALE) {
                            Toast.makeText(MainActivity.this, "This feature currently works only in grayscale mode",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,
                                "Select image for histogram matching"),
                                SELECT_PICTURE);
                        mCameraListener.setViewMode(id);
                        break;


                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    {
                        try {
                            Bitmap imageToMatch =
                                    MediaStore.Images.Media.getBitmap
                                            (getContentResolver(), imageUri);
                            mCameraListener.computeHistOfImageToMatch(imageToMatch);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    public void setResolutionMenu(SubMenu resMenu) {
        resMenu.clear();
        int i=0;
        for (Camera.Size res : mOpenCvCameraView.getResolutionList()) {
            resMenu.add(RESOLUTION_GROUP_ID, i++, Menu.NONE, res.width + "x" + res.height);
        }
    }

    private void setCameraMenu(SubMenu camMenu) {
        for (int i = 0; i < Math.min(mOpenCvCameraView.getNumberOfCameras(), 2); i++) {
            camMenu.add(CAMERA_GROUP_ID, i, Menu.NONE, mCameraNames[i]);
        }
    }

    @SuppressLint("InlinedApi")
    private static void addImageToGallery(final String filePath, final
    Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATE_TAKEN,
                System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        context.getContentResolver().
                insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}

