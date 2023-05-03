package il.ac.tau.adviplab.androidopencvlab;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
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

    //menu members
    private SubMenu mResolutionMenu;
    private SubMenu mCameraMenu;

    //flags
    private Boolean mSettingsMenuAvailable = false;

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
                .setChecked(mCameraListener.isShowCumuHistogram());

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
                        item.setChecked(!item.isChecked());
                        mCameraListener.setShowCumuHistogram(false);
                        mCameraListener.setShowHistogram(item.isChecked());
                        break;
                    case CameraListener.VIEW_MODE_SHOW_CUMUHIST:
                        //Toggle button to show/hide histogram
                        item.setChecked(!item.isChecked());
                        mCameraListener.setShowHistogram(false);
                        mCameraListener.setShowCumuHistogram(item.isChecked());
                        break;
                }

            default:
                return super.onOptionsItemSelected(item);
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
}

