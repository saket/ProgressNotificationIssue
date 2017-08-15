package me.saket.notificationpoop.irrelevant;

import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.io.File;

@AutoValue
public abstract class MediaDownloadJob {

  public enum ProgressState {
    CONNECTING,
    IN_FLIGHT,
    FAILED,
    DOWNLOADED
  }

  public abstract String mediaUrl();

  public abstract ProgressState progressState();

  @IntRange(from = 0, to = 100)
  public abstract int downloadProgress();

  /**
   * Null until the file is downloaded.
   */
  @Nullable
  public abstract File downloadedFile();

  public static MediaDownloadJob createConnecting(String mediaUrl) {
    return new AutoValue_MediaDownloadJob(mediaUrl, ProgressState.CONNECTING, 0, null);
  }

  public static MediaDownloadJob createProgress(String mediaUrl, @IntRange(from = 0, to = 100) int progress) {
    return new AutoValue_MediaDownloadJob(mediaUrl, ProgressState.IN_FLIGHT, progress, null);
  }

  public static MediaDownloadJob createFailed(String mediaUrl) {
    return new AutoValue_MediaDownloadJob(mediaUrl, ProgressState.FAILED, -1, null);
  }

  public static MediaDownloadJob createDownloaded(String mediaUrl, int progress, File downloadedFile) {
    return new AutoValue_MediaDownloadJob(mediaUrl, ProgressState.DOWNLOADED, progress, downloadedFile);
  }
}
