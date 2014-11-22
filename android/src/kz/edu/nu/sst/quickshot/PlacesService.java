package kz.edu.nu.sst.quickshot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PlacesService {

	private String API_KEY = "AIzaSyA8bWy_dU5tOxzazJzPw7bhzIGzWqUsm9E";

	public PlacesService() {
	}

	public ArrayList<Place> findPlaces(double latitude, double longitude) {

		String link = makeUrl(latitude, longitude);

		try {
			String json = getJSON(link);

			// System.out.println(json);
			JSONObject object = new JSONObject(json);
			JSONArray array = object.getJSONArray("results");

			ArrayList<Place> arrayList = new ArrayList<Place>();

			for (int i = 0; i < array.length(); i++) {
				Place place = getPlace((JSONObject) array.get(i));
				arrayList.add(place);
			}
			return arrayList;

		} catch (JSONException ex) {
		}

		return null;
	}

	private String makeUrl(double latitude, double longitude) {
		StringBuilder urlString = new StringBuilder(
				"https://maps.googleapis.com/maps/api/place/nearbysearch/json?");

		urlString.append("&location=");
		urlString.append(Double.toString(latitude));
		urlString.append(",");
		urlString.append(Double.toString(longitude));
		urlString.append("&radius=500");
		urlString.append("&sensor=true&key=" + API_KEY);

		return urlString.toString();
	}

	protected String getJSON(String link) {

		StringBuilder content = new StringBuilder();
		try {
			URL url = new URL(link);
			URLConnection urlConnection = url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(urlConnection.getInputStream()), 8);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content.append(line + "\n");
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content.toString();
	}

	public Place getPlace(JSONObject object) {

		Place place = new Place();

		try {
			if (!object.isNull("name"))
				place.setName(object.getString("name"));

			if (!object.isNull("vicinity"))
				place.setVicinity(object.getString("vicinity"));

			if (!object.isNull("reference"))
				place.setReference(object.getString("reference"));
				
			if (!object.isNull("photos")) {
				JSONArray photos = object.getJSONArray("photos");
				place.setPhotos(new Photo[photos.length()]);
				for (int i = 0; i < photos.length(); i++) {
					place.getPhotos()[i] = new Photo();
					place.getPhotos()[i].width = ((JSONObject) photos.get(i))
							.getInt("width");
					place.getPhotos()[i].height = ((JSONObject) photos.get(i))
							.getInt("height");
					place.getPhotos()[i].photoReference = ((JSONObject) photos
							.get(i)).getString("photo_reference");
					JSONArray attributions = ((JSONObject) photos.get(i))
							.getJSONArray("html_attributions");
					place.getPhotos()[i].attributions = new String[attributions
							.length()];
					for (int j = 0; j < attributions.length(); j++) {
						place.getPhotos()[i].attributions[j] = attributions
								.getString(j);
					}
				}
			}

			place.setLatitude(Double.parseDouble(object
					.getJSONObject("geometry").getJSONObject("location")
					.getString("lat")));
			place.setLongitude(Double.parseDouble(object
					.getJSONObject("geometry").getJSONObject("location")
					.getString("lng")));

		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("EXCEPTION", e.toString());
		}
		return place;
	}

}
