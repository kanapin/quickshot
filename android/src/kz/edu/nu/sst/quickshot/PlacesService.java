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

	public double getPlaceRatingFromJSON(JSONObject object) {

		Place place = new Place();
		double rating = 0;
		try {
			if (!object.isNull("result")) {
				JSONObject res = object.getJSONObject("result");

				if (!res.isNull("rating"))
					rating = object.getJSONObject("result").getDouble("rating");
			}

		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("EXCEPTION", e.toString());
		}
		return rating;
	}

	public double getPlace(String ref) {

		StringBuilder urlString = new StringBuilder(
				"https://maps.googleapis.com/maps/api/place/details/json?reference=");
		urlString.append(ref);
		urlString.append("&sensor=true&key=" + API_KEY);
		String link = urlString.toString();
		String json = getJSON(link);
		double rating = 0;
		try {
			JSONObject object = new JSONObject(json);
			rating = getPlaceRatingFromJSON(object);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return rating;
	}

}
