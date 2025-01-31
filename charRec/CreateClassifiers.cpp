#include<opencv2/imgproc/imgproc.hpp>
#include<opencv2/ml/ml.hpp>
#include<opencv2/core/core.hpp>
#include<opencv2/highgui/highgui.hpp>


#include<iostream>
#include<vector>

using namespace cv;
using namespace std;

const int NUMBER_OF_CHARS = 61;
const int NUMBER_OF_TRAINING_SAMPLES = 40;

const int VALID_CONTOUR_AREA = 100;

const int IMAGE_WIDTH = 25;
const int IMAGE_HEIGHT = 30;




int main() {

	Mat classificationInts;
	Mat imagesFloats;

	int waitKeyInt;


	vector<int> charsToLearn = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',		// digits from 0-9
								 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',		// upper-case letters
								 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
								 'U', 'V', 'W', 'X', 'Y', 'Z',
								 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',		// lower-case letters
								 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
								 'u', 'v', 'w', 'x', 'y', 'z' };
	

	/* Init matrices for image processing */
	Mat trainImg;
	Mat matGrayscale;
	Mat matBlurred;
	Mat matThreshold;
	Mat matThresholdCopy;


	/* Iterate trough folder and save images to the classification files*/
	for (int i = 0; i <= NUMBER_OF_CHARS; i++) {
		int currentChar = charsToLearn.at(i);
		char learnedChar = charsToLearn.at(i);


		for (int j = 0; j < NUMBER_OF_TRAINING_SAMPLES; j++) {

			/* Create the path to the folder */
			string path = "Fonts/";
			path.append(to_string(i));
			path.append("/");
			path.append(to_string(j));
			path.append(".png");

		       
			vector<Vec4i> v4iHierarchy;  
			vector<vector<Point> > potencialContours;


			trainImg = imread(path);
		
			/*File opening error handling*/
			if (trainImg.empty()) {                               
				std::cout << "Could'n read the image!\n\n";  

				waitKeyInt = waitKey(0);
				return(0);                                                  
			}

			/* Transform to grayscale */
			cvtColor(trainImg, matGrayscale, CV_BGR2GRAY);        

			/* Aplly gaussianBlur*/
			GaussianBlur(matGrayscale,              
				matBlurred,                             
				Size(5, 5),                         
				0);                                     

			/* Aplly treshold*/
			adaptiveThreshold(matBlurred,           
				matThreshold,                              
				255,                                    
				ADAPTIVE_THRESH_GAUSSIAN_C,         
				THRESH_BINARY_INV,                  
				11,                                     
				2);                                     

														
			matThresholdCopy = matThreshold.clone();          

			/* Find the contour with potencial data -> not final*/
			findContours(matThresholdCopy,             
				potencialContours,                             
				v4iHierarchy,                          
				RETR_EXTERNAL,                      
				CHAIN_APPROX_SIMPLE);               

			for (int i = 0; i < potencialContours.size(); i++) {   

				/* Find the contours with valid data -> based on number of non zero pixels*/
				if (contourArea(potencialContours[i]) > VALID_CONTOUR_AREA) {    

					Rect foundRect = boundingRect(potencialContours[i]);               
					/* Select the Region Of Interest -> ROI */
					Mat ROI = matThreshold(foundRect);           

					/* Resize ROI to the right size and save it to the images */
					Mat ROIResized;
					resize(ROI, ROIResized, Size(IMAGE_WIDTH, IMAGE_HEIGHT));     
					Mat ROIfloats;                          
					ROIResized.convertTo(ROIfloats, CV_32FC1);
					Mat matImageFlattenedFloatFirst = ROIfloats.reshape(1, 1);       
					imagesFloats.push_back(matImageFlattenedFloatFirst);

					/* Save current char, to the classifications*/
					classificationInts.push_back(currentChar);
			
				} 
			}   



		}
		std::cout <<  i;
		std::cout << learnedChar;


	}

	trainImg.release();
	matGrayscale.release();
	matBlurred.release();
	matThreshold.release();
	matThresholdCopy.release();


	FileStorage fileStorageImages("images.xml", FileStorage::WRITE);

	/*File opening error handling*/
	if (fileStorageImages.isOpened() == false) {
		std::cout << "Couldn't save the images!\n\n";

		waitKeyInt = waitKey(0);
		return(0);
	}

	/*Save to the xml file, to "images" tag*/
	fileStorageImages << "images" << imagesFloats;
	fileStorageImages.release();

   

   

    FileStorage fileStorageClassifications("classifications.xml", cv::FileStorage::WRITE);           

	/*File opening error handling*/
    if (fileStorageClassifications.isOpened() == false) {                                                        
        std::cout << "Couldn't save the classifications!\n\n";
		
		waitKeyInt = waitKey(0);
		return(0);                                                                                      
    }

	/*Save to the xml file, to "classifications" tag*/
    fileStorageClassifications << "classifications" << classificationInts;        
    fileStorageClassifications.release();                                            

                                                                            

                                               
	cout << "Images trained!\n\n";

	classificationInts.release();
	imagesFloats.release();

	waitKey(0);

    return(0);
}




