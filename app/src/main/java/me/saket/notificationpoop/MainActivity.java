package me.saket.notificationpoop;

import android.app.Notification;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;

import io.reactivex.Observable;
import me.saket.notificationpoop.irrelevant.GlideProgressTarget;
import me.saket.notificationpoop.irrelevant.MediaDownloadJob;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.button).setOnClickListener(o -> {
      NotificationManagerCompat.from(this).cancelAll();

      downloadImage("http://i.imgur.com/ScyATef.jpg")
          .subscribe(
              downloadJob -> {
                int progress = downloadJob.downloadProgress();
                Log.i(TAG, progress + ": " + downloadJob.progressState());
                updateProgressNotification(downloadJob, 123);
              },
              error -> error.printStackTrace()
          );
    });
  }

  private Observable<MediaDownloadJob> downloadImage(String mediaUrl) {
    String imageUrl = mediaUrl + "?" + String.valueOf(System.currentTimeMillis());    // Randomizing so that Glide never hits the cache.
    Log.i(TAG, "Image url: " + imageUrl);

    return Observable.create(emitter -> {
      Target<File> fileFutureTarget = new SimpleTarget<File>() {
        @Override
        public void onResourceReady(File downloadedFile, Transition<? super File> transition) {
          Log.i(TAG, "onResourceReady()");
          emitter.onNext(MediaDownloadJob.createDownloaded(mediaUrl, 100, downloadedFile));
//          emitter.onComplete();
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
//          emitter.onNext(MediaDownloadJob.createFailed(mediaUrl));
//          emitter.onComplete();
        }
      };

      GlideProgressTarget<String, File> progressTarget = new GlideProgressTarget<String, File>(fileFutureTarget) {
        @Override
        public float getGranularityPercentage() {
          return 0.1f;
        }

        @Override
        protected void onConnecting() {
//          emitter.onNext(MediaDownloadJob.createConnecting(mediaUrl));
        }

        @Override
        protected void onDownloading(long bytesRead, long expectedLength) {
          int progress = (int) (100 * (float) bytesRead / expectedLength);
          emitter.onNext(MediaDownloadJob.createProgress(mediaUrl, progress));
        }

        @Override
        protected void onDownloaded() {}

        @Override
        protected void onDelivered() {}
      };
      progressTarget.setModel(this, imageUrl);

      Glide.with(this).download(imageUrl).into(progressTarget);
      emitter.setCancellable(() -> Glide.with(this).clear(progressTarget));
    });
  }

  private void updateProgressNotification(MediaDownloadJob mediaDownloadJob, int notificationId) {
    boolean indeterminateProgress = mediaDownloadJob.progressState() == MediaDownloadJob.ProgressState.CONNECTING;

    Notification notification = new NotificationCompat.Builder(this)
        .setContentTitle("Saving image")
        .setContentText(mediaDownloadJob.downloadProgress() + "%")
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setWhen(0)
        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
        .setProgress(100 /* max */, mediaDownloadJob.downloadProgress(), indeterminateProgress)
        .build();

    NotificationManagerCompat.from(this).notify(notificationId, notification);
  }
}
