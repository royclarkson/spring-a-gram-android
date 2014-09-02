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

package com.royclarkson.springagram.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Base64;

import org.springframework.hateoas.ResourceSupport;

/**
 * @author Roy Clarkson
 */
public class ItemResource extends ResourceSupport {

	public static final String REL_SELF = "self";

	public static final String REL_GALLERY = "gallery";

	private static final int THUMBNAIL_HEIGHT = 100;

	private static final int THUMBNAIL_WIDTH = 100;

	private static final int IMAGE_HEIGHT = 400;

	private static final int IMAGE_WIDTH = 400;

	private String name;

	private Bitmap image;

	private Bitmap thumbnail;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setImage(String imageDataUri) {
		String imageDataString = imageDataUri.substring(imageDataUri.indexOf(",")+1);
		byte[] imageData = Base64.decode(imageDataString, Base64.DEFAULT);
		Bitmap original = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		this.image = ThumbnailUtils.extractThumbnail(original, IMAGE_WIDTH, IMAGE_HEIGHT);
		this.thumbnail = ThumbnailUtils.extractThumbnail(original, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
	}

	public Bitmap getImage() {
		return this.image;
	}

	public Bitmap getThumbnail() {
		return this.thumbnail;
	}

}
