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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.royclarkson.springagram.model.GalleryResource;
import com.royclarkson.springagram.model.ItemResource;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Fragment} that displays a list of {@link ItemResource}s assigned to the {@link GalleryResource}
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
		fetchGalleryPhotoList();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo, container, false);
		this.listView = (AbsListView) view.findViewById(android.R.id.list);
		this.listView.setOnItemClickListener(this);
		registerForContextMenu(this.listView);
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = new MenuInflater(this.getActivity());
		inflater.inflate(R.menu.gallery_photo_list_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.action_remove_from_gallery:
				removeFromGallery(info.position);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
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

		public void onDownloadGalleryPhotosComplete(List<ItemResource> photos);

		public ItemResource getGalleryPhotoByPosition(int position);

		public void onRemovePhotoFromGalleryByPosition(int position);

	}


	//***************************************
	// Helper methods
	//***************************************

	public void fetchGalleryPhotoList() {
		new DownloadGalleryPhotosTask().execute(this.galleryPhotosUrl);
	}

	private void refreshPhotoList(Resources resources) {
		List<ItemResource> photos = new ArrayList<ItemResource>(resources.getContent());
		if (null != this.galleryPhotoListFragmentListener) {
			this.galleryPhotoListFragmentListener.onDownloadGalleryPhotosComplete(photos);
		}
		listAdapter = new PhotoListAdapter(getActivity(), photos);
		listView.setAdapter(listAdapter);
	}

	private void removeFromGallery(int position) {
		if (null != this.galleryPhotoListFragmentListener) {
			ItemResource itemResource = this.galleryPhotoListFragmentListener.getGalleryPhotoByPosition(position);
			new RemoveGalleryPhotoTask().execute(itemResource.getLink(ItemResource.REL_GALLERY).getHref());
			this.galleryPhotoListFragmentListener.onRemovePhotoFromGalleryByPosition(position);
			((PhotoListAdapter) listAdapter).notifyDataSetChanged();
		}
	}


	//***************************************
	// Private classes
	//***************************************

	private class DownloadGalleryPhotosTask extends AsyncTask<String, Void, Resources> {

		@Override
		protected Resources doInBackground(String... params) {
			try {
				final String url = params[0];
				RestTemplate restTemplate = RestUtils.getInstance();
				ResponseEntity<Resources<ItemResource>> responseEntity = restTemplate.exchange(url, HttpMethod.GET,
						RestUtils.getRequestEntity(),
						new ParameterizedTypeReference<Resources<ItemResource>>() {
						});
				return responseEntity.getBody();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Resources resources) {
			refreshPhotoList(resources);
		}

	}

	private class RemoveGalleryPhotoTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				final String url = params[0];
				RestTemplate restTemplate = RestUtils.getInstance();
				restTemplate.delete(url);
				return true;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {

		}

	}
}
