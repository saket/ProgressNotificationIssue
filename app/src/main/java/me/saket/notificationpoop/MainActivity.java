package me.saket.notificationpoop;

import android.app.Notification;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
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

      progressDisposable = streamProgress()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
              progress -> {
                Log.i(TAG, "Progress: " + progress);
                updateProgressNotification(progress, 123);
              },
              error -> error.printStackTrace()
          );
    });
  }

  private Observable<Integer> streamProgress() {
    return Observable.range(0, 101)
        .zipWith(Observable.interval(25, TimeUnit.MILLISECONDS), (progress, delay) -> progress);
  }

  private void updateProgressNotification(int progress, int notificationId) {
    Notification notification = new NotificationCompat.Builder(this)
        .setContentTitle("Saving image")
        .setContentText(progress + "%")
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setWhen(0)
        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
        .setProgress(100 /* max */, progress, false /* indeterminateProgress */)
        .build();

    NotificationManagerCompat.from(this).notify(notificationId, notification);
  }
}
