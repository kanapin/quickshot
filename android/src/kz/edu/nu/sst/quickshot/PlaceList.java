package kz.edu.nu.sst.quickshot;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class PlaceList {

	@ElementList(type = Place.class)
	private ArrayList<Place> places = new ArrayList<Place>();

	@Attribute
	private String name;

	public String getName() {
		return name;
	}

	public ArrayList<Place> getList() {
		return places;
	}

	public void setList(ArrayList<Place> places) {
		this.places = places;
	}

	public void setName(String name) {
		this.name = name;
	}

}
