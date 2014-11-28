package kz.edu.nu.sst.quickshot;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1 = 100;
	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_2 = 101;
	Button imageButton;
	ImageView imageView;
	Bitmap image1;
	Uri mImageUri1;
	TextView textView;

	Bitmap image;

	TextView tv;
	PlaceList placeList = null;

	ObjectRecognitionTask task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageButton = (Button) findViewById(R.id.button1);
		imageView = (ImageView) findViewById(R.id.imageView1);

		image = Bitmap.createBitmap(600, 800, Config.ARGB_4444);
		imageView.setImageBitmap(image);

		imageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				File photo;
				try {
					photo = createTemproraryFile("picture1", "jpg");
					photo.delete();

					mImageUri1 = Uri.fromFile(photo);
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri1);
					startActivityForResult(cameraIntent,
							CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1);
				} catch (IOException e) {
					Log.v("MAIN", "can't create file to take pic!!");
				}

			}
		});
		textView = (TextView) findViewById(R.id.textView1);
		
		String [] arr = new String [] {"congresshall.xml", "shabyt.xml", "vokzal.xml", "hanshatyr.xml", "triumf.xml",
                "baiterek.xml",		"pyramid.xml",
                "keruyen.xml", "defence.xml", "nuniversity.xml"};
		byte[] buffer = new byte[1024];
		
		String pathToVocabulary = null;
		InputStream inputStream ;
		OutputStream outputStream = null ;
		try {
			inputStream = this.getAssets().open("vocabulary.yml");
			File vocabularyFile = createTemproraryFile("vocabulary", "yml");
			outputStream = new BufferedOutputStream(new FileOutputStream(vocabularyFile)); 
			pathToVocabulary = vocabularyFile.getAbsolutePath();
	        int length = 0;
	        try {
	            while ((length = inputStream.read(buffer)) > 0){
	                outputStream.write(buffer, 0, length);
	            }
	        } catch (IOException ioe) {
	            /* ignore */
	        }
			
		} catch (IOException e2) {
			e2.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		String [] classNames = new String [arr.length];
		
		for (int i = 0 ; i < arr.length ; i ++) {
			Log.d("MainActivity", "i = " + i);
			int length = 0;
			try {
				inputStream = getAssets().open(arr[i]);
				classNames[i] = arr[i].substring(0, 
						arr[i].indexOf('.'));
				File currentFile = createTemproraryFile(classNames[i], "xml");
				arr[i] = currentFile.getAbsolutePath();
				outputStream = new BufferedOutputStream(new FileOutputStream(
						currentFile));
				while ((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (outputStream != null)
					try {
						outputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
			
		}
		
		task = new ObjectRecognitionTask(textView, pathToVocabulary, arr, classNames);
		

		// Button saveButton = new Button(this);
		// saveButton.setText("Start detection!");
		//
		// saveButton.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if (image1 != null && image2 != null) {
		// imageView.setImageBitmap(image);
		// ObjectRecognitionTask task = new ObjectRecognitionTask(
		// imageView);
		// task.execute(image1, image2);
		// // ObjectRecognizer detector = new ObjectRecognizer(image1,
		// // image2, image);
		// // new Thread(detector).start();
		// }
		// }
		// });

		InputStream in = null;
		try {
			in = this.getAssets().open("data.xml");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Persister serializer = new Persister();
		try {
			placeList = serializer.read(PlaceList.class, in, false);
		} catch (Exception e) {
			e.printStackTrace();			
		}

	}

	private File createTemproraryFile(String part, String ext)
			throws IOException {
		File tempDir = Environment.getExternalStorageDirectory();
		tempDir = new File(tempDir.getAbsolutePath() + "/.temp");
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
		return File.createTempFile(part, ext, tempDir);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1
				&& resultCode == RESULT_OK) {

			// this.getContentResolver().notifyChange(mImageUri1, null);
			// ContentResolver cr = this.getContentResolver();
			Bitmap photo;

			// photo = android.provider.MediaStore.Images.Media.getBitmap(cr,
			// mImageUri1);
			photo = decodeSampledBitmapFromFile(mImageUri1.getPath(), 480, 640);
			Log.d("RESULT", "Path = " + mImageUri1.getPath());
			image1 = photo;
			imageView.setImageBitmap(photo);
			
			/********** for testing purposes *************/
			//displayResults("shabyt");
			/*********************************************/
			task.execute(mImageUri1.getPath());
			
		}
	}

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

	public void displayResults(String id) {
		Place place = null;

		for (Place p : placeList.getList()) {
			if (p.getId().equals(id)) {
				place = p;
				break;
			}
		}
		tv = (TextView) findViewById(R.id.textView1);
		if (place != null)
			tv.setText(place.getDescription());
	}
}
