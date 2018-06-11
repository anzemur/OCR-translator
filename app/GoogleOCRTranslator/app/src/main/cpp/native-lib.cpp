#include <jni.h>
#include <string>
#include<opencv2/core/core.hpp>
#include<opencv2/highgui/highgui.hpp>
#include<opencv2/imgproc/imgproc.hpp>
#include<opencv2/ml/ml.hpp>
#include<iostream>
#include<sstream>
#include <android\asset_manager.h>
#include <android\asset_manager_jni.h>


const int VALID_CONTOUR_AREA = 100;
const int IMAGE_WIDTH = 25;
const int IMAGE_HEIGHT = 30;


using namespace cv;
using namespace std;

/**
 * Function that sorts contours based on their y position.
 * @param contour1
 * @param contour2
 * @return true if contour1 is positioned below contour2
 */
static bool sortLineContours(vector<Point> contour1, vector<Point> contour2) {
    return(boundingRect(contour1).y  < boundingRect(contour2).y);
}


class DataContour {
public:

    vector<Point> ptContour;
    Rect boundingRect;
    float floatArea;


    /**
     * Check if contour is valid-> has enough of data.
     * @return true if it is
     */
    bool isValid() {
        return floatArea >= VALID_CONTOUR_AREA;
    }


    /**
     * Sorts contours with data based on their x position.
     * @param left
     * @param right
     * @return true if left contour is positioned left of right contour
     */
    static bool sortRectX(const DataContour &left, const DataContour &right) {

        return(left.boundingRect.x  < right.boundingRect.x );
    }

    /**
     * Sorts contours with data based on their y position.
     * @param left
     * @param right
     * @return true if left contour is positioned below right contour
     */
    static bool sortRectY(const DataContour &left, const DataContour &right) {

        return(left.boundingRect.y  < right.boundingRect.y);
    }


};


/**
 * Gets the mean of the width from all contours in line
 * @param validContoursWithData
 * @return the mean of the width
 */
double getMeanWidth(vector<DataContour> validContoursWithData)
{
    double mean = 0;

    for (int i = 0; i < validContoursWithData.size(); i++) {
        mean += validContoursWithData[i].boundingRect.width;
    }

    return mean / validContoursWithData.size();
}


/**
 * Function that finds the mass center of the imput image
 * @param src -> imput image
 * @return mass center position
 */
Point findMassCenter(Mat src)
{
    int totalX = 0, totalY = 0;
    int count = 0;
    for (int x = 0; x < src.cols; x++)
    {
        for (int y = 0; y < src.rows; y++)
        {
            totalX += x;
            totalY += y;
            count++;

        }
    }
    return Point(totalX / count, totalY / count);
}


extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_murg_googleocrtranslator_MainActivity_stringFromJNI( JNIEnv *env, jobject /* this */) {
    std::string hello = "Hello from C++";
    Mat egg;

    return env->NewStringUTF(hello.c_str());
}




extern "C"
JNIEXPORT jstring JNICALL
/**
 * Function that detects and extract text from the image
 * @param env
 * @param instance
 * @param srcImgAddr address of the image with desired text
 * @param classAddr address of the classification matrix
 * @param imagesAddr address of the images matrix
 * @return
 */
