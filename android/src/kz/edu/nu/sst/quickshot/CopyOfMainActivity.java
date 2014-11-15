package kz.edu.nu.sst.quickshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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

public class CopyOfMainActivity extends Activity {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1 = 100;
	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_2 = 101;
	Button image1Button;
	Button image2Button;
	ImageView imageView;
	Bitmap image1, image2;
	Uri mImageUri1, mImageUri2;

	Bitmap image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		image1Button = new Button(this);
		image1Button.setText("1");

		image2Button = new Button(this);
		image2Button.setText("2");

		imageView = new ImageView(this);

		image = Bitmap.createBitmap(600, 800, Config.ARGB_4444);
		imageView.setImageBitmap(image);

		image1Button.setOnClickListener(new View.OnClickListener() {
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

		image2Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				File photo;
				try {
					photo = createTemproraryFile("picture2", "jpg");
					photo.delete();

					mImageUri2 = Uri.fromFile(photo);
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri2);
					startActivityForResult(cameraIntent,
							CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_2);
				} catch (IOException e) {
					Log.v("MAIN", "can't create file to take pic!!");
				}

			}
		});

		Button saveButton = new Button(this);
		saveButton.setText("Start detection!");

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (image1 != null && image2 != null) {
					ObjectRecognizer detector = new ObjectRecognizer(image1,
							image2, image);
					new Thread(detector).start();
				}
			}
		});
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.addView(image1Button);
		layout.addView(image2Button);
		layout.addView(saveButton);
		layout.addView(imageView);

		setContentView(layout);

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

			this.getContentResolver().notifyChange(mImageUri1, null);
			ContentResolver cr = this.getContentResolver();
			Bitmap photo;
			try {
				photo = android.provider.MediaStore.Images.Media.getBitmap(cr,
						mImageUri1);
				image1 = photo;
				imageView.setImageBitmap(photo);
				Log.d("MAIN", "Successfully made imageView");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_2
				&& resultCode == RESULT_OK) {

			this.getContentResolver().notifyChange(mImageUri2, null);
			ContentResolver cr = this.getContentResolver();
			Bitmap photo;
			try {
				photo = android.provider.MediaStore.Images.Media.getBitmap(cr,
						mImageUri2);
				image2 = photo;
				imageView.setImageBitmap(photo);
				Log.d("MAIN", "Successfully made imageView");

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
