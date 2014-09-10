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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.royclarkson.springagram.model.ApiResource;
import com.royclarkson.springagram.model.GalleryResource;
import com.royclarkson.springagram.model.ItemResource;

import java.util.List;


/**
 * @author Roy Clarkson
 */
public class MainActivity extends Activity
		implements NavigationDrawerFragment.NavigationDrawerCallbacks,
		HomeFragment.HomeFragmentListener,
		PhotoListFragment.PhotoListFragmentListener,
		PhotoDetailFragment.PhotoDetailFragmentListener,
		PhotoAddFragment.PhotoAddFragmentListener,
		PhotoAddToGalleryFragment.PhotoAddToGalleryFragmentListener,
		GalleryListFragment.GalleryListFragmentListener,
		GalleryAddFragment.GalleryAddFragmentListener,
		GalleryPhotoListFragment.GalleryPhotoListFragmentListener {

	private NavigationDrawerFragment navigationDrawerFragment;

	private ApiResource apiResource;

	private List<ItemResource> photos;

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
			showPhotoAddFragment();
			return true;
		} else if (id == R.id.action_refresh_photo_list) {
			FragmentManager fragmentManager = getFragmentManager();
			PhotoListFragment galleryListFragment =
					(PhotoListFragment) fragmentManager.findFragmentByTag(PhotoListFragment.TAG);
			galleryListFragment.fetchPhotoList();
			return true;
		} else if (id == R.id.action_add_gallery) {
			showGalleryAddFragment();
			return true;
		} else if (id == R.id.action_refresh_gallery_list) {
			FragmentManager fragmentManager = getFragmentManager();
			GalleryListFragment galleryListFragment =
					(GalleryListFragment) fragmentManager.findFragmentByTag(GalleryListFragment.TAG);
			galleryListFragment.fetchGalleryList();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showPhotoAddFragment() {
		FragmentManager fragmentManager = getFragmentManager();
		String url = this.apiResource.getLink(ApiResource.REL_ITEMS).getHref();
		PhotoAddFragment photoAddFragment = PhotoAddFragment.newInstance(url);
		FragmentTransaction transaction = fragmentManager.beginTransaction()
				.add(R.id.container, photoAddFragment)
				.addToBackStack(null);
		transaction.commit();
	}

	private void showGalleryAddFragment() {
		FragmentManager fragmentManager = getFragmentManager();
		String url = this.apiResource.getLink(ApiResource.REL_GALLERIES).getHref();
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
		String url = null;
		Fragment fragment = null;
		String tag = null;
		switch (position) {
			case 0:
				url = getString(R.string.base_uri);
				fragment = HomeFragment.newInstance(url);
				tag = HomeFragment.TAG;
				break;
			case 1:
				if (this.apiResource != null) {
					url = this.apiResource.getLink(ApiResource.REL_ITEMS).getHref();
				}
				fragment = PhotoListFragment.newInstance(url);
				tag = PhotoListFragment.TAG;
				break;
			case 2:
				if (this.apiResource != null) {
					url = this.apiResource.getLink(ApiResource.REL_GALLERIES).getHref();
				}
				fragment = GalleryListFragment.newInstance(url);
				tag = GalleryListFragment.TAG;
				break;
		}
		// update the main content by replacing fragments
		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.popBackStack();
			fragmentManager.beginTransaction()
					.replace(R.id.container, fragment, tag)
					.commit();
		}
	}



	//***************************************
	// HomeFragmentListener methods
	//***************************************

	@Override
	public void onResourceDownloadComplete(ApiResource apiResource) {
		this.apiResource = apiResource;
	}

	public void onNetworkError(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}


	//***************************************
	// PhotoListFragmentListener methods
	//***************************************

	@Override
	public void onDownloadPhotosComplete(List<ItemResource> photos) {
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
	public ItemResource getPhotoByPosition(int position) {
		return this.photos.get(position);
	}

	@Override
	public void onDeletePhotoByPosition(int position) {
		this.photos.remove(position);
	}

	@Override
	public void onPhotoAddToGallerySelected(int position) {
		ItemResource item = this.photos.get(position);
		String itemGalleryUrl = item.getLink(ItemResource.REL_GALLERY).getHref();
		PhotoAddToGalleryFragment photoAddToGalleryFragment = PhotoAddToGalleryFragment.newInstance(itemGalleryUrl);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction()
				.add(R.id.container, photoAddToGalleryFragment)
				.addToBackStack(null);
		transaction.commit();
	}


	//***************************************
	// PhotoDetailFragmentListener methods
	//***************************************

//	@Override
//	public PhotoResource getPhotoByPosition(int position) {
//		return this.photos.get(position);
//	}


	//***************************************
	// PhotoDetailFragmentListener methods
	//***************************************

	@Override
	public void onPhotoAddComplete() {
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.popBackStack();
		PhotoListFragment photoListFragment =
				(PhotoListFragment) fragmentManager.findFragmentByTag(PhotoListFragment.TAG);
		photoListFragment.fetchPhotoList();
	}


	//***************************************
	// PhotoAddToGalleryFragmentListener methods
	//***************************************

	@Override
	public List<GalleryResource> getGalleryList() {
		return this.galleries;
	}

	@Override
	public void onPhotoAddToGalleryComplete() {
		getFragmentManager().popBackStack();
	}


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
		String url = gallery.getLink(GalleryResource.REL_ITEMS).getHref();
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
	public void onDeleteGalleryByPosition(int position) {
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
	public void onDownloadGalleryPhotosComplete(List<ItemResource> photos) {
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

	@Override
	public ItemResource getGalleryPhotoByPosition(int position) {
		return this.photos.get(position);
	}

	@Override
	public void onRemovePhotoFromGalleryByPosition(int position) {
		this.photos.remove(position);
	}

}
