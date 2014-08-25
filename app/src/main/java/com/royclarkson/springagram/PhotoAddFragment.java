/*
 * Copyright 2014 Roy Clarkson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.royclarkson.springagram;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.royclarkson.springagram.model.Item;
import com.royclarkson.springagram.model.ItemResource;

import org.springframework.util.support.Base64;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link Fragment} for adding a new photo within an {@link ItemResource}
 *
 * @author Roy Clarkson
 */
public class PhotoAddFragment extends Fragment {

	private static final String TAG = PhotoAddFragment.class.getSimpleName();

	private static final String ARG_PHOTOS_LIST_URL = "photos_url";

	private static final int REQUEST_TAKE_PHOTO = 1;

	private String photosUrl;

	private PhotoAddFragmentListener photoAddFragmentListener;

	private String photoPath;


	public PhotoAddFragment() {
		// Required empty public constructor
	}

	public static PhotoAddFragment newInstance(String photosUrl) {
		PhotoAddFragment fragment = new PhotoAddFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PHOTOS_LIST_URL, photosUrl);
		fragment.setArguments(args);
		return fragment;
	}


	//***************************************
	// Fragment methods
	//***************************************

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			this.photosUrl = getArguments().getString(ARG_PHOTOS_LIST_URL);
		}
		dispatchTakePictureIntent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_photo_add, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			photoAddFragmentListener = (PhotoAddFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement PhotoAddFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.photoAddFragmentListener = null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
			new AddPhotoTask().execute(this.photosUrl);
		} else {
			Toast.makeText(this.getActivity(),
					"An error occurred while capturing the photo",
					Toast.LENGTH_SHORT).show();
		}
	}


	//***************************************
	// Helper methods
	//***************************************

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		File image = File.createTempFile(imageFileName, ".jpg", storageDir);
		this.photoPath = image.getAbsolutePath();
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "PhotoPath: " + this.photoPath);
		}
		return image;
	}


	//***************************************
	// Listener interface
	//***************************************

	public interface PhotoAddFragmentListener {

		public void onPhotoAddComplete();

	}


	//***************************************
	// Private classes
	//***************************************

	private class AddPhotoTask extends AsyncTask<String, Void, Boolean> {

		private Item item;

		@Override
		protected void onPreExecute() {
			File photoFile = new File(photoPath);
			if (photoFile.exists()) {
				String name = photoFile.getName();
				String imageDataString = null;
				try {
					imageDataString = Base64.encodeFromFile(photoFile.getAbsolutePath());
				} catch (IOException e) {
					// TODO: handle error
				}
				this.item = new Item(name, imageDataString);
			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				final String url = params[0];
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.postForLocation(url, this.item);
				return true;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (photoAddFragmentListener != null) {
				photoAddFragmentListener.onPhotoAddComplete();
			}
		}

	}

}