#include <cstdio>
#include <cstring>
#include <string>
#include <fstream>
#include <set>
#include <map>


#include <opencv2/opencv.hpp>
#include "opencv2/core/core.hpp"
#include "opencv2/features2d/features2d.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/calib3d/calib3d.hpp"
#include "opencv2/nonfree/nonfree.hpp"

using namespace cv;

const int DICTIONARY_SIZE = 200;

std::vector<std::string> classes_names;

std::map<std::string, Mat> classes_training_data;



void trainImages(string listFileName, string vocabularyFileName = "vocabulary.yml") {
    
    Mat vocabulary;
    FileStorage fs(vocabularyFileName.c_str(), FileStorage::READ);
    fs["vocabulary"] >> vocabulary;
    fs.release();
    
    
    SIFT detector;
    Ptr<DescriptorExtractor> extractor = DescriptorExtractor::create("SIFT");
    Ptr<DescriptorMatcher> matcher = DescriptorMatcher::create("FlannBased");

    BOWImgDescriptorExtractor bowide(extractor, matcher);

    bowide.setVocabulary(vocabulary);
    
    std::fstream in;
    in.open(listFileName, std::fstream::in);
    
    std::string imageFileName;
    Mat input;
    std::vector<KeyPoint> keypoints;
    Mat responseHistogram(1, bowide.descriptorSize(), CV_32FC1);
    
    while (in >> imageFileName)
    {
        int index = imageFileName.find('_');
        
        
        std::string cur_class_name = imageFileName.substr(0, index);
        
        imageFileName = "images/" + imageFileName;
        input = imread(imageFileName.c_str(), CV_LOAD_IMAGE_GRAYSCALE);
        detector.detect(input, keypoints);
        bowide.compute(input, keypoints, responseHistogram);
        if (classes_training_data.count(cur_class_name) == 0) {
            classes_names.push_back(cur_class_name);
            //classes_training_data.create(0, responseHistogram.cols, responseHistogram.type());
            
        }
        classes_training_data[cur_class_name].push_back(responseHistogram);
        
    }
    
    in.close();

    for (int i = 0 ; i < classes_names.size() ; i++) {
        string currentClass = classes_names[i];
        std::cout << "Training class: " << currentClass << std::endl;
        
        
        Mat samples(0, extractor->descriptorSize(), extractor->descriptorType());
        Mat labels(0,1,CV_32FC1);
        
        std::cout << "Adding " << classes_training_data[currentClass].rows << " positive" << std::endl;
        
        samples.push_back(classes_training_data[currentClass]);
        Mat class_label = Mat::ones(classes_training_data[currentClass].rows, 1, CV_32FC1);
        labels.push_back(class_label);
        
        //copy rest samples and label
        for (std::map<std::string,Mat>::iterator it1 = classes_training_data.begin();
             it1 != classes_training_data.end(); ++it1)
        {
            std::string otherClass = (*it1).first;
            if(otherClass.compare(currentClass)==0) continue; //skip class itself
            
            samples.push_back(classes_training_data[otherClass]);
            class_label = Mat::zeros(classes_training_data[otherClass].rows, 1, CV_32FC1);
            labels.push_back(class_label);
        }
        
        std::cout << "Training.." << std::endl;
        Mat samples_32f; samples.convertTo(samples_32f, CV_32F);
        if(samples.rows == 0) continue; //phantom class?!
        CvSVM classifier;
        classifier.train(samples_32f, labels);
        classifier.save(("classifiers/" + currentClass + ".xml").c_str());
        
    }
}


