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

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.royclarkson.springagram.model.GalleryResource;
import com.royclarkson.springagram.model.PhotoResource;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;


public class MainActivity extends Activity
		implements NavigationDrawerFragment.NavigationDrawerCallbacks,
		HomeFragment.HomeFragmentListener,
		PhotoListFragment.PhotoListFragmentListener,
		PhotoDetailFragment.PhotoDetailFragmentListener,
		GalleryListFragment.GalleryListFragmentListener,
		GalleryAddFragment.GalleryAddFragmentListener,
		GalleryPhotoListFragment.GalleryPhotoListFragmentListener {

	private static final String TAG_FRAGMENT_HOME = "fragment_home";

	private static final String REL_ITEMS = "items";

	private static final String REL_GALLERIES = "galleries";

	private NavigationDrawerFragment navigationDrawerFragment;

	private ResourceSupport rootResource;

	private List<PhotoResource> photos;

	private List<GalleryResource> galleries;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence title;

	private int menuPosition = 0;


	//***************************************
	// Activity methods
	//***************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		navigationDrawerFragment = (NavigationDrawerFragment)
				getFragmentManager().findFragmentById(R.id.navigation_drawer);
		title = getTitle();

		// Set up the drawer.
		navigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!navigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			switch (this.menuPosition) {
				case 1:
					getMenuInflater().inflate(R.menu.photos_list_main, menu);
					break;
				case 2:
					getMenuInflater().inflate(R.menu.gallery_list_main, menu);
					break;
				case 0:
				default:
					getMenuInflater().inflate(R.menu.main, menu);
					break;
			}
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(title);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_add_photo) {
			Toast.makeText(this, "Add Photo", Toast.LENGTH_SHORT).show();
			return true;
		} else if (id == R.id.action_add_gallery) {
			showGalleryAddFragment();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showGalleryAddFragment() {
		FragmentManager fragmentManager = getFragmentManager();
		String url = this.rootResource.getLink(REL_GALLERIES).getHref();
		GalleryAddFragment galleryAddFragment = GalleryAddFragment.newInstance(url);
		FragmentTransaction transaction = fragmentManager.beginTransaction()
				.add(R.id.container, galleryAddFragment)
				.addToBackStack(null);
		transaction.commit();
	}


	//***************************************
	// NavigationDrawerCallbacks methods
	//***************************************

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		this.menuPosition = position;
		String url;
		Fragment fragment = null;
		String tag = null;
		switch (position) {
			case 0:
				url = getString(R.string.base_uri);
				fragment = HomeFragment.newInstance(url);
				tag = TAG_FRAGMENT_HOME;
				break;
			case 1:
				url = this.rootResource.getLink(REL_ITEMS).getHref();
				fragment = PhotoListFragment.newInstance(url);
				tag = PhotoListFragment.TAG;
				break;
			case 2:
				url = this.rootResource.getLink(REL_GALLERIES).getHref();
				fragment = GalleryListFragment.newInstance(url);
				tag = GalleryListFragment.TAG;
				break;
		}
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, fragment, tag)
				.commit();
	}

	public void onSectionAttached(int number) {
		switch (number) {
			case 1:
				title = getString(R.string.title_section_home);
				break;
			case 2:
				title = getString(R.string.title_section_photos);
				break;
			case 3:
				title = getString(R.string.title_section_galleries);
				break;
		}
	}


	//***************************************
	// HomeFragmentListener methods
	//***************************************

	@Override
	public void onResourceDownloadComplete(ResourceSupport rootResource) {
		this.rootResource = rootResource;
	}


	//***************************************
	// PhotoListFragmentListener methods
	//***************************************

	@Override
	public void onDownloadPhotosComplete(List<PhotoResource> photos) {
		this.photos = photos;
	}

	@Override
	public void onPhotoSelected(int position) {
		PhotoDetailFragment photoDetailFragment = PhotoDetailFragment.newInstance(position);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction()
				.add(R.id.container, photoDetailFragment)
				.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public PhotoResource getPhotoByPosition(int position) {
		return this.photos.get(position);
	}

	@Override
	public void deletePhotoByPosition(int position) {
		this.photos.remove(position);
	}


	//***************************************
	// PhotoDetailFragmentListener methods
	//***************************************

//	@Override
//	public PhotoResource getPhotoByPosition(int position) {
//		return this.photos.get(position);
//	}


	//***************************************
	// GalleryListFragmentListener methods
	//***************************************

	@Override
	public void onDownloadGalleriesComplete(List<GalleryResource> galleries) {
		this.galleries = galleries;
	}

	@Override
	public void onGallerySelected(int position) {
		GalleryResource gallery = this.galleries.get(position);
		String url = gallery.getLink(REL_ITEMS).getHref();
		GalleryPhotoListFragment galleryPhotoListFragment = GalleryPhotoListFragment.newInstance(url);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction()
				.add(R.id.container, galleryPhotoListFragment)
				.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public GalleryResource getGalleryByPosition(int position) {
		return this.galleries.get(position);
	}

	@Override
	public void deleteGalleryByPosition(int position) {
		this.galleries.remove(position);
	}


	//***************************************
	// GalleryAddFragmentListener methods
	//***************************************

	public void onGalleryAddComplete() {
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.popBackStack();
		GalleryListFragment galleryListFragment =
				(GalleryListFragment) fragmentManager.findFragmentByTag(GalleryListFragment.TAG);
		galleryListFragment.fetchGalleryList();
	}


	//***************************************
	// GalleryPhotoListFragmentListener methods
	//***************************************

	@Override
	public void onDownloadGalleryPhotosComplete(List<PhotoResource> photos) {
		this.photos = photos;
	}

	@Override
	public void onGalleryPhotoSelected(int position) {
		PhotoDetailFragment photoDetailFragment = PhotoDetailFragment.newInstance(position);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction()
				.add(R.id.container, photoDetailFragment)
				.addToBackStack(null);
		transaction.commit();
	}



	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section
		 * number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(
					getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}

}
