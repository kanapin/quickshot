package kz.edu.nu.sst.quickshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.location.Location;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class GetImageHashMapTask extends AsyncTask<Void, Void, Void> {

	Location location;
	HashMap<Bitmap, String> photosMap;

	GetImageHashMapTask(Location loc) {
		location = loc;
		photosMap = new HashMap<Bitmap, String>();
	}

	protected Void doInBackground(Void... arg0) {
		PlacesService service = new PlacesService();
		ArrayList<String> placeReferences = service.fetchReferences(location);

		ArrayList<Place> placesList = new ArrayList<Place>();
		System.out.println("doInBackground");
		if (!placeReferences.isEmpty())
			for (String ref : placeReferences) {
				Log.d("zQuickShot", ref);
				placesList.add(service.getPlace(ref));
				// if (p.getPhotos() != null)
				// for (Photo photo : p.getPhotos()) {
				// new ImageDownloadTask(photo.getPhotoReference(),
				// p.getId()).execute();
				// }
			}
		return null;
	}

	private class ImageDownloadTask extends AsyncTask<Void, Integer, Bitmap> {
		Bitmap bitmap = null;

		String reference;
		String placeID;

		ImageDownloadTask(String reference, String placeID) {
			this.reference = reference;
			this.placeID = placeID;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			System.out.println("started image task...");
			String url = "https://maps.googleapis.com/maps/api/place/photo?";
			String key = "key=AIzaSyA8bWy_dU5tOxzazJzPw7bhzIGzWqUsm9E";
			String sensor = "sensor=true";
			String maxWidth = "maxwidth=600";
			String maxHeight = "maxheight=400";
			url = url + "&" + key + "&" + sensor + "&" + maxWidth + "&"
					+ maxHeight;
			url = url + "&photoreference=" + reference;
			try {
				bitmap = downloadImage(url);
			} catch (Exception e) {
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			photosMap.put(bitmap, placeID);
			// imageView.setImageBitmap(bitmap);
		}

		Bitmap downloadImage(String strUrl) throws IOException {
			Bitmap bitmap = null;
			InputStream inputStream = null;
			try {
				URL url = new URL(strUrl);

				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				urlConnection.connect();
				inputStream = urlConnection.getInputStream();
				bitmap = BitmapFactory.decodeStream(inputStream);
			} catch (Exception e) {
			} finally {
				inputStream.close();
			}
			return bitmap;
		}
	}

}
