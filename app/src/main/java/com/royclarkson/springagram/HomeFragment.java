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

import com.royclarkson.springagram.model.ApiResource;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author Roy Clarkson
 */
public class HomeFragment extends Fragment {

	public static final String TAG = HomeFragment.class.getSimpleName();

	private static final String ARG_API_URL = "api_url";

	private String apiUrl;

	private HomeFragmentListener homeFragmentListener;


	public HomeFragment() {
		// Required empty public constructor
	}

	public static HomeFragment newInstance(String apiUrl) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();
		args.putString(ARG_API_URL, apiUrl);
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
			this.apiUrl = getArguments().getString(ARG_API_URL);
		}
		new DownloadRootResourceTask().execute(this.apiUrl);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			homeFragmentListener = (HomeFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		homeFragmentListener = null;
	}


	//***************************************
	// Listener interface
	//***************************************

	public interface HomeFragmentListener {

		public void onResourceDownloadComplete(ApiResource resource);

	}


	// ***************************************
	// Private classes
	// ***************************************

	private class DownloadRootResourceTask extends AsyncTask<String, Void, ApiResource> {

		@Override
		protected ApiResource doInBackground(String... params) {
			try {
				final String url = params[0];
				RestTemplate restTemplate = RestUtils.getInstance();
				ResponseEntity<ApiResource> responseEntity = restTemplate.exchange(url, HttpMethod.GET,
						RestUtils.getRequestEntity(), ApiResource.class);
				return responseEntity.getBody();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(ApiResource apiResource) {
			if (homeFragmentListener != null) {
				homeFragmentListener.onResourceDownloadComplete(apiResource);
			}
		}

	}

}
