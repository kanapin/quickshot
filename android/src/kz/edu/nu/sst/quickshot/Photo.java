package kz.edu.nu.sst.quickshot;

public class Photo {

	String photoReference = "";
	int height = 300;
	int width = 300;
	String attributions [];

	public Photo(String photoReference, int height, int width) {
		this.photoReference = photoReference;
		this.height = height;
		this.width = width;
	}
	
	public Photo() {
	}

	public String getPhotoReference() {
		return photoReference;
	}

	public void setPhotReference(String photoReference) {
		this.photoReference = photoReference;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String[] getAttributions() {
		return attributions;
	}

	public void setAttributions(String[] attributions) {
		this.attributions = attributions;
	}

}
