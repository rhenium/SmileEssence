/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2012-2014 lacolaco.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.lacolaco.smileessence.twitter.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.notification.NotificationType;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.util.BackgroundTask;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TweetTask extends BackgroundTask<Tweet, Void> {

    // ------------------------------ FIELDS ------------------------------

    private static final int MEDIA_SIZE_LIMIT = 2 * 1024 * 1024;
    private final Account account;
    private final StatusUpdate update;
    private final String mediaPath;
    private String tempFilePath;
    private boolean resizeFlag;

    // --------------------------- CONSTRUCTORS ---------------------------

    public TweetTask(Account account, StatusUpdate update) {
        this(account, update, null, false);
    }

    public TweetTask(Account account, StatusUpdate update, String mediaPath, boolean resize) {
        this.account = account;
        this.update = update;
        this.mediaPath = mediaPath;
        resizeFlag = resize;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public File getMediaFile() {
        File file = new File(mediaPath);
        if (file.length() >= MEDIA_SIZE_LIMIT && resizeFlag) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true; //decoder is not return bitmap but set option
            BitmapFactory.decodeFile(mediaPath, opt);
            tempFilePath = Environment.getExternalStorageDirectory() + "/temp.jpg";
            File compressedFile = new File(tempFilePath);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(compressedFile);
                float ratio = (float) file.length() / (float) MEDIA_SIZE_LIMIT;
                BitmapFactory.Options resizeOpt = new BitmapFactory.Options();
                resizeOpt.inPurgeable = true;
                resizeOpt.inSampleSize = (int) Math.ceil(ratio);
                Bitmap bitmap = BitmapFactory.decodeFile(mediaPath, resizeOpt);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                bitmap.recycle();
                return compressedFile;
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error(e);
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.error(e);
                }
            }
        }
        return file;
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected Tweet doInBackground(Void... params) {
        try {
            if (!TextUtils.isEmpty(mediaPath)) {
                File mediaFile = getMediaFile();
                if (mediaFile.exists()) {
                    update.setMedia(mediaFile);
                }
            }
            Tweet tweet = Tweet.fromTwitter(account.getTwitter().tweets().updateStatus(update));
            if (tempFilePath != null) {
                new File(tempFilePath).delete();
            }
            if (tweet != null) {
                Notificator.getInstance().publish(R.string.notice_tweet_succeeded);
            } else {
                Notificator.getInstance().publish(R.string.notice_tweet_failed, NotificationType.ALERT);
            }
            return tweet;
        } catch (TwitterException e) {
            e.printStackTrace();
            Logger.error(e.toString());
            return null;
        }
    }
}
