package kz.edu.nu.sst.quickshot;

import java.lang.ref.WeakReference;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class ObjectRecognitionTask extends AsyncTask<String, Void, String> {

	private final WeakReference<TextView> textViewReference;
	private OpenCVTool instance;

	private final static int WORKING_WIDTH = 480, WORKING_HEIGHT = 640;

	// Load classifiers and vocabulary to the memory
	public ObjectRecognitionTask(TextView imageView) {
		textViewReference = new WeakReference<TextView>(imageView);
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
		
		// Loading scaled image
		Bitmap bitmapImage = OpenCVTool.decodeSampledBitmapFromFile(arg0[0],
				WORKING_WIDTH, WORKING_HEIGHT);

		
		int w = bitmapImage.getWidth(), h = bitmapImage.getHeight();

		Log.d("RecognitionTask", "w, h = " + w + ", " + h);

		IplImage initialImage = IplImage.create(w, h, opencv_core.IPL_DEPTH_8U,
				4);

		bitmapImage.copyPixelsToBuffer(initialImage.getByteBuffer());

		IplImage image = IplImage.create(w, h, opencv_core.IPL_DEPTH_8U, 3);

		org.bytedeco.javacpp.opencv_imgproc.cvCvtColor(initialImage, image,
				org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2BGR);

		// Extracting keypoints
		KeyPoint keypoints = new KeyPoint();

		Mat input = new Mat(image);
		instance.detector.detect(input, keypoints);

		Mat response_hist = new Mat();

		instance.bowide.compute(input, keypoints, response_hist);
		// Finding best match
		float minf = Float.MAX_VALUE;
		String bestMatch = null;
		for (int i = 0; i < instance.places.length; i++) {
			float res = instance.classifiers[i].predict(response_hist, true);
			Log.d("OPENCV", instance.places[i].className + " is " + res);
			if (res < minf) {
				minf = res;
				bestMatch = instance.places[i].className;
			}
		}
		Log.d("OpenCV", "detected " + bestMatch);
		image.release();
		return bestMatch;

	}

	@Override
	protected void onPostExecute(String s) {
		displayResults(s);
	}

	public void displayResults(String id) {
		Place place = null;

		for (Place p : MainActivity.placeList.getList()) {
			if (p.getId().equals(id)) {
				place = p;
				break;
			}
		}
		TextView view = textViewReference.get();
		if (place != null && view != null)
			view.setText(place.getName() + "\n" + place.getDescription());
	}

}
