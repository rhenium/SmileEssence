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

package net.lacolaco.smileessence;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.CommandOpenUserDetail;
import net.lacolaco.smileessence.data.PostState;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.twitter.task.ShowStatusTask;
import net.lacolaco.smileessence.util.UIHandler;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.dialog.StatusDetailDialogFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntentRouter {

    // ------------------------------ FIELDS ------------------------------

    private static final String TWITTER_HOST = "twitter.com";
    private static final Pattern TWITTER_STATUS_PATTERN = Pattern.compile("\\A(?:/#!)?/(?:\\w{1,15})/status(?:es)?/(\\d+)\\z", Pattern.CASE_INSENSITIVE);
    private static final Pattern TWITTER_USER_PATTERN = Pattern.compile("\\A(?:/#!)?/(\\w{1,15})/?\\z", Pattern.CASE_INSENSITIVE);
    private static final Pattern TWITTER_POST_PATTERN = Pattern.compile("\\A/(intent/tweet|share)\\z", Pattern.CASE_INSENSITIVE);

    // -------------------------- STATIC METHODS --------------------------

    public static void onNewIntent(MainActivity activity, Intent intent) {
        Logger.debug("onNewIntent");
        Uri uri = intent.getData();
        if (uri != null) {
            onUriIntent(activity, uri);
        } else if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case Intent.ACTION_SEND: {
                    if ("text/plain".equals(intent.getType())) {
                        Bundle extra = intent.getExtras();
                        if (extra != null) {
                            String text = getText(extra);
                            openPostPage(activity, text);
                        }
                    } else if (intent.getType().startsWith("image/")) {
                        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                        openPostPageWithImage(activity, imageUri);
                    }
                    break;
                }
            }
        }
    }

    private static void onUriIntent(MainActivity activity, Uri uri) {
        Logger.debug(uri.toString());

        if (uri.getHost().equals(TWITTER_HOST)) {
            Matcher postMatcher = TWITTER_POST_PATTERN.matcher(uri.getPath()); // /share and /intent/tweet: don't accept status parameter
            if (postMatcher.find()) {
                openPostPage(activity, extractText(uri));
                return;
            }
            Matcher statusMatcher = TWITTER_STATUS_PATTERN.matcher(uri.getPath());
            if (statusMatcher.find()) {
                showStatusDialog(activity, Long.getLong(statusMatcher.group(1)));
                return;
            }
            Matcher userMatcher = TWITTER_USER_PATTERN.matcher(uri.getPath());
            if (userMatcher.find()) {
                showUserDialog(activity, statusMatcher.group(1));
            }
        }
    }

    private static String getText(Bundle extra) {
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(extra.getCharSequence(Intent.EXTRA_SUBJECT))) {
            builder.append(extra.getCharSequence(Intent.EXTRA_SUBJECT)).append(" ");
        }
        builder.append(extra.getCharSequence(Intent.EXTRA_TEXT));
        return builder.toString();
    }

    private static String extractText(Uri uri) {
        String result = "";
        String text = uri.getQueryParameter("text");
        String url = uri.getQueryParameter("url");
        String via = uri.getQueryParameter("via");
        String hashtags = uri.getQueryParameter("hashtags");

        if (!TextUtils.isEmpty(text)) result += text;
        if (!TextUtils.isEmpty(url)) result += " " + url;
        if (!TextUtils.isEmpty(hashtags)) result += " " + hashtags.trim().replaceAll(",", " #");
        if (!TextUtils.isEmpty(via)) result += " via @" + via;

        return result;
    }

    private static void showStatusDialog(final MainActivity activity, long id) {
        new ShowStatusTask(Application.getInstance().getCurrentAccount(), id)
                .onDoneUI(tweet -> {
                    StatusDetailDialogFragment fragment = new StatusDetailDialogFragment();
                    fragment.setStatusID(tweet.getId());
                    DialogHelper.showDialog(activity, fragment);
                })
                .onFail(x -> Notificator.getInstance().alert(R.string.error_intent_status_cannot_load));
    }

    private static void showUserDialog(MainActivity activity, String screenName) {
        CommandOpenUserDetail openUserDetail = new CommandOpenUserDetail(activity, screenName);
        openUserDetail.execute();
    }

    private static void openPostPage(final MainActivity activity, final String str) {
        new UIHandler().post(() -> PostState.newState().beginTransaction().setText(str).commitWithOpen(activity));
    }

    private static void openPostPageWithImage(final MainActivity activity, final Uri imageUri) {
        new UIHandler().post(() -> activity.openPostPageWithImage(imageUri));
    }
}
