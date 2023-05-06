package il.ac.tau.adviplab.androidopencvlab;

import android.graphics.Bitmap;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

class CameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {
    // Constants:
    static final int VIEW_MODE_DEFAULT              = 0;
    static final int VIEW_MODE_RGBA                 = 1;
    static final int VIEW_MODE_GRAYSCALE            = 2;
    static final int VIEW_MODE_SHOW_HIST            = 3;
    static final int VIEW_MODE_SHOW_CUMUHIST        = 4;

    static final int VIEW_MODE_HIST_EQUALIZE        = 5;

    static final int VIEW_MODE_HIST_MATCH           = 6;



    //Mode selectors:
    private int mViewMode  = VIEW_MODE_DEFAULT;
    private int mColorMode = VIEW_MODE_RGBA   ;
    private boolean mShowHistogram = false;
    private boolean mShowCumulativeHistogram = false;

    private boolean mShowMatchedHistogram = false;

    private boolean mShowEqualizedHistogram = false;

    //Members
    private Mat mImToProcess;
    private Mat[] mHistArray;
    private Mat[] mCumuHistArray;
    private Mat mImageToMatch;
    private Mat[] mHistDstArray;

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

    public void setShowHistogram(boolean showHistogram) {
        mShowHistogram = showHistogram;
    }

    boolean isShowHistogram() {
        return mShowHistogram;
    }
    boolean isShowCumulativeHistogram() {
        return mShowCumulativeHistogram;
    }

    boolean isShowMatchedHistogram() {
        return mShowMatchedHistogram;
    }

    boolean isShowEqualizedHistogram() {
        return mShowEqualizedHistogram;
    }
    public void setShowCumulativeHistogram(boolean showCumulativeHistogram) {
        mShowCumulativeHistogram = showCumulativeHistogram;
    }

    public void setShowEqualizedHistogram(boolean showEqualizedHistogram) {
        mShowEqualizedHistogram = showEqualizedHistogram;
    }

    public void setShowMatchedHistogram(boolean showMatchedHistogram) {
        mShowMatchedHistogram = showMatchedHistogram;
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
        if (mHistDstArray != null) {
            for (Mat mat : mHistDstArray) {
                mat.release();
            }
        }
        if (mImageToMatch != null) {
            mImageToMatch.release();
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
//  here we wish to handle the view mode
//  if histogram or cumulative histogram are selected it only controls the presentation of the histogram image
//  when we are in histogram equalization mode we wish to show the equalized image and the histogram/cumulative, if selected
//  when we are in matched mode we wish to show the match target histogram, and also diaplay histogram/cumulative if selected
        int histSizeNum = 100;
        int numberOfChannels = Math.min(mImToProcess.channels(), 3);
        switch (mViewMode) {
            case VIEW_MODE_DEFAULT:
                mShowEqualizedHistogram = false;
                mShowMatchedHistogram = false;
                mShowHistogram = false;
                mShowCumulativeHistogram = false;
                return mImToProcess;
            case VIEW_MODE_SHOW_HIST:
                if (mShowEqualizedHistogram) {
                    MyImageProc.equalizeHist(mImToProcess);
                }
                MyImageProc.calcHist(mImToProcess, mHistArray, histSizeNum);
                MyImageProc.showHist(mImToProcess, mHistArray, histSizeNum);
                // keep matched histogram if selected
                if (mShowMatchedHistogram) {
                    if (mHistDstArray == null) {
                        break;
                    }
                    if (mHistDstArray[0].total() > 0) {
                        MyImageProc.matchHist(mImToProcess, mImageToMatch,
                                mHistArray, mHistDstArray, true);
                    }
                }
                break;
            case VIEW_MODE_SHOW_CUMUHIST:
                if (mShowEqualizedHistogram) {
                    MyImageProc.equalizeHist(mImToProcess);
                }
                MyImageProc.calcHist(mImToProcess, mHistArray, histSizeNum);
                MyImageProc.calcCumulativeHist(mHistArray, mCumuHistArray, numberOfChannels);
                MyImageProc.showHist(mImToProcess, mCumuHistArray, histSizeNum);
                if (mShowMatchedHistogram) {
                    if (mHistDstArray == null) {
                        break;
                    }
                    if (mHistDstArray[0].total() > 0) {
                        MyImageProc.matchHist(mImToProcess, mImageToMatch,
                                mHistArray, mHistDstArray, true);
                    }
                }
            case VIEW_MODE_HIST_EQUALIZE:
                  // equalize the image and show the histogram/ cumulative histogram if selected
                if (mShowEqualizedHistogram) {
                    MyImageProc.equalizeHist(mImToProcess);
                }
                if (mShowHistogram) {
                    MyImageProc.calcHist(mImToProcess, mHistArray, histSizeNum);
                    MyImageProc.showHist(mImToProcess, mHistArray, histSizeNum);
                }
                if (mShowCumulativeHistogram) {
                    MyImageProc.calcHist(mImToProcess, mHistArray, histSizeNum);
                    MyImageProc.calcCumulativeHist(mHistArray, mCumuHistArray, numberOfChannels);
                    MyImageProc.showHist(mImToProcess, mCumuHistArray, histSizeNum);
                }
                break;
            case VIEW_MODE_HIST_MATCH:
                if (mHistDstArray == null) {
                    break;
                }
                if (mHistDstArray[0].total() > 0) {
                    MyImageProc.matchHist(mImToProcess, mImageToMatch,
                            mHistArray, mHistDstArray, true);
                }
                if (mShowHistogram) {
                    MyImageProc.calcHist(mImToProcess, mHistArray, histSizeNum);
                    MyImageProc.showHist(mImToProcess, mHistArray, histSizeNum);
                }
                if (mShowCumulativeHistogram) {
                    MyImageProc.calcHist(mImToProcess, mHistArray, histSizeNum);
                    MyImageProc.calcCumulativeHist(mHistArray, mCumuHistArray, numberOfChannels);
                    MyImageProc.showHist(mImToProcess, mCumuHistArray, histSizeNum);
                }

                break;
        }
        return mImToProcess;
    }

    void computeHistOfImageToMatch(Bitmap image) {
        //converts a bitmap to Mat
        mImageToMatch = new Mat();
        Utils.bitmapToMat(image, mImageToMatch);
        //convert to grayscale
        Imgproc.cvtColor(mImageToMatch, mImageToMatch,
                Imgproc.COLOR_RGBA2GRAY);
        if (mHistDstArray == null) {
            mHistDstArray = new Mat[3];
            for (int i = 0; i < mHistDstArray.length; i++) {
                mHistDstArray[i] = new Mat();
            }
        }
        MyImageProc.calcHist(mImageToMatch, mHistDstArray, 256,
                MyImageProc.HIST_NORMALIZATION_CONST, Core.NORM_L1);
    }

}
