package kz.edu.nu.sst.quickshot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;

public class PlacesService {

	private String API_KEY = "AIzaSyA8bWy_dU5tOxzazJzPw7bhzIGzWqUsm9E";

	public PlacesService() {
	}

	public ArrayList<Place> findPlaces(Location location) {

		String link = makeUrl(location.getLatitude(), location.getLongitude());

		try {
			String json = getJSON(link);

			// System.out.println(json);
			JSONObject object = new JSONObject(json);
			JSONArray array = object.getJSONArray("results");

			ArrayList<Place> arrayList = new ArrayList<Place>();

			for (int i = 0; i < array.length(); i++) {
				Place place = getPlaceFromJSON((JSONObject) array.get(i));
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

	public Place getPlaceFromJSON(JSONObject object) {

		Place place = new Place();

		try {
			if (!object.isNull("result")) {
				JSONObject res = object.getJSONObject("result");

				if (!res.isNull("name"))
					place.setName(object.getJSONObject("result").getString(
							"name"));

				if (!res.isNull("reference"))
					place.setName(object.getJSONObject("result").getString(
							"reference"));

				if (!res.isNull("photos")) {

					JSONArray photos = res.getJSONArray("photos");
					place.setPhotos(new Photo[photos.length()]);
					for (int i = 0; i < photos.length(); i++) {
						place.getPhotos()[i] = new Photo();
						place.getPhotos()[i].width = ((JSONObject) photos
								.get(i)).getInt("width");
						place.getPhotos()[i].height = ((JSONObject) photos
								.get(i)).getInt("height");
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
				if (!res.isNull("geometry")) {
					place.setLatitude(Double.parseDouble(res.getJSONObject("geometry")
							.getJSONObject("location").getString("lat")));
					place.setLongitude(Double.parseDouble(res.getJSONObject("geometry")
							.getJSONObject("location").getString("lng")));
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("EXCEPTION", e.toString());
		}
		return place;
	}

	public ArrayList<String> fetchReferences(Location location) {
		
		StringBuilder urlString = new StringBuilder(
				"https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
		urlString.append("&location=");
		urlString.append(Double.toString(location.getLatitude()));
		urlString.append(",");
		urlString.append(Double.toString(location.getLongitude()));
		urlString.append("&radius=500");
		urlString.append("&sensor=true&key=" + API_KEY);
		urlString.toString();

		String link = urlString.toString();
		String json = getJSON(link);
		ArrayList<String> referencesList = new ArrayList<String>();
		try {
			JSONObject object = new JSONObject(json);
			JSONArray array = object.getJSONArray("results");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = (JSONObject) array.get(i);
				if (!obj.isNull("reference"))
					referencesList.add(obj.getString("reference"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return referencesList;
	}

	public Place getPlace(String ref) {

		StringBuilder urlString = new StringBuilder(
				"https://maps.googleapis.com/maps/api/place/details/json?reference=");
		urlString.append(ref);
		urlString.append("&sensor=true&key=" + API_KEY);
		String link = urlString.toString();
		String json = getJSON(link);
		// System.out.println(json);
		Place place = null;

		try {
			JSONObject object = new JSONObject(json);
			place = getPlaceFromJSON(object);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return place;
	}

}
