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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;

import com.royclarkson.springagram.model.GalleryResource;
import com.royclarkson.springagram.model.ItemResource;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * {@link Fragment} that displays a list of {@link GalleryResource}s to associate with an {@link ItemResource}.
 *
 * @author Roy Clarkson
 */
public class PhotoAddToGalleryFragment extends Fragment implements AbsListView.OnItemClickListener, View.OnClickListener {

	private static final String TAG = PhotoAddToGalleryFragment.class.getSimpleName();

	private static final String ARG_PHOTO_GALLERY_URL = "photo_gallery_url";

	private PhotoAddToGalleryFragmentListener photoAddToGalleryFragmentListener;

	private String itemGalleryUrl;

	private String galleryUrl;

	private List<GalleryResource> galleries;

	private AbsListView listView;


	public PhotoAddToGalleryFragment() {
		// Required empty public constructor
	}

	public static PhotoAddToGalleryFragment newInstance(String itemGalleryUrl) {
		PhotoAddToGalleryFragment fragment = new PhotoAddToGalleryFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PHOTO_GALLERY_URL, itemGalleryUrl);
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
			this.itemGalleryUrl = getArguments().getString(ARG_PHOTO_GALLERY_URL);
		}
		this.galleries = this.photoAddToGalleryFragmentListener.getGalleryList();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_add_to_gallery_list, container, false);
		this.listView = (AbsListView) view.findViewById(android.R.id.list);
		this.listView.setOnItemClickListener(this);
		this.listView.setAdapter(new PhotoAddToGalleryListAdapter(getActivity(), this.galleries));
		Button button = (Button) view.findViewById(R.id.button_save);
		button.setOnClickListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.photoAddToGalleryFragmentListener = (PhotoAddToGalleryFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement PhotoAddToGalleryFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.photoAddToGalleryFragmentListener = null;
	}


	//***************************************
	// OnItemClickListener methods
	//***************************************

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (null != this.photoAddToGalleryFragmentListener) {
			GalleryResource selectedGallery = this.photoAddToGalleryFragmentListener.getGalleryByPosition(position);
			this.galleryUrl = selectedGallery.getLink(GalleryResource.REL_SELF).getHref();
		}
	}


	//***************************************
	// onClickListener methods
	//***************************************

	public void onClick(View view) {
		if (view.getId() == R.id.button_save) {
			new AddToGalleryTask().execute(this.itemGalleryUrl, this.galleryUrl);
		}
	}


	//***************************************
	// Listener Interface
	//***************************************

	public interface PhotoAddToGalleryFragmentListener {

		public List<GalleryResource> getGalleryList();

		public GalleryResource getGalleryByPosition(int position);

		public void onPhotoAddToGalleryComplete();

	}


	//***************************************
	// Private classes
	//***************************************

	private class AddToGalleryTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				final String url = params[0];
				final String postData = params[1];

				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setContentType(new MediaType("text", "uri-list"));
				HttpEntity<String> requestEntity = new HttpEntity<String>(postData, requestHeaders);
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
				return true;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (photoAddToGalleryFragmentListener != null) {
				photoAddToGalleryFragmentListener.onPhotoAddToGalleryComplete();
			}
		}

	}

}
