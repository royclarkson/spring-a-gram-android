/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import android.widget.ImageView;
import android.widget.TextView;

import com.royclarkson.springagram.model.PhotoResource;

import java.util.List;


/**
 * @author Roy Clarkson
 */
public class PhotoListAdapter extends BaseAdapter {

	private final List<PhotoResource> photos;

	private final LayoutInflater layoutInflater;

	public PhotoListAdapter(Context context, List<PhotoResource> photos) {
		this.layoutInflater = LayoutInflater.from(context);
		this.photos = photos;
	}

	@Override
	public int getCount() {
		return this.photos.size();
	}

	@Override
	public PhotoResource getItem(int position) {
		return this.photos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		PhotoResource photo = getItem(position);
		View view = convertView;

		if (view == null) {
			view = this.layoutInflater.inflate(R.layout.photo_list_item, parent, false);
		}

		ImageView thumbnailImageView = (ImageView) view.findViewById(R.id.photo_list_item_thumbnail);
		thumbnailImageView.setImageBitmap(photo.getThumbnail());

		TextView nameTextView = (TextView) view.findViewById(R.id.photo_list_item_name);
		nameTextView.setText(photo.getName());

		return view;
	}

}