Java_com_example_murg_googleocrtranslator_MainActivity_trainAndDetect(JNIEnv *env, jobject instance, jlong srcImgAddr, jlong classAddr, jlong imagesAddr) {


    std::string hello = "maybe dela";

    Mat& inputImage = *(Mat*)srcImgAddr;
    Mat& classificationInts = *(Mat*)classAddr;
    Mat& imagesFloats = *(Mat*)imagesAddr;

    if( inputImage.empty() || classificationInts.empty() || imagesFloats.empty() ) {
        string error = "error: one of matrices not defined!";
        return env->NewStringUTF(error.c_str());;
    }

    if( !inputImage.isContinuous() || !classificationInts.isContinuous() || !imagesFloats.isContinuous() ) {
        string error = "error: one of matrices is not continuous!";
        return env->NewStringUTF(error.c_str());;
    }

    std::string outputText ="";

    Mat inputCopy = inputImage.clone();


    /* INIT SVM model */
    Ptr<ml::SVM> svm = ml::SVM::create();
    svm->setType(ml::SVM::C_SVC);
    svm->setKernel(ml::SVM::POLY);
    svm->setGamma(3);
    svm->setDegree(3);

    /* Train SVM model */
    Ptr<ml::TrainData> tData = ml::TrainData::create(imagesFloats, ml::SampleTypes::ROW_SAMPLE, classificationInts);
    svm->train(tData);


    /* Init matrices that we will need in the future */
    Mat matGrayscale;
    Mat matBlurred;
    Mat matThreshold;


    cvtColor(inputImage, matGrayscale, CV_BGR2GRAY);

    /* Aplly gaussianBlur*/
    GaussianBlur( matGrayscale,
                  matBlurred,
                  Size(5, 5),
                  0);

    /* Aplly treshold*/
    adaptiveThreshold( matBlurred,
                       matThreshold,
                       255,
                       ADAPTIVE_THRESH_GAUSSIAN_C,
                       THRESH_BINARY_INV,
                       11,
                       2);


    /*Find the lines of text*/
    Mat grayLines;
    cvtColor(inputCopy, grayLines, COLOR_BGR2GRAY);

    Mat thresh;
    threshold(grayLines, thresh, 0, 255, THRESH_OTSU | THRESH_BINARY_INV);


    //imshow("dilation", thresh1);
    //int c = waitKey(0);


    Mat kernel = getStructuringElement(MORPH_RECT, Size(15, 3));
    Mat dilation;
    dilate(thresh, dilation, kernel, Point(-1, -1) ,3);

    //imshow("dilation", dilation);
    //int d = waitKey(0);


    vector<vector<Point> > lineContours;
    vector<Vec4i> v4iHierarchyLine;

    findContours(dilation,
                 lineContours,
                 v4iHierarchyLine,
                 RETR_EXTERNAL,
                 CHAIN_APPROX_NONE);




    if(lineContours.size() > 0) {

        std::sort(lineContours.begin(), lineContours.end(), sortLineContours);

        for (int i = 0; i < lineContours.size(); i++) {
            Rect currentLine = boundingRect(lineContours[i]);

            Mat textRow;
            textRow = matThreshold(currentLine);


            Mat textRowCopy;
            textRowCopy = textRow.clone();


            vector<std::vector<Point> > potencialContour;
            vector<Vec4i> v4iHierarchy;

            /*Find the contours in a row/line of text */
            findContours( textRowCopy,
                          potencialContour,
                          v4iHierarchy,
                          RETR_EXTERNAL,
                          CHAIN_APPROX_SIMPLE);


            std::vector<DataContour> contoursWithData;
            std::vector<DataContour> validContoursWithData;


            /* Single out the contours with data */
            for (int i = 0; i < potencialContour.size(); i++) {

                DataContour contourWithData;
                contourWithData.ptContour = potencialContour[i];
                contourWithData.boundingRect = boundingRect(contourWithData.ptContour);
                contourWithData.floatArea = static_cast<float>(contourArea(contourWithData.ptContour));

                contoursWithData.push_back(contourWithData);
            }

            /* And single out the contours that are useful */
            for (int i = 0; i < contoursWithData.size(); i++) {
                if (contoursWithData[i].isValid())
                    validContoursWithData.push_back(contoursWithData[i]);

            }

            /* Sort the contours in current line from left to right */
            std::sort(validContoursWithData.begin(), validContoursWithData.end(),
                      DataContour::sortRectX);


            /* Get the char width mean for spaces computation. */
            double meanWidthChars = 0;
            if (validContoursWithData.size() != 0) {
                meanWidthChars = getMeanWidth(validContoursWithData);
                std::cout << "\n\n" << "MEAN = " << meanWidthChars << "\n\n";
            }

            for (int i = 0; i < validContoursWithData.size(); i++) {

                Mat ROI = textRow(validContoursWithData[i].boundingRect);


                Mat ROIResized;
                resize(ROI, ROIResized, Size(IMAGE_WIDTH, IMAGE_HEIGHT));

                Mat ROIFloat;
                ROIResized.convertTo(ROIFloat, CV_32FC1);

                Mat ROIFlattenedFloat = ROIFloat.reshape(1, 1);

                Mat currentChar(0, 0, CV_32F);

                /* Use SVM to find the most similar char */
                ROIFlattenedFloat.reshape(1, 1);
                svm->predict(ROIFlattenedFloat, currentChar);


                float fltCurrentChar = (float) currentChar.at<float>(0, 0);
                cout << char(int(fltCurrentChar));

                /* Add char to the output text.  */
                outputText = outputText + char(int(fltCurrentChar));

                /*Find spaces between the chars */
                if (i + 1 < validContoursWithData.size()) {
                    int currWidthX = validContoursWithData[i].boundingRect.x +
                                     validContoursWithData[i].boundingRect.width;
                    int nextWidthX = validContoursWithData[i + 1].boundingRect.x;

                    std::cout << "\n\n" << "Xcurr = " << currWidthX << "\n\n";
                    std::cout << "Xnext = " << nextWidthX << "\n\n";
                    std::cout << "NORM = " << abs(currWidthX - nextWidthX) << "\n\n";
                    std::cout << "MEAN = " << meanWidthChars * 2 << "\n\n";


                    if (abs(currWidthX - nextWidthX) >= meanWidthChars / 2) {
                        outputText = outputText + " ";
                    }


                } else if (i == validContoursWithData.size() - 1) {
                    outputText = outputText + " ";
                }
            }

        }



    /* Attempt to find the lines-> not so useful/good, but better char detection?
     *
     *
     *
     *
     *
    Mat1f horizontalProjection;
    reduce(matThreshold, horizontalProjection, 1, CV_REDUCE_AVG);

    float treshHold = 0;
    Mat1b hist = horizontalProjection <= treshHold;

    vector<int> yCoordinates;
    int y = 0;
    int count = 0;
    bool isSpace = false;

    for (int i = 0; i < matThreshold.rows; ++i) {
        if (!isSpace) {
            if (hist(i)) {
                isSpace = true;
                count = 1;
                y = i;
            }

        } else {
            if (!hist(i)) {
                isSpace = false;
                yCoordinates.push_back(y / count);
            } else {
                y += i;
                count++;
            }
        }
    }




    if(yCoordinates.size() > 0) {
        for (int i = 0; i < yCoordinates.size() - 1; ++i) {

            Point pTopLeft;
            Point pBottomRight;

            if (i == 0) {
                pTopLeft.x = 0;
                pTopLeft.y = 0;

                pBottomRight.x = matThreshold.cols;
                pBottomRight.y = yCoordinates[i];


            } else {
                pTopLeft.x = 0;
                pTopLeft.y = yCoordinates[i];

                pBottomRight.x = matThreshold.cols;
                pBottomRight.y = yCoordinates[i + 1];


            }

            Rect rowRect(pTopLeft, pBottomRight);
            Mat textRow;
            textRow = matThreshold(rowRect);


            Mat textRowCopy;
            textRowCopy = textRow.clone();

            -||-

        }


    */

    } else {
        /*If we didn' find any lines of text */

        Mat matThresholdCopy = matThreshold.clone();

        vector<std::vector<Point> > potencialContour;
        vector<Vec4i> v4iHierarchy;

        findContours(matThresholdCopy,
                     potencialContour,
                     v4iHierarchy,
                     RETR_EXTERNAL,
                     CHAIN_APPROX_SIMPLE);



        vector<DataContour> contoursWithData;
        vector<DataContour> validContoursWithData;



        for (int i = 0; i < potencialContour.size(); i++) {

            DataContour contourWithData;
            contourWithData.ptContour = potencialContour[i];
            contourWithData.boundingRect = boundingRect(contourWithData.ptContour);
            contourWithData.floatArea = static_cast<float>(cv::contourArea(contourWithData.ptContour));

            contoursWithData.push_back(contourWithData);
        }

        for (int i = 0; i < contoursWithData.size(); i++) {
            if (contoursWithData[i].isValid()) {
                validContoursWithData.push_back(contoursWithData[i]);
            }
        }

        sort(validContoursWithData.begin(), validContoursWithData.end(), DataContour::sortRectX);

        double meanWidthChars = 0;
        if (validContoursWithData.size() != 0) {
            meanWidthChars = getMeanWidth(validContoursWithData);
            cout << "\n\n" << "MEAN = " << meanWidthChars << "\n\n";
        }



        for (int i = 0; i < validContoursWithData.size(); i++) {



            Mat matROI = matThreshold(validContoursWithData[i].boundingRect);

            Mat matROIResized;
            resize(matROI, matROIResized, Size(IMAGE_WIDTH, IMAGE_HEIGHT));

            Mat matROIFloat;
            matROIResized.convertTo(matROIFloat, CV_32FC1);

            Mat matROIFlattenedFloat = matROIFloat.reshape(1, 1);

            Mat matCurrentChar(0, 0, CV_32F);

            matROIFlattenedFloat.reshape(1, 1);
            svm->predict(matROIFlattenedFloat, matCurrentChar);

            float fltCurrentChar = (float)matCurrentChar.at<float>(0, 0);


            outputText = outputText + char(int(fltCurrentChar));

            /*Find spaces between the chars */
            if (i + 1 < validContoursWithData.size()) {
                int currWidthX = validContoursWithData[i].boundingRect.x + validContoursWithData[i].boundingRect.width;
                int nextWidthX = validContoursWithData[i + 1].boundingRect.x;

                std::cout << "\n\n" << "Xcurr = " << currWidthX << "\n\n";
                std::cout << "Xnext = " << nextWidthX << "\n\n";
                std::cout << "NORM = " << abs(currWidthX - nextWidthX) << "\n\n";
                std::cout << "MEAN = " << meanWidthChars * 2 << "\n\n";


                if (abs(currWidthX - nextWidthX) >= meanWidthChars / 2) {
                    outputText = outputText + " ";
                }


            } else if (i == validContoursWithData.size() - 1) {
                outputText = outputText + " ";
            }
        }



    }


    for(int i = 0; i < outputText.length(); i++) {
        if(outputText[i] == '0') {
            if(i != 0 && !isdigit(outputText[i-1])) {
                outputText[i] = 'o';
            } else if(i < outputText.length() - 1 && !isdigit(outputText[i+1])){
                outputText[i] = 'o';
            }
        }
    }

    return env->NewStringUTF(outputText.c_str());
}