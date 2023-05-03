package il.ac.tau.adviplab.androidopencvlab;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

class CameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {
    // Constants:
    static final int VIEW_MODE_DEFAULT              = 0;
    static final int VIEW_MODE_RGBA                 = 1;
    static final int VIEW_MODE_GRAYSCALE            = 2;
    static final int VIEW_MODE_SHOW_HIST            = 3;
    static final int VIEW_MODE_SHOW_CUMUHIST        = 4;



    //Mode selectors:
    private int mViewMode  = VIEW_MODE_DEFAULT;
    private int mColorMode = VIEW_MODE_RGBA   ;
    private boolean mShowHistogram = false;
    private boolean mShowCumulativeHistogram = false;

    //Members
    private Mat mImToProcess;
    private Mat[] mHistArray;
    private Mat[] mCumuHistArray;

    //Getters and setters

    int getColorMode() {
        return mColorMode;
    }

    void setColorMode(int colorMode) {
        mColorMode = colorMode;
    }

    // not used
    /*
    int getViewMode() {
        return mViewMode;
    }
    */

    void setViewMode(int viewMode) {
        mViewMode = viewMode;
    }

    boolean isShowHistogram() {
        return mShowHistogram;
    }
    public void setShowHistogram(boolean showHistogram) {
        mShowHistogram = showHistogram;
    }

    boolean isShowCumulativeHistogram() {
        return mShowCumulativeHistogram;
    }
    public void setShowCumulativeHistogram(boolean showCumulativeHistogram) {
        mShowCumulativeHistogram = showCumulativeHistogram;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mImToProcess = new Mat();
        mHistArray = new Mat[] {new Mat(), new Mat(), new Mat()};
        mCumuHistArray = new Mat[] {new Mat(), new Mat(), new Mat()};
    }

    @Override
    public void onCameraViewStopped() {
        mImToProcess.release();
        for (Mat histMat : mHistArray) {
            histMat.release();
        }
        for (Mat histMat : mCumuHistArray) {
            histMat.release();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        switch (mColorMode) {
            case VIEW_MODE_RGBA:
                mImToProcess = inputFrame.rgba();
                break;
            case VIEW_MODE_GRAYSCALE:
                mImToProcess = inputFrame.gray();
                break;
        }

        switch (mViewMode) {
            case VIEW_MODE_DEFAULT:
                break;
        }
        if (mShowHistogram) {
            int histSizeNum = 100;
            MyImageProc.calcHist(mImToProcess, mHistArray, histSizeNum);
            MyImageProc.showHist(mImToProcess, mHistArray, histSizeNum);
        }
        if (mShowCumulativeHistogram) {
            int histSizeNum = 100;
            int numberOfChannels = Math.min(mImToProcess.channels(),3);
            MyImageProc.calcHist(mImToProcess, mHistArray, histSizeNum);
            MyImageProc.calcCumulativeHist(mHistArray, mCumuHistArray, numberOfChannels);
            MyImageProc.showHist(mImToProcess, mCumuHistArray, histSizeNum);
        }
        return mImToProcess;
    }
}
