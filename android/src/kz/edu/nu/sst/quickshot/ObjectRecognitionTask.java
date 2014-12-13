package kz.edu.nu.sst.quickshot;

import java.lang.ref.WeakReference;

import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_features2d.DMatchVectorVector;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacv.JavaCV;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class ObjectRecognitionTask extends AsyncTask<String, Void, String> {

	private final WeakReference<TextView> textViewReference;
	private final WeakReference<ImageView> imageViewReference;
	private OpenCVTool instance;
	private Resources res;
	private Bitmap newImage;
	private String packageName;
	private static Place detectedPlace;
	public synchronized Place getPlace() {
		return detectedPlace;
	}
	public synchronized void setDetectedPlace(Place place) {
		detectedPlace = place;
	}

	private final static int WORKING_WIDTH = 480, WORKING_HEIGHT = 640;

	// Load classifiers and vocabulary to the memory
	public ObjectRecognitionTask(TextView imageView, Resources _res, ImageView view, String _packageName) {
		res = _res;
		textViewReference = new WeakReference<TextView>(imageView);
		imageViewReference = new WeakReference<ImageView>(view);
		packageName = _packageName;
		detectedPlace = null;
	}

	@Override
	protected String doInBackground(String... arg0) {
		// Wait until singleton instance of OpenCVTool is initialized ...
		while (!OpenCVTool.isInitialized()) {
			// Busy waiting
		}
		// Not proceed to recognition
		instance = OpenCVTool.getInstance();
		Log.d("RecognitionTask", "Started");
		
		long time = System.currentTimeMillis();
		long timeForDecoding = System.currentTimeMillis();
		Bitmap bitmapImage = OpenCVTool.decodeSampledBitmapFromFile(arg0[0],
				WORKING_WIDTH, WORKING_HEIGHT);
		timeForDecoding = System.currentTimeMillis() - timeForDecoding;
		Log.d("RecognitionTask", "time for decoding = " + timeForDecoding);
		
		int w = bitmapImage.getWidth(), h = bitmapImage.getHeight();
		Log.d("RecognitionTask", "w, h = " + w + ", " + h);
		IplImage initialImage = IplImage.create(w, h, opencv_core.IPL_DEPTH_8U,
				4);
		
		
		long timeConvert = System.currentTimeMillis();
		bitmapImage.copyPixelsToBuffer(initialImage.getByteBuffer());
		IplImage image = IplImage.create(w, h, opencv_core.IPL_DEPTH_8U, 3);
		org.bytedeco.javacpp.opencv_imgproc.cvCvtColor(initialImage, image,
				org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2BGR);
		timeConvert = System.currentTimeMillis() - timeConvert;
		Log.d("REC", "time for conversion: " + timeConvert);

		long timeToExtract = System.currentTimeMillis();
		// Extracting keypoints
		KeyPoint keypoints = new KeyPoint();
		Mat input = new Mat(image);
		Mat inputDescriptors = new Mat();
		instance.detector.detectAndCompute(input, Mat.EMPTY, keypoints, inputDescriptors);
		Mat response_hist = new Mat();
		timeToExtract = System.currentTimeMillis() - timeToExtract;
		Log.d("Rec", "time to extract = " + timeToExtract);
		
		long timeForCompute = System.currentTimeMillis();
		instance.bowide.compute(input, keypoints, response_hist);
		timeForCompute = System.currentTimeMillis() - timeForCompute;
		Log.d("Rec", "Computed histogram in " + timeForCompute);
		
		// Finding best match
		float minf = Float.MAX_VALUE;
		String bestMatch = null;
		long timePrediction = System.currentTimeMillis();
		for (int i = 0; i < instance.places.length; i++) {
			float res = instance.classifiers[i].predict(response_hist, true);
			Log.d("OPENCV", instance.places[i].className + " is " + res);
			if (res < minf) {
				minf = res;
				bestMatch = instance.places[i].className;
			}
		}
		timePrediction = System.currentTimeMillis() - timePrediction;
		Log.d("Rec", "detected " + bestMatch + " in " + timePrediction + " ms");
		
		time = System.currentTimeMillis() - time;
		Log.d("REC", "time for the first = " + time);
		time = System.currentTimeMillis();
		
		// TODO create map
		
		int id = res.getIdentifier(bestMatch, "raw", packageName);
		Bitmap templateImageBitmap = decodeSampledBitmapFromResource(res, id, 500, 500);
		
		initialImage = IplImage.create(w, h, opencv_core.IPL_DEPTH_8U,	4);
		templateImageBitmap.copyPixelsToBuffer(initialImage.getByteBuffer());

		IplImage templateImage = IplImage.create(w, h, opencv_core.IPL_DEPTH_8U, 3);

		org.bytedeco.javacpp.opencv_imgproc.cvCvtColor(initialImage, templateImage,
				org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2BGR);
		
		Mat templateMat = new Mat(templateImage);
		KeyPoint templateKeypoints = new KeyPoint();
		Mat templateDescriptors = new Mat();
		
		instance.detector.detectAndCompute(templateMat, Mat.EMPTY, templateKeypoints, templateDescriptors);
		
		double [][] scene_corners = findObject(templateMat, templateKeypoints, templateDescriptors, input, keypoints, inputDescriptors);
		if (scene_corners != null ) {
			drawQonMat(scene_corners, input, CvScalar.CYAN);
			Log.d("Rec", "Drawing");
			
			IplImage sceneImage = input.asIplImage();
			newImage = Bitmap.createBitmap(sceneImage.width(), sceneImage.height(), Config.ARGB_4444);
	        
			org.bytedeco.javacpp.opencv_imgproc.cvCvtColor(sceneImage, initialImage, 
					org.bytedeco.javacpp.opencv_imgproc.CV_RGB2RGBA);

	        
	        newImage.copyPixelsFromBuffer(initialImage.getByteBuffer());
		} else {
			Log.d("Rec", "scene_corners is null!!!");
			return null;
		}
		time = System.currentTimeMillis() - time;
		Log.d("REC", "time for the second = " + time);
		
		image.release();
		return bestMatch;

	}
	private double [][] findObject(Mat templateMat, KeyPoint templateKeypoints, Mat templateDescriptors, 
			Mat sceneMat, KeyPoint sceneKeypoints, Mat sceneDescriptors) {
		
		
		// TODO: FlannBasedMatcher
		FlannBasedMatcher matcher = new FlannBasedMatcher();
		
		DMatchVectorVector matches12 = new DMatchVectorVector();
		DMatchVectorVector matches21 = new DMatchVectorVector();
		DMatchVectorVector matches = new DMatchVectorVector();

		long t = System.currentTimeMillis();

		matcher.knnMatch(templateDescriptors, sceneDescriptors, matches12, 2);
		matcher.knnMatch(sceneDescriptors, templateDescriptors, matches21, 2);
		
		matches12 = refineMatches(matches12);
		matches21 = refineMatches(matches21);
		
		Log.d("Recongnizer", "matches12 = " + matches12.size());
		Log.d("Recongnizer", "matches21 = " + matches21.size());
		
		matches = symmetryTest(matches12, matches21);
		
		
		//matcher.knnMatch(templateDescriptors, sceneDescriptors, matches, 2);
		//matches = refineMatches(matches);

		Log.d("REC", "time = " + (System.currentTimeMillis() - t));

		Log.d("Recongnizer", "matches = " + matches.size());

		Log.d("Recognizer", "Old size = " + matches.size());
		
		
		
		
		Log.d("Rec", "New size = " + matches.size());
		if (matches.size() < 4) {
			return null;
		}
		
		Mat homography = getHomography(templateKeypoints, sceneKeypoints, matches);
		
		double corners[][] = new double [4][2];
		
		corners[0] = new double[]{0, 0};
        corners[1] = new double[]{templateMat.cols(), 0};
        corners[2] = new double[]{templateMat.cols(), templateMat.rows()};
        corners[3] = new double[]{0, templateMat.rows()};

        // map this rectangle to the image to obtain its coordinates relative to the patch
        double [][] scene_corners = new double[4][];
        for (int i = 0; i < 4; i++) {
            scene_corners[i] = new double[2];
            JavaCV.perspectiveTransform(corners[i], scene_corners[i], homography.asCvMat());
            Log.d("RECT", scene_corners[i][0] + ", " + scene_corners[i][1]);
        }

        homography.release();
        return scene_corners;
	}

	@Override
	protected void onPostExecute(String s) {
		if (s != null) {
			displayResults(s);
			Log.d("REC", "Set imageView");
			ImageView view = imageViewReference.get();
			if (view != null) {
				if (newImage != null) {
					view.setImageBitmap(newImage);
				} else {
					Log.d("Rec", "newImage is null!!!");
				}
			}
		}
		
	}

	public void displayResults(String id) {
		Place place = null;

		for (Place p : MainActivity.placeList.getList()) {
			if (p.getId().equals(id)) {
				place = p;
				break;
			}
		}
		setDetectedPlace(place);
		TextView view = textViewReference.get();
		if (place != null && view != null)
			view.setText(place.getName() + "\n" + place.getDescription());
	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = OpenCVTool.calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}

	
	private DMatchVectorVector refineMatches(DMatchVectorVector oldMatches) {
		// Ratio of Distances
		double RoD = 0.8;
		opencv_features2d.DMatchVectorVector newMatches = new opencv_features2d.DMatchVectorVector();

		// Refine results 1: Accept only those matches, where best dist is < RoD
		// of 2nd best match.
		int sz = 0;
		newMatches.resize(oldMatches.size());

		double maxDist = 0.0, minDist = 1e100; // infinity

		for (int i = 0; i < oldMatches.size(); i++) {
			newMatches.resize(i, 1);
			if (oldMatches.get(i, 0).distance() < RoD
					* oldMatches.get(i, 1).distance()) {
				newMatches.put(sz, 0, oldMatches.get(i, 0));
				sz++;
				double distance = oldMatches.get(i, 0).distance();
				if (distance < minDist)
					minDist = distance;
				if (distance > maxDist)
					maxDist = distance;
			}
		}
		newMatches.resize(sz);

		// Refine results 2: accept only those matches which distance is no more
		// than 3x greater than best match
		sz = 0;
		opencv_features2d.DMatchVectorVector brandNewMatches = new opencv_features2d.DMatchVectorVector();
		brandNewMatches.resize(newMatches.size());
		for (int i = 0; i < newMatches.size(); i++) {
			// TODO: Move this weights into params
			// Since minDist may be equal to 0.0, add some non-zero value
			if (newMatches.get(i, 0).distance() <= 3 * minDist) {
				brandNewMatches.resize(sz, 1);
				brandNewMatches.put(sz, 0, newMatches.get(i, 0));
				sz++;
			}
		}
		brandNewMatches.resize(sz);
		return brandNewMatches;
	}
	
	/**
     * Finds homography between two sets of keypoints using <a href="http://en.wikipedia.org/wiki/RANSAC">RANSAC</a>
     * algorithm. Does two iterations: first separates the outliers from inliers,
     * the second iteration performs the RANSAC inliers only to achieve better result.
     * @param logoKeyPoints the key points of the logo template
     * @param frameRegionKeyPoints key points of the frame patch
     * @return 3x3 transformation matrix
     */
    private opencv_core.Mat getHomography(KeyPoint logoKeyPoints, KeyPoint frameRegionKeyPoints, DMatchVectorVector matches) {

        // First iteration: find homography matrix and outliers
        int size = (int)matches.size();
        opencv_core.CvMat _src = opencv_core.cvCreateMat(size, 2, opencv_core.CV_32FC1);
        opencv_core.CvMat _dst = opencv_core.cvCreateMat(size, 2, opencv_core.CV_32FC1);
        for (int i = 0 ; i < size ; i ++) {
            int queryIndex = matches.get(i, 0).queryIdx();
            int trainIndex = matches.get(i, 0).trainIdx();
            opencv_core.Point2f logoPoint = logoKeyPoints.position(queryIndex).pt();
            opencv_core.Point2f frameRegionPoint = frameRegionKeyPoints.position(trainIndex).pt();
            logoKeyPoints.position(0);
            frameRegionKeyPoints.position(0);
            _src.put(i, 0, logoPoint.x());
            _src.put(i, 1, logoPoint.y());
            _dst.put(i, 0, frameRegionPoint.x());
            _dst.put(i, 1, frameRegionPoint.y());
        }
        opencv_core.Mat src = new opencv_core.Mat( _src );
        opencv_core.Mat dst = new opencv_core.Mat( _dst );
        opencv_core.Mat mask = new opencv_core.Mat(src.rows(), 1, opencv_core.TYPE_MASK);

        // Information about outliers will be stored in mask
        opencv_core.Mat h = opencv_calib3d.findHomography(src, dst, opencv_calib3d.RANSAC,
                1.0, mask); // reprojection threshold = 1.0 for RANSAC

        // Second iteration: using only inliers
        opencv_core.Mat __src = new opencv_core.Mat(0, 2, opencv_core.CV_32FC1);
        opencv_core.Mat __dst = new opencv_core.Mat(0, 2, opencv_core.CV_32FC1);

        opencv_core.CvMat cvMat = mask.asCvMat();
        for (int i = 0 ; i < cvMat.rows() ; i ++) {
            if (cvMat.get(i, 0) == 1.0) { // discard outliers
                __src.push_back(src.row(i));
                __dst.push_back(dst.row(i));
            }
        }

        mask = new opencv_core.Mat(__src.rows(), 1, opencv_core.TYPE_MASK);

        // Find a homography matrix based on only inliers
        h = opencv_calib3d.findHomography(__src, __dst, opencv_calib3d.RANSAC,
                1.0, mask);
        // Force release
        __src.release();
        __dst.release();
        mask.release();
        return h;
    }
    
    private opencv_features2d.DMatchVectorVector symmetryTest(
            opencv_features2d.DMatchVectorVector matches12,
            opencv_features2d.DMatchVectorVector matches21)
    {
        opencv_features2d.DMatchVectorVector matches = new opencv_features2d.DMatchVectorVector();
        matches.resize(Math.max(matches12.size(), matches21.size()));
        int sz = 0;
        // Checking every pair of matches and choosing symmetric ones.
        for (int i = 0 ; i < matches12.size() ; i ++) {
            for (int j = 0 ; j < matches21.size() ; j ++) {
                opencv_features2d.DMatch dMatch12 = matches12.get(i, 0), dMatch21 = matches21.get(j, 0);
                if (dMatch12.queryIdx() == dMatch21.trainIdx() && dMatch12.trainIdx() == dMatch21.queryIdx()) {
                    matches.resize(sz, 1);
                    matches.put(sz, 0, dMatch12);
                    sz ++;
                    break;
                }
            }
        }
        matches.resize(sz);
        return matches;
    }
    
    /**
     *  Draws quadrilateral on given image matrix with color given as a scalar
     * @param Q 4x2 array with coordinates of the quadrilateral
     * @param finalImage image matrix on which to draw an image
     * @param scalar the color
     */
    public static void drawQonMat(double [][] Q, opencv_core.Mat finalImage, opencv_core.CvScalar scalar) {
        opencv_core.Scalar color = new opencv_core.Scalar(scalar);
        for (int i = 0; i < 4; i++) {
            opencv_core.line(finalImage, new opencv_core.Point((int) Q[i][0], (int) Q[i][1]),
                    new opencv_core.Point((int) Q[(i + 1) % 4][0], (int) Q[(i + 1) % 4][1]), color, 4, 4, 0);

        }
    }
}
