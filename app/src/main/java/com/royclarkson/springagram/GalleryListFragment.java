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
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.royclarkson.springagram.model.GalleryResource;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ListFragment} that displays a list of {@link GalleryResource}s
 *
 * @author Roy Clarkson
 */
public class GalleryListFragment extends ListFragment {

	public static final String TAG = GalleryListFragment.class.getSimpleName();

	private static final String ARG_GALLERIES_LIST_URL = "galleries_url";

	private String galleriesUrl;

	private GalleryListFragmentListener galleryListFragmentListener;


	public GalleryListFragment() {
		// Required empty public constructor
	}

	public static GalleryListFragment newInstance(String galleriesUrl) {
		GalleryListFragment fragment = new GalleryListFragment();
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
		fetchGalleryList();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		registerForContextMenu(this.getListView());
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
			galleryListFragmentListener = (GalleryListFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnGalleryFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		galleryListFragmentListener = null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = new MenuInflater(this.getActivity());
		inflater.inflate(R.menu.gallery_list_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.action_delete:
				deleteGallery(info.position);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}


	//***************************************
	// ListFragment methods
	//***************************************

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (null != this.galleryListFragmentListener) {
			this.galleryListFragmentListener.onGallerySelected(position);
		}
	}


	//***************************************
	// Private methods
	//***************************************

	public void fetchGalleryList() {
		new DownloadGalleriesTask().execute(this.galleriesUrl);
	}

	private void refreshGalleryList(Resources resources) {
		List<GalleryResource> galleries = new ArrayList<GalleryResource>(resources.getContent());
		if (null != this.galleryListFragmentListener) {
			this.galleryListFragmentListener.onDownloadGalleriesComplete(galleries);
		}
		ListAdapter listAdapter = new GalleryListAdapter(getActivity(), galleries);
		setListAdapter(listAdapter);
	}

	private void deleteGallery(int position) {
		GalleryResource galleryResource = this.galleryListFragmentListener.getGalleryByPosition(position);
		new DeleteGalleryTask().execute(galleryResource.getLink("self").getHref());
		this.galleryListFragmentListener.onDeleteGalleryByPosition(position);
		((GalleryListAdapter) getListAdapter()).notifyDataSetChanged();
	}


	// ***************************************
	// Listener interface
	// ***************************************

	public interface GalleryListFragmentListener {

		public void onDownloadGalleriesComplete(List<GalleryResource> galleries);

		public void onGallerySelected(int position);

		public GalleryResource getGalleryByPosition(int position);

		public void onDeleteGalleryByPosition(int position);

	}


	// ***************************************
	// Private classes
	// ***************************************

	private class DownloadGalleriesTask extends AsyncTask<String, Void, Resources> {

		@Override
		protected Resources doInBackground(String... params) {
			try {
				final String url = params[0];
				RestTemplate restTemplate = RestUtils.getInstance();
				ResponseEntity<Resources<GalleryResource>> responseEntity = restTemplate.exchange(url, HttpMethod.GET,
						RestUtils.getRequestEntity(),
						new ParameterizedTypeReference<Resources<GalleryResource>>() {
						});
				return responseEntity.getBody();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				return null;
			}
		}

		@Override
		protected void onPostExecute(Resources resources) {
			refreshGalleryList(resources);
		}

	}

	private class DeleteGalleryTask extends AsyncTask<String, Void, Boolean> {

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
