package kz.edu.nu.sst.quickshot;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_features2d.BFMatcher;
import org.bytedeco.javacpp.opencv_features2d.DMatchVectorVector;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_nonfree;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;
import org.bytedeco.javacv.JavaCV;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class ObjectRecognizer implements Runnable {
	Bitmap scene;
	Bitmap object;
	Bitmap image;
	
	public ObjectRecognizer(Bitmap _object, Bitmap _scene, Bitmap _image) {
		scene = _scene;
		object = _object;
		image = _image;
	}

	/**
	 * Convert bitmap scene into gray Mat. Open template file into mat Get
	 * descriptors & features from both Run K-th max matcher Eliminate weak
	 * matceches Built a homography Draw rectangle on scene Mat Convert scene
	 * Mat into Bitmap
	 */
	@Override
	public void run() {

		Loader.load(opencv_core.class);
		Loader.load(opencv_nonfree.class);
		Loader.load(opencv_features2d.class);
		opencv_nonfree.initModule_nonfree();
		opencv_features2d.initModule_features2d();
		int w = scene.getWidth();
		int h = scene.getHeight();
		Log.d("Recognizer", "scene w,h=" + w + ", " + h);

		IplImage result = IplImage.create(w, h, opencv_core.IPL_DEPTH_8U, 4);
		scene.copyPixelsToBuffer(result.getByteBuffer());
		IplImage sceneIpl = IplImage.create(w, h, opencv_core.IPL_DEPTH_8U, 3);
		org.bytedeco.javacpp.opencv_imgproc.cvCvtColor(result, sceneIpl,
				org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2BGR);

		SIFT sift = new SIFT();

		Mat sceneMat = new Mat(sceneIpl);
		Mat sceneDescriptors = new Mat();
		KeyPoint sceneKeyPoints = new KeyPoint();
		sift.detectAndCompute(sceneMat, Mat.EMPTY, sceneKeyPoints,
				sceneDescriptors);

		/*
		 * String path = Environment.getExternalStorageDirectory().toString();
		 * String pathToTemplate = "/storage/emulated/0/Pictures/1.jpg";
		 * IplImage template = opencv_highgui.cvLoadImage(pathToTemplate);
		 */

		IplImage template = IplImage.create(object.getWidth(),
				object.getHeight(), opencv_core.IPL_DEPTH_8U, 4);
		object.copyPixelsToBuffer(template.getByteBuffer());
		IplImage templateIpl = IplImage.create(object.getWidth(),
				object.getHeight(), opencv_core.IPL_DEPTH_8U, 3);
		org.bytedeco.javacpp.opencv_imgproc.cvCvtColor(template, templateIpl,
				org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2BGR);

		Mat templateMat = new Mat(templateIpl);
		Mat templateDescriptors = new Mat();
		KeyPoint templateKeyPoints = new KeyPoint();
		sift.detectAndCompute(templateMat, Mat.EMPTY, templateKeyPoints,
				templateDescriptors);

		int rows = templateDescriptors.rows();
		int cols = templateDescriptors.cols();
		int depth = templateDescriptors.depth();
		int step = (int) templateDescriptors.step();

		Log.d("RECOGNIZER", "rows = " + rows + " cols = " + cols + " depth = "
				+ depth + " step = " + step);

		BFMatcher matcher = new BFMatcher();
		// opencv_features2d.BFMatcher matcher = new
		// opencv_features2d.BFMatcher();
		DMatchVectorVector matches = new DMatchVectorVector();

		long t = System.currentTimeMillis();

		matcher.knnMatch(templateDescriptors, sceneDescriptors, matches, 2);

		Log.d("REC", "time = " + (System.currentTimeMillis() - t));

		Log.d("Recongnizer", "matches = " + matches.size());


		Log.d("Recognizer", "Old size = " + matches.size());
		
		
		matches = refineMatches(matches);
		
		Mat homography = getHomography(templateKeyPoints, sceneKeyPoints, matches);
		
		double corners[][] = new double [4][2];
		
		corners[0] = new double[]{0, 0};
        corners[1] = new double[]{templateMat.cols(), 0};
        corners[2] = new double[]{templateMat.cols(), templateMat.rows()};
        corners[3] = new double[]{0, templateMat.rows()};

        // map this rectangle to the image to obtain its coordinates relative to the patch
        double[][] scene_corners = new double[4][];
        for (int i = 0; i < 4; i++) {
            scene_corners[i] = new double[2];
            JavaCV.perspectiveTransform(corners[i], scene_corners[i], homography.asCvMat());
            Log.d("RECT", scene_corners[i][0] + ", " + scene_corners[i][1]);
        }

        homography.release();
        
        drawQonMat(scene_corners, sceneMat, CvScalar.CYAN);
        
        scene = Bitmap.createBitmap(sceneMat.cols(), sceneMat.rows(), Config.ARGB_4444);
        
		
        IplImage sceneImage = sceneMat.asIplImage();
        
        
		org.bytedeco.javacpp.opencv_imgproc.cvCvtColor(sceneImage, result, 
				org.bytedeco.javacpp.opencv_imgproc.CV_RGB2RGBA);

        
        scene.copyPixelsFromBuffer(result.getByteBuffer());
		
        
        opencv_highgui.cvSaveImage("/storage/emulated/0/Pictures/saved_1.png",
				result);
		Log.d("Recognizer", "New size = " + matches.size());
		
		
		Mat finalResult = new Mat();
		opencv_features2d.drawMatches(templateMat, templateKeyPoints, sceneMat,
				sceneKeyPoints, matches, finalResult);
		opencv_highgui.cvSaveImage("/storage/emulated/0/Pictures/saved_2.png",
				finalResult.asIplImage());
		/*
		
		opencv_features2d.drawMatches(templateMat, templateKeyPoints, sceneMat,
				sceneKeyPoints, matches, finalResult);

		IplImage finalImage = finalResult.asIplImage();

		// image.copyPixelsFromBuffer(finalImage.getByteBuffer());

		opencv_highgui.cvSaveImage("/storage/emulated/0/Pictures/saved_1.png",
				finalImage);
		*/
		Log.d("Detector", "Saved!");
	}

	/**
     * Perform
     *  <a href = "http://www.cs.ubc.ca/~lowe/papers/ijcv04.pdf#page=20">Lowe's Ratio test</a>
     */
	private DMatchVectorVector refineMatches(DMatchVectorVector oldMatches) {
		// Ratio of Distances
		double RoD = 0.75;
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
