package il.ac.tau.adviplab.androidopencvlab;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;

@SuppressWarnings("SameParameterValue")
class MyImageProc extends CameraListener {
    static void calcHist(Mat image, Mat[] histList, int histSizeNum, int
            normalizationConst, int normalizationNorm) {
        int numberOfChannels = Math.min(image.channels(), 3);
// if the image is RGBA,
// ignore the last channel (Alpha channel)
        MatOfInt[] channels = new MatOfInt[numberOfChannels];
        for (int i = 0; i < numberOfChannels; i++) {
            channels[i] = new MatOfInt(i);
        }
        Mat mat0 = new Mat();
        MatOfInt histSize = new MatOfInt(histSizeNum);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        int chIdx = 0;
        for (MatOfInt channel : channels) {
            Imgproc.calcHist(Collections.singletonList(image), channel,
                    mat0, histList[chIdx], histSize, ranges);
            Core.normalize(histList[chIdx], histList[chIdx],
                    normalizationConst, 0, normalizationNorm);
            chIdx++;
        }

        for (MatOfInt channel : channels) {
            channel.release();
        }
        mat0.release();
        histSize.release();
        ranges.release();
    }

    static void calcHist(Mat image, Mat[] histList, int histSizeNum) {
        calcHist( image, histList, histSizeNum, 1, Core.NORM_L1);
    }
    private static void showHist(Mat image, Mat[] histList, int
            histSizeNum, int offset, int thickness) {
        int numberOfChannels = Math.min(image.channels(), 3);
        // if the image is RGBA,
        // ignore the last channel (Alpha channel)
        float[] buff = new float[histSizeNum];
        Point mP1 = new Point();
        Point mP2 = new Point();
        Scalar[] mColorsRGB;
        mColorsRGB = new Scalar[]{new Scalar(255, 0, 0), new Scalar(0,
                255, 0), new Scalar(0, 0, 255)};
        for (int chIdx = 0; chIdx < numberOfChannels; chIdx++) {
            Core.normalize(histList[chIdx], histList[chIdx],
                    image.height() / 2.0, 0, Core.NORM_INF);
            histList[chIdx].get(0, 0, buff);
            for (int h = 0; h < histSizeNum; h++) {
                mP1.x = mP2.x = offset + (chIdx * (histSizeNum + 10) + h) *
                        thickness;
                mP1.y = image.height() - 50;
                mP2.y = mP1.y - 2 - (int) buff[h];
                Imgproc.line(image, mP1, mP2, mColorsRGB[chIdx], thickness);
            }
        }
    }
    static void showHist(Mat image, Mat[] histList, int histSizeNum) {
        int thickness = Math.min(image.width() / (histSizeNum + 10) / 5, 5);
        int offset =  (image.width() - (5 * histSizeNum + 4 * 10) * thickness) / 2;
        showHist(image, histList, histSizeNum, offset, thickness);
    }
    private static void calcCumulativeHist(Mat hist, Mat cumuHist) {
    // Mat hist - histogram
    // Mat cumuHist - cumulative histogram
        int hist_size = (int) hist.total();
        float[] buffer = new float [hist_size];
        hist.get(0,0, buffer);
        for (int i=1; i<buffer.length; i++ ){
            buffer[i] = buffer[i]+buffer[i-1];
        }
        cumuHist.put(0,0, buffer);
    }
    static void calcCumulativeHist(Mat[] hist, Mat[] cumuHist, int
            numberOfChannels) {
        for (int chIdx = 0; chIdx < numberOfChannels; chIdx++) {
            cumuHist[chIdx].create(hist[chIdx].size(), hist[chIdx].type());
            calcCumulativeHist(hist[chIdx], cumuHist[chIdx]);
        }
    }

}