// TODO get list of files as a parameter
void buildVocabulary(string listFileName) {
    
    std::cout << "Creating a vocabulary\n";
    Mat input;
    
    
    
    vector<KeyPoint> keypoints;
    
    Mat descriptor;
    
    Mat featuresUnclustered;
    
    SIFT sift;
    
    
    
    std::fstream imageListFile;
    imageListFile.open(listFileName, std::fstream::in);
    
    string imageFileName;
    
    
    
    while (imageListFile >> imageFileName)
    {
        int index = imageFileName.find('_');
        
        
        std::string cur_class_name = imageFileName.substr(0, index);
        
        imageFileName = "images/" + imageFileName;
        std::cout << "Processing " << imageFileName << std::endl;
        
        input = imread(imageFileName.c_str(), CV_LOAD_IMAGE_GRAYSCALE);
        sift.detect(input, keypoints);
        
        std::cout << "Found " << keypoints.size() << " keypoints\n";
        sift.compute(input, keypoints, descriptor);
        
        featuresUnclustered.push_back(descriptor);
    }
    
    imageListFile.close();
    
    std::cout << "In total " << featuresUnclustered.rows << std::endl;
    
    
    
    
    TermCriteria tc(CV_TERMCRIT_ITER, 100, 0.0001);
    int retries = 1;
    int flags = KMEANS_RANDOM_CENTERS;
    BOWKMeansTrainer bowTrainer(DICTIONARY_SIZE, tc, retries, flags);
    
    Mat vocabulary = bowTrainer.cluster(featuresUnclustered);
    
    FileStorage fs("vocabulary.yml", FileStorage::WRITE);
    fs << "vocabulary" << vocabulary;
    fs.release();
    
    std::cout << "Saved a vocabulary to vocabulary.yml\n";
}

void testClassifiers(string testImageListFileName) {
    Mat vocabulary;
    FileStorage fs("vocabulary.yml", FileStorage::READ);
    fs["vocabulary"] >> vocabulary;
    fs.release();
    
    
    SIFT detector;
    Ptr<DescriptorExtractor> extractor = DescriptorExtractor::create("SIFT");
    Ptr<DescriptorMatcher> matcher = DescriptorMatcher::create("FlannBased");
    
    
    BOWImgDescriptorExtractor bowide(extractor, matcher);
    
    bowide.setVocabulary(vocabulary);
    
    
    const int classNumber = 10;
    std::string class_names[classNumber] = {"congresshall", "shabyt", "vokzal", "hanshatyr", "triumf",
        "baiterek",		"pyramid",
        "keruyen", "defence", "nu"};
    CvSVM classifiers[classNumber];
    for (int i = 0 ; i < classNumber ; i ++) {
        
        classifiers[i].load( ("classifiers/" + class_names[i] + ".xml").c_str() );
        std::cout << "classifiers/" + class_names[i] + ".xml loaded." << std::endl;
    }
    string testImageName;
    Mat input, response_hist, descriptor;
    std::vector<KeyPoint> keypoints;
    
    std::fstream testImageListFile;
    testImageListFile.open(testImageListFileName, std::fstream::in);
    
    int totalTestCases = 0, correct = 0;
    
    while (testImageListFile >> testImageName) {
        
        input = imread(("test-images/" + testImageName).c_str(), CV_LOAD_IMAGE_GRAYSCALE);
        
        std::cout << "Loaded " + testImageName << input.rows << " x " << input.cols << std::endl;
        totalTestCases ++;
        
        string actualClassName = testImageName.substr(0, testImageName.find('_'));
        
        detector.detect(input, keypoints);
        std::cout << "Found " << keypoints.size() << " keypoints\n";
        //extractor->compute(input, keypoints, descriptor);
        
        bowide.compute(input, keypoints, response_hist);
        std::cout << "Response hist " << response_hist.cols << std::endl;
        

        float minf = FLT_MAX;
        std::string bestMatch;
        for (int i = 0 ; i < classNumber ; i ++) {
            float res = classifiers[i].predict(response_hist,true);
            if (res < minf) {
                minf = res;
                bestMatch = class_names[i];
            }
        }
        std::cout << testImageName << " is " << bestMatch << std::endl;
        if (actualClassName == bestMatch)
            correct ++;
    }
    testImageListFile.close();
    
    std::cout << "Total: " << totalTestCases << ", correct: " << correct << "\n";
    std::cout << "Accuracy: " << 1.0 * correct / totalTestCases << "\n";
}
int main(int argc, char* argv[]) {
    
    if (argc != 4) {
        std::cout << "Usage: BuildVocabulary <files for vocabulary> <files for training> <files for testing>\n";
        return 0;
    }
    
    buildVocabulary(argv[1]);
    trainImages(argv[2]);
    testClassifiers(argv[3]);
    
    
    
    
    
    return 0;
}








