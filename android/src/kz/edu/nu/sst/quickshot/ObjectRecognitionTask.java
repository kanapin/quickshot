package kz.edu.nu.sst.quickshot;

import java.lang.ref.WeakReference;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class ObjectRecognitionTask extends AsyncTask<String, Void, String> {
	
	private final WeakReference<TextView> textViewReference;
	Mat vocabulary;
	CvSVM [] classifiers;
	String [] className;
	SIFT detector;
	FlannBasedMatcher matcher;
	BOWImgDescriptorExtractor bowide;
	
	
	
	// Load classifiers and vocabulary to the memory
	public ObjectRecognitionTask(TextView imageView, String pathToVocabulary, String [] pathToClassifiers,
			String [] correspondingClassNames) 
	{
		textViewReference = new WeakReference<TextView>(imageView);
		Loader.load(opencv_core.class);
        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage(pathToVocabulary, null, opencv_core.CV_STORAGE_READ);
        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new opencv_core.Mat(cvMat);
        Log.d("OpenCV", "vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        
        classifiers = new CvSVM [pathToClassifiers.length];
        className = correspondingClassNames;
        for (int i = 0 ; i < pathToClassifiers.length ; i++) {
			Log.d("OpenCV", "Ok. Creating class name from "
					+ pathToClassifiers[i]);
        	classifiers[i] = new CvSVM();
        	classifiers[i].load(pathToClassifiers[i]);
        }
        
        detector = new SIFT();
        matcher = new FlannBasedMatcher();
        
        bowide =
                new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

        bowide.setVocabulary(vocabulary);
        Log.d("OpenCV", "Vocab is set");
	}

	@Override
	protected String doInBackground(String... arg0) {
		IplImage image = org.bytedeco.javacpp.opencv_highgui.cvvLoadImage(arg0[0]);
		
		KeyPoint keypoints = new KeyPoint();
		
		Mat input = new Mat(image);
		detector.detect(input, keypoints);
		
		Mat response_hist = new Mat();
        

        bowide.compute(input, keypoints, response_hist);


        float minf = Float.MAX_VALUE;
        String bestMatch = null;
        for (int i = 0 ; i < className.length ; i ++) {
            float res = classifiers[i].predict(response_hist,true);
            System.out.println("res = " + res);
            if (res < minf) {
                minf = res;
                bestMatch = className[i];
            }
        }
        Log.d("OpenCV", "detected " + bestMatch);
		return bestMatch;
		
	}
	@Override
    protected void onPostExecute(String s) {
		TextView view = textViewReference.get();
		if (view != null)
			view.setText("This is " + s);
    }
	
	

}
