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
import android.widget.ListAdapter;
import android.widget.TextView;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Fragment} that displays a list of {@link PhotoResource}s
 *
 * @author Roy Clarkson
 */
public class GalleryPhotoListFragment extends Fragment implements AbsListView.OnItemClickListener {

	private static final String TAG = GalleryPhotoListFragment.class.getSimpleName();

	private static final String ARG_GALLERY_PHOTOS_URI = "gallery_photos_uri";

	private String galleryPhotosUrl;

	private GalleryPhotoListFragmentListener galleryPhotoListFragmentListener;

	private AbsListView listView;

	private ListAdapter listAdapter;


	public GalleryPhotoListFragment() {
		// Required empty public constructor
	}

	public static GalleryPhotoListFragment newInstance(String galleryPhotosUrl) {
		GalleryPhotoListFragment fragment = new GalleryPhotoListFragment();
		Bundle args = new Bundle();
		args.putString(ARG_GALLERY_PHOTOS_URI, galleryPhotosUrl);
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
			this.galleryPhotosUrl = getArguments().getString(ARG_GALLERY_PHOTOS_URI);
		}

		new DownloadGalleryPhotosTask().execute(this.galleryPhotosUrl);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo, container, false);
		listView = (AbsListView) view.findViewById(android.R.id.list);
		listView.setOnItemClickListener(this);
		return view;
	}

	/**
	 * The default content for this Fragment has a TextView that is shown when
	 * the list is empty. If you would like to change the text, call this method
	 * to supply the text it should use.
	 */
	public void setEmptyText(CharSequence emptyText) {
		View emptyView = listView.getEmptyView();

		if (emptyText instanceof TextView) {
			((TextView) emptyView).setText(emptyText);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.galleryPhotoListFragmentListener = (GalleryPhotoListFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnPhotoSelectedListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.galleryPhotoListFragmentListener = null;
	}


	//***************************************
	// OnItemClickListener methods
	//***************************************

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (null != this.galleryPhotoListFragmentListener) {
			this.galleryPhotoListFragmentListener.onGalleryPhotoSelected(position);
		}
	}


	//***************************************
	// Listener interface
	//***************************************

	public interface GalleryPhotoListFragmentListener {

		public void onGalleryPhotoSelected(int position);

		public void onDownloadGalleryPhotosComplete(List<PhotoResource> photos);

	}


	private void refreshPhotos(Resources resources) {
		List<PhotoResource> photos = new ArrayList<PhotoResource>(resources.getContent());
		if (null != this.galleryPhotoListFragmentListener) {
			this.galleryPhotoListFragmentListener.onDownloadGalleryPhotosComplete(photos);
		}
		listAdapter = new PhotoListAdapter(getActivity(), photos);
		listView.setAdapter(listAdapter);
	}


	private class DownloadGalleryPhotosTask extends AsyncTask<String, Void, Resources> {

		@Override
		protected Resources doInBackground(String... params) {
			try {
				final String url = params[0];
				RestTemplate restTemplate = RestUtils.getInstance();
				ResponseEntity<Resources<PhotoResource>> responseEntity = restTemplate.exchange(url, HttpMethod.GET,
						RestUtils.getRequestEntity(),
						new ParameterizedTypeReference<Resources<PhotoResource>>() {
						});
				return responseEntity.getBody();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Resources resources) {
			refreshPhotos(resources);
		}

	}

}
