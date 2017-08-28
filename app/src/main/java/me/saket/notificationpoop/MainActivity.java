package me.saket.notificationpoop;

import android.app.Notification;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private Disposable progressDisposable = Disposables.disposed();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.button).setOnClickListener(o -> {
      progressDisposable.dispose();
      NotificationManagerCompat.from(this).cancelAll();

      long downloadStartTime = System.currentTimeMillis();
      int notificationId = 123;

      progressDisposable = streamProgress()
          //.sample(200, TimeUnit.MILLISECONDS, true /* emitLast */)
          .subscribe(
              progress -> {
                Log.i(TAG, "Progress: " + progress);
                updateProgressNotification(progress, notificationId, downloadStartTime);
              },
              error -> error.printStackTrace()
          );
    });
  }

  private Observable<Integer> streamProgress() {
    return Observable.range(0, 101)
        // Add a delay to every emission. Observable#delay() cannot be used here.
        .zipWith(Observable.interval(25, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()), (progress, delay) -> progress);
  }

  private void updateProgressNotification(int progress, int notificationId, long downloadStartTime) {
    Notification notification = new NotificationCompat.Builder(this)
        .setContentTitle("Saving image")
        .setContentText(progress + "%")
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setWhen(downloadStartTime)
        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
        .setProgress(100 /* max */, progress, false /* indeterminateProgress */)
        .build();
    NotificationManagerCompat.from(this).notify(notificationId, notification);
  }
}
