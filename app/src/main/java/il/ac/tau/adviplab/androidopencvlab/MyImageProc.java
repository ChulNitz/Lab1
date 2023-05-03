package il.ac.tau.adviplab.androidopencvlab;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("SameParameterValue")
class MyImageProc extends CameraListener {
    static final int HIST_NORMALIZATION_CONST = 1;
    private static final int COMP_MATCH_DISTANCE = 99;
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

    private static void matchHistogram(Mat histSrc, Mat histDst, Mat
            lookUpTable) {
    //  In MyImageProc.java create a function matchHistogram that accepts a source histogram and a destination histogram
    //  and returns a lookup table: for each intensity level [0,..,255]
    //  the look-up table should hold the new value (i.e., lookupTable[i] = j means that the intensity level i at the source image
    //  should be replaced by the intensity level j at the destination image)

        Mat cdfSrc = new Mat(histSrc.size(), histSrc.type());
        Mat cdfDst = new Mat(histDst.size(), histDst.type());

        calcCumulativeHist(histSrc, cdfSrc);
        calcCumulativeHist(histDst, cdfDst);

        // Normalize CDFs
        Core.normalize(cdfSrc, cdfSrc, 1, 0, Core.NORM_MINMAX);
        Core.normalize(cdfDst, cdfDst, 1, 0, Core.NORM_MINMAX);

        // Compute the look-up table
        for (int i = 0; i < 256; i++) {
            double cdfSrcValue = cdfSrc.get(i, 0)[0];
            int j;
            for (j = 0; j < 256; j++) {
                if (cdfSrcValue <= cdfDst.get(j, 0)[0]) {
                    break;
                }
            }
            lookUpTable.put(i, 0, j);
        }
    }

    private static void applyIntensityMapping(Mat srcImage, Mat
            lookUpTable) {
        Mat tempMat = new Mat();
        Core.LUT(srcImage, lookUpTable, tempMat);
        tempMat.convertTo(srcImage, CvType.CV_8UC1);
        tempMat.release();
    }

    static void matchHist(Mat srcImage, Mat dstImage, Mat[] srcHistArray, Mat[]
            dstHistArray, boolean histShow) {
        Mat lookupTable = new Mat(256, 1, CvType.CV_32SC1);

//        compute histogram of current input image with 256 bins
        calcHist(srcImage, srcHistArray, 256);
        Point point_1 = new Point(2,10);
        Point point_2 = new Point(2,15);
        compareHistograms(srcImage, srcHistArray[0], dstHistArray[0], point_1, COMP_MATCH_DISTANCE, "Distance Before Matching");
//        find mapping from input image to destination image
        matchHistogram(srcHistArray[0], dstHistArray[0], lookupTable);
//        apply mapping to get new image with histogram similar to destination image
        applyIntensityMapping(srcImage, lookupTable);
        lookupTable.release();

        // calculate the new histogram of the image and save in a new variable
//        Mat[] newHistArray = new Mat[1];
        calcHist(srcImage, srcHistArray, 256);
        compareHistograms(srcImage, srcHistArray[0], dstHistArray[0], point_2, COMP_MATCH_DISTANCE, "Match Distance after matching");
        //Here add the part that displays the histogram if histShow == true
        //If (histShow == true), then display a 100-bin histogram of the destination image at the right side of the screen.
        if (histShow) {
            int thickness = Math.min(srcImage.width() / (100 + 10) / 5, 5);
            int offset =  (srcImage.width() - (100 + 4 * 10) * thickness);
            calcHist(dstImage, srcHistArray, 100);
            showHist(srcImage, srcHistArray, 100, offset, thickness);

        }
        // release newHistArray
//        newHistArray[0].release();

    }

    private static void compareHistograms(Mat image, Mat h1, Mat h2, Point
            point, int compType, String string) {
        double dist;
        if (compType == COMP_MATCH_DISTANCE) {
            dist = matchDistance(h1, h2); //Computes the match distance
        } else {
            dist = Imgproc.compareHist(h1, h2, compType);
        }
        Imgproc.putText(image,
                string + String.format(Locale.ENGLISH, "%.2f", dist),
                point, Imgproc.FONT_HERSHEY_COMPLEX_SMALL, 0.8,
                new Scalar(255, 255, 255), 1);
    }

    private static double matchDistance(Mat h1, Mat h2) {
        // l1 distance
        double dist;
        // Add your implementation here
        dist = 0;
        for (int i = 0; i < h1.rows(); i++) {
            dist += Math.abs(h1.get(i, 0)[0] - h2.get(i, 0)[0]);
        }
        return dist;
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

    static void equalizeHist(Mat image) {
        int numberOfChannels = image.channels();
        if (numberOfChannels > 1) {
            List<Mat> RGBAChannels = new ArrayList<>(numberOfChannels);
            Core.split(image, RGBAChannels);
            // Equalize the channels R,G,B,
            // Don’t equalize the channel A
            int i = 0;
            for (Mat colorChannel : RGBAChannels) {
                if (i != 3) {
                    Imgproc.equalizeHist(colorChannel, colorChannel);
                    i++;
                } }
            Core.merge(RGBAChannels, image);
            for (Mat colorChannel : RGBAChannels) {
                colorChannel.release();
            }
        } else {
            Imgproc.equalizeHist(image, image);
        }
    }

}

