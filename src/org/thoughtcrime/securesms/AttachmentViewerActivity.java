package org.thoughtcrime.securesms;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Wei.He on 9/9/14.
 */
public class AttachmentViewerActivity extends Activity {
    private static  final String TAG = "ImageViewerActivity";

    private MediaPlayer mp = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView imageView = new ImageView(this);
        setContentView(imageView);
        Log.d(TAG, "uri: " + getIntent().getData());
        imageView.setImageURI(getIntent().getData());
        PhotoViewAttacher mAttacher = new PhotoViewAttacher(imageView);
    }
}
