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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


/**
 * @author Roy Clarkson
 */
public class GalleryListAdapter extends BaseAdapter {

	private final List<GalleryResource> galleries;

	private final LayoutInflater layoutInflater;

	public GalleryListAdapter(Context context, List<GalleryResource> galleries) {
		this.layoutInflater = LayoutInflater.from(context);
		this.galleries = galleries;
	}

	@Override
	public int getCount() {
		return this.galleries.size();
	}

	@Override
	public GalleryResource getItem(int position) {
		return this.galleries.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		GalleryResource gallery = getItem(position);
		View view = convertView;

		if (view == null) {
			view = this.layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		}

		TextView nameTextView = (TextView) view.findViewById(android.R.id.text1);
		nameTextView.setText(gallery.getDescription());

		return view;
	}

}
