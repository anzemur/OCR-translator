#include<opencv2/core/core.hpp>
#include<opencv2/highgui/highgui.hpp>
#include<opencv2/imgproc/imgproc.hpp>
#include<opencv2/ml/ml.hpp>

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
	


	/* Iterate trough folder and save images to the classification files*/
	for (int i = 10; i <= NUMBER_OF_CHARS; i++) {
		int currentChar = charsToLearn.at(i);
		char learnedChar = charsToLearn.at(i);


		for (int j = 0; j < NUMBER_OF_TRAINING_SAMPLES; j++) {
			string path = "Fonts/";
			path.append(to_string(i));
			path.append("/");
			path.append(to_string(j));
			path.append(".png");


			Mat trainImg;        
			Mat imgGrayscale;               
			Mat imgBlurred;                 
			Mat imgThresh;                  
			Mat imgThreshCopy;              

			vector<vector<Point> > potencialContours;        
			vector<Vec4i> v4iHierarchy;                    


			trainImg = imread(path);
		
			/*File opening error handling*/
			if (trainImg.empty()) {                               
				std::cout << "Could'n read the image!\n\n";  

				waitKeyInt = waitKey(0);
				return(0);                                                  
			}

			cvtColor(trainImg, imgGrayscale, CV_BGR2GRAY);        

			GaussianBlur(imgGrayscale,              
				imgBlurred,                             
				Size(5, 5),                         
				0);                                     

														
			adaptiveThreshold(imgBlurred,           
				imgThresh,                              
				255,                                    
				ADAPTIVE_THRESH_GAUSSIAN_C,         
				THRESH_BINARY_INV,                  
				11,                                     
				2);                                     

														
			imgThreshCopy = imgThresh.clone();          

			findContours(imgThreshCopy,             
				potencialContours,                             
				v4iHierarchy,                          
				RETR_EXTERNAL,                      
				CHAIN_APPROX_SIMPLE);               

			for (int i = 0; i < potencialContours.size(); i++) {                           
				if (contourArea(potencialContours[i]) > VALID_CONTOUR_AREA) {    

					Rect boundingRect1 = boundingRect(potencialContours[i]);               
					Mat matROI = imgThresh(boundingRect1);           

					Mat matROIResized;
					resize(matROI, matROIResized, Size(IMAGE_WIDTH, IMAGE_HEIGHT));     
					classificationInts.push_back(currentChar);

					Mat matImageFloatFirst;                          
					matROIResized.convertTo(matImageFloatFirst, CV_32FC1);
					Mat matImageFlattenedFloatFirst = matImageFloatFirst.reshape(1, 1);       
					imagesFloats.push_back(matImageFlattenedFloatFirst);
			
				} 
			}   



		}
		std::cout <<  i;
		std::cout << learnedChar;


	}



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
	int c = waitKey(0);

    return(0);
}




