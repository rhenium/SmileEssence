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

package net.lacolaco.smileessence.viewmodel;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.data.FavoriteCache;
import net.lacolaco.smileessence.data.ImageCache;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.util.TwitterUtils;
import net.lacolaco.smileessence.util.Morse;
import net.lacolaco.smileessence.util.NameStyles;
import net.lacolaco.smileessence.util.StringUtils;
import net.lacolaco.smileessence.util.Themes;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.dialog.StatusDetailDialogFragment;
import net.lacolaco.smileessence.view.dialog.UserDetailDialogFragment;
import net.lacolaco.smileessence.view.listener.ListItemClickListener;

import twitter4j.*;

import java.util.ArrayList;
import java.util.List;

public class StatusViewModel implements IViewModel {
    private Tweet tweet;
    private boolean isMyStatus;
    private boolean isMention;
    private boolean isRetweetOfMe;

    private ArrayList<AsyncTask> lastTasks = new ArrayList<>(); // internal

    // --------------------------- CONSTRUCTORS ---------------------------

    public StatusViewModel(Tweet tw) {
        tweet = tw;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public Tweet getTweet() {
        return tweet;
    }

    private List<Long> getEmbeddedStatusIDs() {
        ArrayList<Long> list = new ArrayList<>();
        for (URLEntity url : tweet.getUrls()) {
            Uri uri = Uri.parse(url.getExpandedURL());
            if (uri.getHost().equals("twitter.com")) {
                String[] arr = uri.toString().split("/");
                if (arr[arr.length - 2].equals("status")) {
                    list.add(Long.parseLong(arr[arr.length - 1].split("\\?")[0]));
                }
            }
        }
        return list;
    }

    private String getFooterText() {
        StringBuilder builder = new StringBuilder();
        if (tweet.isRetweet()) {
            builder.append("(RT: ").append(tweet.getUser().getScreenName()).append(") ");
        }
        builder.append(StringUtils.dateToString(tweet.getCreatedAt()));
        builder.append(" via ");
        builder.append(Html.fromHtml(tweet.getSource()));
        return builder.toString();
    }

    public boolean isFavorited() {
        if (tweet.isRetweet()) {
            return FavoriteCache.getInstance().get(tweet.getRetweetedTweet().getId());
        }
        return FavoriteCache.getInstance().get(tweet.getId());
    }

    public boolean isMention() {
        if (tweet.isRetweet()) {
            return tweet.getRetweetedTweet() == null;
        }
        return isMention;
    }

    public void setMention(boolean mention) {
        isMention = mention;
    }

    public boolean isMyStatus() {
        if (tweet.isRetweet()) {
            return tweet.getRetweetedTweet() == null;
        }
        return isMyStatus;
    }

    public void setMyStatus(boolean myStatus) {
        isMyStatus = myStatus;
    }

    public boolean isRetweetOfMe() {
        return isRetweetOfMe;
    }

    public void setRetweetOfMe(boolean retweet) {
        this.isRetweetOfMe = retweet;
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface IViewModel ---------------------

    @Override
    public View getView(final Activity activity, final LayoutInflater inflater, View convertedView) {
        boolean extendStatusURL = new UserPreferenceHelper(activity).getValue(R.string.key_setting_extend_status_url, true);
        return getView(activity, inflater, convertedView, extendStatusURL);
    }

    // -------------------------- OTHER METHODS --------------------------

    public View getView(final Activity activity, final LayoutInflater inflater, View convertedView, boolean extendStatusURL) {
        for (AsyncTask task : lastTasks) {
            task.cancel(true);
        }
        lastTasks.clear();

        if (convertedView == null) {
            convertedView = inflater.inflate(R.layout.list_item_status, null);
        }
        UserPreferenceHelper preferenceHelper = new UserPreferenceHelper(activity);
        int textSize = preferenceHelper.getValue(R.string.key_setting_text_size, 10);
        int nameStyle = preferenceHelper.getValue(R.string.key_setting_namestyle, 0);
        int theme = ((MainActivity) activity).getThemeIndex();
        NetworkImageView icon = (NetworkImageView) convertedView.findViewById(R.id.imageview_status_icon);
        ImageCache.getInstance().setImageToView(tweet.getUser().getProfileImageUrl(), icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onIconClick(activity);
            }
        });
        TextView header = (TextView) convertedView.findViewById(R.id.textview_status_header);
        header.setTextSize(textSize);
        int colorHeader = Themes.getStyledColor(activity, theme, R.attr.color_status_text_header, 0);
        int colorMineHeader = Themes.getStyledColor(activity, theme, R.attr.color_status_text_mine, 0);
        header.setTextColor(isMyStatus() ? colorMineHeader : colorHeader);
        header.setText(NameStyles.getNameString(nameStyle, tweet.getUser().getScreenName(), tweet.getUser().getName()));
        TextView content = (TextView) convertedView.findViewById(R.id.textview_status_text);
        content.setTextSize(textSize);
        int colorNormal = Themes.getStyledColor(activity, theme, R.attr.color_status_text_normal, 0);
        content.setTextColor(colorNormal);
        String rawText = tweet.getText();
        if (isReadMorseEnabled((MainActivity) activity) && Morse.isMorse(rawText)) {
            content.setText(String.format("%s\n(%s)", rawText, Morse.morseToJa(rawText)));
        } else {
            content.setText(rawText);
        }
        TextView footer = (TextView) convertedView.findViewById(R.id.textview_status_footer);
        footer.setTextSize(textSize - 2);
        int colorFooter = Themes.getStyledColor(activity, theme, R.attr.color_status_text_footer, 0);
        footer.setTextColor(colorFooter);
        footer.setText(getFooterText());
        ImageView favorited = (ImageView) convertedView.findViewById(R.id.imageview_status_favorited);
        favorited.setVisibility(isFavorited() ? View.VISIBLE : View.GONE);
        if (tweet.isRetweet()) {
            int colorBgRetweet = Themes.getStyledColor(activity, theme, R.attr.color_status_bg_retweet, 0);
            convertedView.setBackgroundColor(colorBgRetweet);
        } else if (isMention()) {
            int colorBgMention = Themes.getStyledColor(activity, theme, R.attr.color_status_bg_mention, 0);
            convertedView.setBackgroundColor(colorBgMention);
        } else {
            int colorBgNormal = Themes.getStyledColor(activity, theme, R.attr.color_status_bg_normal, 0);
            convertedView.setBackgroundColor(colorBgNormal);
        }
        convertedView.setOnClickListener(new ListItemClickListener(activity, new Runnable() {
            @Override
            public void run() {
                onClick(activity);
            }
        }));

        final ViewGroup embeddedStatus = (ViewGroup) convertedView.findViewById(R.id.view_status_embedded_status);
        embeddedStatus.removeAllViews();
        if (extendStatusURL) {
            List<Long> embeddedStatusIDs = getEmbeddedStatusIDs();
            if (embeddedStatusIDs.size() > 0) {
                embeddedStatus.setVisibility(View.VISIBLE);
                final Account account = ((MainActivity) activity).getCurrentAccount();
                for (long id : embeddedStatusIDs) {
                    AsyncTask task = TwitterUtils.tryGetStatus(account, id, new TwitterUtils.StatusCallback() {
                        @Override
                        public void success(Tweet tweet) {
                            StatusViewModel viewModel = new StatusViewModel(tweet);
                            View embeddedHolder = viewModel.getView(activity, inflater, null, false);
                            embeddedStatus.addView(embeddedHolder);
                            embeddedStatus.invalidate();
                        }

                        @Override
                        public void error() {
                        }
                    });
                    lastTasks.add(task);
                }

            }
        } else {
            embeddedStatus.setVisibility(View.GONE);
        }
        return convertedView;
    }

    public boolean isMention(String screenName) {
        if (tweet.getMentions() == null) {
            return false;
        }
        for (UserMentionEntity mention : tweet.getMentions()) {
            if (mention.getScreenName().equals(screenName)) {
                return true;
            }
        }
        return false;
    }

        private boolean isReadMorseEnabled(MainActivity activity) {
        return activity.getUserPreferenceHelper().getValue(R.string.key_setting_read_morse, true);
    }

    private void onClick(Activity activity) {
        StatusDetailDialogFragment fragment = new StatusDetailDialogFragment();
        fragment.setStatusID(tweet.getId());
        DialogHelper.showDialog(activity, fragment);
    }

    private void onIconClick(Activity activity) {
        UserDetailDialogFragment dialogFragment = new UserDetailDialogFragment();
        dialogFragment.setUserID(tweet.getOriginalTweet().getUser().getId());
        DialogHelper.showDialog(activity, dialogFragment);
    }
}