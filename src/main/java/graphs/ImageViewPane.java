package graphs;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */


/**
 * File: ImageViewPane.java
 * ---------------------------
 * Defines a container for an ImageView that ensures
 * the given ImageView is resizable, maintains aspect ratio,
 * and is centered in the container.
 */
public class ImageViewPane extends Region {

	private ObjectProperty<ImageView> imageViewProperty = new SimpleObjectProperty<ImageView>();

	public ObjectProperty<ImageView> imageViewProperty() {
		return imageViewProperty;
	}

	public ImageView getImageView() {
		return imageViewProperty.get();
	}

	public void setImageView(ImageView imageView) {
		this.imageViewProperty.set(imageView);
	}

	public ImageViewPane() {
		this(new ImageView());
	}

	@Override
	protected void layoutChildren() {
		ImageView imageView = imageViewProperty.get();
		if (imageView != null) {
	        imageView.setFitWidth(getWidth());
	        // heuristic to ensure aspect ratio is mostly maintained
	        if(getHeight() <= getWidth()) {
	        	imageView.setFitHeight(getHeight());
	        }
			layoutInArea(imageView, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
		}
		super.layoutChildren();
	}

	public ImageViewPane(ImageView imageView) {
		imageViewProperty.addListener(new ChangeListener<ImageView>() {

			@Override
			public void changed(ObservableValue<? extends ImageView> arg0, ImageView oldIV, ImageView newIV) {
				if (oldIV != null) {
					getChildren().remove(oldIV);
				}
				if (newIV != null) {
					getChildren().add(newIV);
				}
			}
		});
		this.imageViewProperty.set(imageView);
	}
}

