/**
 * Copyright (C) 2011 Ray Message
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.mms;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.BitmapDecodingException;

import java.io.IOException;

public class AttachmentManager {
  private final static String TAG = AttachmentManager.class.getSimpleName();

  private final Context context;
  private final View attachmentView;
  private final ImageView thumbnail;
  private final Button removeButton;
  private final SlideDeck slideDeck;
  private final AttachmentListener attachmentListener;

  public AttachmentManager(Activity view, AttachmentListener listener) {
    this.attachmentView     = (View)view.findViewById(R.id.attachment_editor);
    this.thumbnail          = (ImageView)view.findViewById(R.id.attachment_thumbnail);
    this.removeButton       = (Button)view.findViewById(R.id.remove_image_button);
    this.slideDeck          = new SlideDeck();
    this.context            = view;
    this.attachmentListener = listener;

    this.removeButton.setOnClickListener(new RemoveButtonListener());
  }

  public void clear() {
    slideDeck.clear();
    attachmentView.setVisibility(View.GONE);
    attachmentListener.onAttachmentChanged();
  }

  public void setImage(Uri image) throws IOException, BitmapDecodingException {
    ImageSlide slide = new ImageSlide(context, image);
    slideDeck.addSlide(slide);
    thumbnail.setImageDrawable(slide.getThumbnail(345, 261));
    attachmentView.setVisibility(View.VISIBLE);
    attachmentListener.onAttachmentChanged();
  }

  public void setVideo(Uri video) throws IOException, MediaTooLargeException {
    VideoSlide slide = new VideoSlide(context, video);
    slideDeck.addSlide(slide);
    thumbnail.setImageDrawable(slide.getThumbnail(thumbnail.getWidth(), thumbnail.getHeight()));
    attachmentView.setVisibility(View.VISIBLE);
    attachmentListener.onAttachmentChanged();
  }

  public void setAudio(Uri audio)throws IOException, MediaTooLargeException {
    AudioSlide slide = new AudioSlide(context, audio);
    slideDeck.addSlide(slide);
    thumbnail.setImageDrawable(slide.getThumbnail(thumbnail.getWidth(), thumbnail.getHeight()));
    attachmentView.setVisibility(View.VISIBLE);
    attachmentListener.onAttachmentChanged();
  }

  public boolean isAttachmentPresent() {
    return attachmentView.getVisibility() == View.VISIBLE;
  }

  public SlideDeck getSlideDeck() {
    return slideDeck;
  }

  public static void selectVideo(Activity activity, int requestCode) {
    selectMediaType(activity, "video/*", requestCode);
  }

  public static void selectImage(Activity activity, int requestCode) {
    selectMediaType(activity, "image/*", requestCode);
  }

  public static void selectAudio(Activity activity, int requestCode) {
    selectMediaType(activity, "audio/*", requestCode);
  }

  private static void selectMediaType(Activity activity, String type, int requestCode) {
    final Intent intent = new Intent();
    intent.setType(type);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
      try {
        activity.startActivityForResult(intent, requestCode);
        return;
      } catch (ActivityNotFoundException anfe) {
        Log.w(TAG, "couldn't complete ACTION_OPEN_DOCUMENT, no activity found. falling back.");
      }
    }

    intent.setAction(Intent.ACTION_GET_CONTENT);
    try {
      activity.startActivityForResult(intent, requestCode);
    } catch (ActivityNotFoundException anfe) {
      Log.w(TAG, "couldn't complete ACTION_GET_CONTENT intent, no activity found. falling back.");
      Toast.makeText(activity, R.string.AttachmentManager_cant_open_media_selection, Toast.LENGTH_LONG).show();
    }
  }

  private class RemoveButtonListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      clear();
    }
  }

  public interface AttachmentListener {
    public void onAttachmentChanged();
  }
}
