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
import android.graphics.Typeface;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1 = 100;
	Button imageButton;
	ImageView imageView;
	Bitmap image;
	Uri mImageUri1;
	TextView textView;
	LinearLayout layout;

	TextView tv;
	static PlaceList placeList = null;

	public void initOpenCV() {
		if (OpenCVTool.initialized)
			return;
		String[] classNames = new String[] { "congresshall", "shabyt",
				"vokzal", "hanshatyr", "triumf", "baiterek", "pyramid",
				"keruyen", "defence", "nuniversity" };

		PlaceCV[] places = new PlaceCV[classNames.length];

		byte[] buffer = new byte[1024];

		String pathToVocabulary = null;
		InputStream inputStream;
		OutputStream outputStream = null;
		try {
			inputStream = this.getAssets().open("vocabulary.yml");
			File vocabularyFile = createTemproraryFile("vocabulary", "yml");
			outputStream = new BufferedOutputStream(new FileOutputStream(
					vocabularyFile));
			pathToVocabulary = vocabularyFile.getAbsolutePath();
			int length = 0;
			try {
				while ((length = inputStream.read(buffer)) > 0) {
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
					e.printStackTrace();
				}
			}
		}

		for (int i = 0; i < classNames.length; i++) {
			Log.d("MainActivity", "i = " + i);
			int length = 0;
			try {
				inputStream = getAssets().open(classNames[i] + ".xml");

				File currentFile = createTemproraryFile(classNames[i], "xml");
				places[i] = new PlaceCV(classNames[i],
						currentFile.getAbsolutePath());

				outputStream = new BufferedOutputStream(new FileOutputStream(
						currentFile));
				while ((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (outputStream != null)
					try {
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

		OpenCVTool.initializePlacesForTraining(places, pathToVocabulary);
	}

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
		Typeface type = Typeface.createFromAsset(getAssets(),
				"Kingthings Exeter.ttf");
		textView.setTypeface(type);
		imageButton.setTypeface(type);
		layout = (LinearLayout) findViewById(R.id.layout1);
		layout.setBackgroundResource(R.drawable.pic4);

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

		initOpenCV();
		Toast toast = Toast.makeText(getApplicationContext(),
				"OpenCV tools were inited", Toast.LENGTH_SHORT);
		toast.show();
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

			Bitmap photo;

			photo = OpenCVTool.decodeSampledBitmapFromFile(
					mImageUri1.getPath(), 480, 640);
			Log.d("RESULT", "Path = " + mImageUri1.getPath());
			image = photo;
			imageView.setImageBitmap(photo);

			/********** for testing purposes *************/
			// displayResults("shabyt");
			/*********************************************/
			ObjectRecognitionTask task = new ObjectRecognitionTask(textView);
			task.execute(mImageUri1.getPath());
		}
	}

}
