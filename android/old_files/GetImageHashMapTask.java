package kz.edu.nu.sst.quickshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;

public class GetImageHashMapTask extends AsyncTask<Void, Void, Void> {

	double lat;// Location location;
	double lon;

	public GetImageHashMapTask(double lat, double lon) {
		// location = loc;
		this.lat = lat;
		this.lon = lon;
	}

	protected Void doInBackground(Void... arg0) {
		PlacesService service = new PlacesService();
		ArrayList<String> placeReferences = service.fetchReferences(lat, lon);
		ArrayList<Place> placesList = new ArrayList<Place>();
		if (!placeReferences.isEmpty())
			for (String ref : placeReferences) {
				Place place = service.getPlace(ref);
				placesList.add(place);
				if (place.getPhotos() != null)
					for (Photo photo : place.getPhotos()) {
						new ImageDownloadTask(photo.getPhotoReference(),
								place.getReference()).execute();
					}
			}
		return null;
	}

	private class ImageDownloadTask extends AsyncTask<Void, Integer, Bitmap> {
		Bitmap bitmap = null;

		String reference;
		String placeReference;

		ImageDownloadTask(String reference, String placeReference) {
			this.reference = reference;
			this.placeReference = placeReference;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
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
			MainActivity.photosMap.put(placeReference, bitmap);
			// MainActivity.imageView.setImageBitmap(bitmap);
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
