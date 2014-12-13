package kz.edu.nu.sst.quickshot;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class OpenCVInit implements Runnable {
	
	Context context;
	
	String [] classNames;
	PlaceCV [] places;
	ArrayList<Place> nearestPlacesList;
	
	public OpenCVInit(Context context, ArrayList<Place> placeList) {
		if (OpenCVTool.isInitialized())
			return;
		
		this.context = context;
		
		
		this.nearestPlacesList = placeList;
		classNames = new String [nearestPlacesList.size()];
		for (int i = 0 ; i < nearestPlacesList.size() ; i ++) {
			classNames[i] = nearestPlacesList.get(i).getId();
		}
		
		places = new PlaceCV[classNames.length];
		
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
	public void run() {
		byte[] buffer = new byte[1024];

		String pathToVocabulary = null;
		InputStream inputStream;
		OutputStream outputStream = null;
		try {
			inputStream = context.getAssets().open("vocabulary.yml");
			File vocabularyFile = createTemproraryFile("vocabulary", "yml");
			outputStream = new BufferedOutputStream(new FileOutputStream(
					vocabularyFile));
			pathToVocabulary = vocabularyFile.getAbsolutePath();
			int length = 0;
			try {
				while ((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);
				}
			} catch (IOException ioe) {
				/* ignore */
			}

		} catch (IOException e2) {
			e2.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		for (int i = 0; i < classNames.length; i++) {
			Log.d("MainActivity", "i = " + i);
			int length = 0;
			try {
				inputStream = context.getAssets().open(classNames[i] + ".xml");

				File currentFile = createTemproraryFile(classNames[i], "xml");
				places[i] = new PlaceCV(classNames[i],
						currentFile.getAbsolutePath());

				outputStream = new BufferedOutputStream(new FileOutputStream(
						currentFile));
				while ((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (outputStream != null)
					try {
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

		OpenCVTool.initializePlacesForTraining(places, pathToVocabulary);
		
		Log.d("init", "Init is done");
	}

}
