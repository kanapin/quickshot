package kz.edu.nu.sst.quickshot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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

		FindLocation task = new FindLocation();
		task.execute();

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

			ObjectRecognitionTask task = new ObjectRecognitionTask(textView);
			task.execute(mImageUri1.getPath());
		}
	}

	private void getNearestBuildings(Location location) {
		InputStream in = null;
		PlaceList list = null;
		placeList = new PlaceList();
		try {
			in = this.getAssets().open("data.xml");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Persister serializer = new Persister();
		try {
			list = serializer.read(PlaceList.class, in, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (Place p : list.getList()) {
			if (getDistance(location.getLatitude(), location.getLongitude(),
					p.getLatitude(), p.getLongitude()) < 1000) {
				placeList.getList().add(p);
				System.out.println("FOUND!!!!!!!" + p.getName());
			}
		}

		// initOpenCV();
		OpenCVInit init = new OpenCVInit(getApplicationContext(), list.getList());
		new Thread(init).start();
	}

	double getDistance(double lat1, double lon1, double lat2, double lon2) {
		int R = 6378100; // radius
		double dLat = (lat2 - lat1) * (Math.PI / 180);
		double dLon = (lon2 - lon1) * (Math.PI / 180);
		double a = (Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1
				* (Math.PI / 180))
				* Math.cos(lat2 * (Math.PI / 180))
				* Math.sin(dLon / 2)
				* Math.sin(dLon / 2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;
		return d;
	}

	public class FindLocation extends AsyncTask<String, Integer, String>
			implements LocationListener {

		Location loc;
		LocationManager locationManager;

		@Override
		protected void onPreExecute() {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0, this);
			} else {
				Toast.makeText(
						MainActivity.this,
						"Could not find your location. Please turn on GPS and restart the app...",
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			getNearestBuildings(loc);
		}

		@Override
		protected String doInBackground(String... params) {
			while (loc == null) {
			}
			return null;
		}

		@Override
		public void onLocationChanged(Location location) {
			loc = location;
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

	}
}
