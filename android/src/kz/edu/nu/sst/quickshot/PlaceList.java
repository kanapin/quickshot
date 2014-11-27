package kz.edu.nu.sst.quickshot;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class PlaceList {

	@ElementList(type = Place.class)
	private List<Place> places = new ArrayList<Place>();

	@Attribute
	private String name;

	public String getName() {
		return name;
	}

	public List<Place> getList() {
		return places;
	}

	public void setList(List<Place> places) {
		this.places = places;
	}

	public void setName(String name) {
		this.name = name;
	}

}
