package kz.edu.nu.sst.quickshot;


public class Place {
	private String id;
	private String icon;
	private String name;
	private String vicinity;
	private Double latitude;
	private Double longitude;
	private String reference;
	
	private Photo photos[];

	public Photo[] getPhotos() {
		return photos;
	}

	public void setPhotos(Photo[] photos) {
		this.photos = photos;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVicinity() {
		return vicinity;
	}

	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}

	// static Place getPlace(JSONObject object) {
	// try {
	// Place place = new Place();
	// JSONObject geometry = (JSONObject) object.get("geometry");
	// JSONObject location = (JSONObject) geometry.get("location");
	// place.setLatitude((Double) location.get("lat"));
	// place.setLongitude((Double) location.get("lng"));
	// place.setIcon(object.getString("icon"));
	// place.setName(object.getString("name"));
	// place.setVicinity(object.getString("vicinity"));
	// place.setId(object.getString("id"));
	// if(!object.isNull("photos")){
	// place.setPhoto(new Photo(object.getString("photo-reference"),
	// Integer.parseInt(object.getString("height")), Integer
	// .parseInt(object.getString("width"))));
	// }
	// return place;
	// } catch (JSONException ex) {
	// }
	// return null;
	// }

	
}