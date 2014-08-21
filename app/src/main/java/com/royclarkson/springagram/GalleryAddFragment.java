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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.royclarkson.springagram.model.Gallery;

import org.springframework.web.client.RestTemplate;


/**
 * {@link Fragment} that displays a form to add a new {@link Gallery}
 *
 * @author Roy Clarkson
 */
public class GalleryAddFragment extends Fragment implements View.OnClickListener {

	private static final String TAG = GalleryAddFragment.class.getSimpleName();

	private static final String ARG_GALLERIES_LIST_URL = "galleries_url";

	private String galleriesUrl;

	private GalleryAddFragmentListener galleryAddFragmentListener;


	public GalleryAddFragment() {
		// Required empty public constructor
	}

	public static GalleryAddFragment newInstance(String galleriesUrl) {
		GalleryAddFragment fragment = new GalleryAddFragment();
		Bundle args = new Bundle();
		args.putString(ARG_GALLERIES_LIST_URL, galleriesUrl);
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
			this.galleriesUrl = getArguments().getString(ARG_GALLERIES_LIST_URL);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_gallery_add, container, false);
		Button button = (Button) view.findViewById(R.id.button_save);
		button.setOnClickListener(this);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			galleryAddFragmentListener = (GalleryAddFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement GalleryAddFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		galleryAddFragmentListener = null;
	}


	//***************************************
	// onClickListener methods
	//***************************************

	public void onClick(View view) {
		if (view.getId() == R.id.button_save) {
			new AddGalleryTask().execute(this.galleriesUrl);
		}
	}

	private String getGalleryDescription() {
		EditText descriptionText = (EditText) this.getActivity().findViewById(R.id.text_description);
		return descriptionText.getText().toString();
	}


	//***************************************
	// Listener interface
	//***************************************

	public interface GalleryAddFragmentListener {

		public void onGalleryAddComplete();

	}


	//***************************************
	// Private classes
	//***************************************

	private class AddGalleryTask extends AsyncTask<String, Void, Boolean> {

		private Gallery gallery;

		@Override
		protected void onPreExecute() {
			this.gallery = new Gallery(getGalleryDescription());
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				final String url = params[0];
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.postForLocation(url, this.gallery);
				return true;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (galleryAddFragmentListener != null) {
				galleryAddFragmentListener.onGalleryAddComplete();
			}
		}

	}

}
