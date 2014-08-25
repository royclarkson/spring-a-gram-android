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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.royclarkson.springagram.model.ItemResource;

/**
 * @author Roy Clarkson
 */
public class PhotoDetailFragment extends Fragment {

	private static final String ARG_PHOTO_POSITION = "photo_position";

	private int photoPosition;

	private PhotoDetailFragmentListener photoDetailFragmentListener;


	public PhotoDetailFragment() {
		// Required empty public constructor
	}

	public static PhotoDetailFragment newInstance(int photoPosition) {
		PhotoDetailFragment fragment = new PhotoDetailFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_PHOTO_POSITION, photoPosition);
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
			this.photoPosition = getArguments().getInt(ARG_PHOTO_POSITION);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_detail, container, false);
		ImageView imageView = (ImageView) view.findViewById(R.id.photo_detail_image);
		ItemResource photo = this.photoDetailFragmentListener.getPhotoByPosition(this.photoPosition);
		imageView.setImageBitmap(photo.getImage());
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.photoDetailFragmentListener = (PhotoDetailFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.photoDetailFragmentListener = null;
	}


	//***************************************
	// UI events
	//***************************************

	// TODO: Rename method, update argument and hook method into UI event
//	public void onButtonPressed(Uri uri) {
//		if (this.photoDetailFragmentListener != null) {
//			this.photoDetailFragmentListener.onFragmentInteraction(uri);
//		}
//	}


	//***************************************
	// Listener interface
	//***************************************

	public interface PhotoDetailFragmentListener {

		public ItemResource getPhotoByPosition(int position);

	}

}
