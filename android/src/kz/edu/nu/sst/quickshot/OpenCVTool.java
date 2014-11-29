package kz.edu.nu.sst.quickshot;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class OpenCVTool {
	
	public final PlaceCV [] places ;
	public final String pathToVocabulary ;
	
	final Mat vocabulary;
	final CvSVM [] classifiers;
	final SIFT detector;
	final FlannBasedMatcher matcher;
	final BOWImgDescriptorExtractor bowide;
	
	public static boolean initialized = false;
	
	private static OpenCVTool instance;
	
	private OpenCVTool(PlaceCV [] _places, String _pathToVocabulary) {
		places = _places;
		pathToVocabulary = _pathToVocabulary; 
		
		Loader.load(opencv_core.class);
        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage(pathToVocabulary, null, opencv_core.CV_STORAGE_READ);
        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new opencv_core.Mat(cvMat);
        
        Log.d("OpenCV", "vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        
        classifiers = new CvSVM [places.length];
        for (int i = 0 ; i < places.length ; i++) {
			Log.d("OpenCV", "Ok. Creating class name from "
					+ places[i].className);
        	classifiers[i] = new CvSVM();
        	classifiers[i].load(places[i].absolutePathToClassifier);
        }
        
        detector = new SIFT();
        matcher = new FlannBasedMatcher();
        
        bowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

        bowide.setVocabulary(vocabulary);
        Log.d("OpenCV", "Vocab is set");
	}
	
	public static void initializePlacesForTraining(PlaceCV [] _places, String _pathToVocabulary) {
		instance = new OpenCVTool(_places, _pathToVocabulary);
		initialized = true;
	}
	public static OpenCVTool getInstance() {
		if (instance == null) 
			throw new NullPointerException("Did you call initialize first?");
		return instance;
	}
	
	/** Methods for Bitmap manipulation. */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromFile(String pathName,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
	
}
