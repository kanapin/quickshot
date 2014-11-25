#include <cstdio>
#include <cstring>
#include <string>


#include <opencv2/opencv.hpp>
#include "opencv2/core/core.hpp"
#include "opencv2/features2d/features2d.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/calib3d/calib3d.hpp"
#include "opencv2/nonfree/nonfree.hpp"

using namespace cv;

const int NUMBER_OF_CLASSES = 4;
const int DICTIONARY_SIZE = 200;

char file_name[81] = "images/list.txt";

void train_images() {
    //Step 2 - Obtain the BoF descriptor for given image/video frame. 

    //prepare BOW descriptor extractor from the dictionary    
    Mat dictionary; 
    FileStorage fs("dictionary.yml", FileStorage::READ);
    fs["vocabulary"] >> dictionary;
    fs.release();    
    


    //create a nearest neighbor matcher
    Ptr<DescriptorMatcher> matcher(new FlannBasedMatcher);
    //create Sift feature point extracter
    Ptr<FeatureDetector> detector(new SiftFeatureDetector());
    //create Sift descriptor extractor
    Ptr<DescriptorExtractor> extractor(new SiftDescriptorExtractor);    
    //create BoF (or BoW) descriptor extractor
    BOWImgDescriptorExtractor bowDE(extractor,matcher);
    //Set the dictionary with the vocabulary we created in the first step
    bowDE.setVocabulary(dictionary);

    /*
    for (int i=0;i<classes_names.size();i++) {
       string class_ = classes_names[i];
       cout << " training class: " << class_ << ".." << endl;
             
       Mat samples(0,response_cols,response_type);
       Mat labels(0,1,CV_32FC1);
             
       //copy class samples and label
       cout << "adding " << classes_training_data[class_].rows << " positive" << endl;
       samples.push_back(classes_training_data[class_]);
       Mat class_label = Mat::ones(classes_training_data[class_].rows, 1, CV_32FC1);
       labels.push_back(class_label);
             
       //copy rest samples and label
       for (map<string,Mat>::iterator it1 = classes_training_data.begin(); it1 != classes_training_data.end(); ++it1) {
          string not_class_ = (*it1).first;
          if(not_class_.compare(class_)==0) continue; //skip class itself
          samples.push_back(classes_training_data[not_class_]);
          class_label = Mat::zeros(classes_training_data[not_class_].rows, 1, CV_32FC1);
          labels.push_back(class_label);
       }
        
       cout << "Train.." << endl;
       Mat samples_32f; samples.convertTo(samples_32f, CV_32F);
       if(samples.rows == 0) continue; //phantom class?!
       CvSVM classifier;
       classifier.train(samples_32f,labels);
     
       //do something with the classifier, like saving it to file
    }

    */

}


int main() {

    
    Mat input;    

    
    //To store the keypoints that will be extracted by SIFT
    vector<KeyPoint> keypoints;
    //To store the SIFT descriptor of current image
    Mat descriptor;
    //To store all the descriptors that are extracted from all the images.
    Mat featuresUnclustered;
    //The SIFT feature extractor and descriptor
    SIFT sift;    

    // Open file with names
    freopen(file_name, "r", stdin);

    char* image_name = new char[81];
    char* dir_name = new char[21];

    for (int i = 0; i < NUMBER_OF_CLASSES ; i ++) 
    {        
        scanf("%s", dir_name);
        int len = strlen(dir_name);
        dir_name[len - 1] = 0;
        printf("Processing %s\n", dir_name);
        for (int j = 1 ; j <= 10 ; j++) 
        {
            scanf("%s", file_name);
            sprintf(image_name, "images/%s/%s", dir_name, file_name);

            puts(image_name);
            
            input = imread(image_name, CV_LOAD_IMAGE_GRAYSCALE); //Load as grayscale                
            //detect feature points
            sift.detect(input, keypoints);

            printf("Found %d keypoints\n", keypoints.size());
            //compute the descriptors for each keypoint
            sift.compute(input, keypoints, descriptor);        
            //put the all feature descriptors in a single Mat object 
            featuresUnclustered.push_back(descriptor);        
        }
        getchar();
    }    

    printf("In total %d rows\n", featuresUnclustered.rows);
    


    
    //define Term Criteria
    TermCriteria tc(CV_TERMCRIT_ITER, 100, 0.0001);
    //retries number
    int retries=1;
    //necessary flags
    int flags=KMEANS_RANDOM_CENTERS;
    //Create the BoW (or BoF) trainer
    BOWKMeansTrainer bowTrainer(DICTIONARY_SIZE,tc,retries,flags);
    //cluster the feature vectors
    Mat dictionary=bowTrainer.cluster(featuresUnclustered);    
    //store the vocabulary
    FileStorage fs("dictionary.yml", FileStorage::WRITE);
    fs << "vocabulary" << dictionary;
    fs.release();
}