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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.data.ImageCache;
import net.lacolaco.smileessence.entity.RBinding;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.util.*;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.dialog.StatusDetailDialogFragment;
import net.lacolaco.smileessence.view.dialog.UserDetailDialogFragment;
import net.lacolaco.smileessence.view.listener.ListItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class StatusViewModel implements IViewModel {
    private Tweet tweet;

    private ArrayList<BackgroundTask> lastTasks = new ArrayList<>(); // internal

    // --------------------------- CONSTRUCTORS ---------------------------

    public StatusViewModel(Tweet tw) {
        tweet = tw;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public Tweet getTweet() {
        return tweet;
    }

    private String getFooterText() {
        StringBuilder builder = new StringBuilder();
        if (tweet.isRetweet()) {
            builder
                    .append("(RT: ")
                    .append(tweet.getUser().getScreenName())
                    .append(") ");
        }
        builder.append(StringUtils.dateToString(tweet.getOriginalTweet().getCreatedAt()));
        builder.append(" via ");
        builder.append(Html.fromHtml(tweet.getOriginalTweet().getSource()));
        return builder.toString();
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface IViewModel ---------------------

    @Override
    public View getView(final Activity activity, final LayoutInflater inflater, View convertedView) {
        boolean extendStatusURL = UserPreferenceHelper.getInstance().get(R.string.key_setting_extend_status_url, true);
        return getView(activity, inflater, convertedView, extendStatusURL);
    }

    // -------------------------- OTHER METHODS --------------------------

    public View getView(final Activity activity, final LayoutInflater inflater, View convertedView, boolean extendStatusURL) {
        if (convertedView == null) {
            convertedView = inflater.inflate(R.layout.list_item_status, null);
        }
        UIObserverBundle bundle = (UIObserverBundle) convertedView.getTag();
        if (bundle != null) {
            bundle.detachAll();
        } else {
            bundle = new UIObserverBundle();
            convertedView.setTag(bundle);
        }

        convertedView.setOnClickListener(new ListItemClickListener(activity, () -> onClick(activity)));

        updateViewUser(((MainActivity) activity), convertedView);
        updateViewBody(((MainActivity) activity), convertedView);
        updateViewFavorited(((MainActivity) activity), convertedView);
        updateViewEmbeddeds(((MainActivity) activity), convertedView, extendStatusURL);

        final View view = convertedView;
        bundle.attach(tweet, (x, changes) -> {
            if (changes.contains(RBinding.FAVORITERS))
                updateViewFavorited(((MainActivity) activity), view);
        });
        bundle.attach(tweet.getUser(), (x, changes) -> {
            if (changes.contains(RBinding.BASIC))
                updateViewUser(((MainActivity) activity), view);
        });

        return convertedView;
    }

    private void updateViewUser(MainActivity activity, View convertedView) {
        int textSize = UserPreferenceHelper.getInstance().getTextSize();
        int nameStyle = UserPreferenceHelper.getInstance().getNameStyle();
        int theme = Application.getThemeResId();

        NetworkImageView icon = (NetworkImageView) convertedView.findViewById(R.id.imageview_status_icon);
        ImageCache.getInstance().setImageToView(tweet.getOriginalTweet().getUser().getProfileImageUrl(), icon);
        icon.setOnClickListener(v -> onIconClick(activity));

        TextView header = (TextView) convertedView.findViewById(R.id.textview_status_header);
        header.setTextSize(textSize);
        int colorHeader = Themes.getStyledColor(activity, theme, R.attr.color_status_text_header, 0);
        int colorMineHeader = Themes.getStyledColor(activity, theme, R.attr.color_status_text_mine, 0);
        header.setTextColor(tweet.getUser() == Application.getCurrentAccount().getUser() ? colorMineHeader : colorHeader);
        header.setText(NameStyles.getNameString(nameStyle, tweet.getOriginalTweet().getUser()));
    }

    private void updateViewBody(MainActivity activity, View convertedView) {
        int textSize = UserPreferenceHelper.getInstance().getTextSize();
        int theme = Application.getThemeResId();

        TextView content = (TextView) convertedView.findViewById(R.id.textview_status_text);
        content.setTextSize(textSize);
        int colorNormal = Themes.getStyledColor(activity, theme, R.attr.color_status_text_normal, 0);
        content.setTextColor(colorNormal);
        String rawText = tweet.getOriginalTweet().getText();
        if (isReadMorseEnabled() && Morse.isMorse(rawText)) {
            content.setText(String.format("%s\n(%s)", rawText, Morse.morseToJa(rawText)));
        } else {
            content.setText(rawText);
        }
        TextView footer = (TextView) convertedView.findViewById(R.id.textview_status_footer);
        footer.setTextSize(textSize - 2);
        int colorFooter = Themes.getStyledColor(activity, theme, R.attr.color_status_text_footer, 0);
        footer.setTextColor(colorFooter);
        footer.setText(getFooterText());


        if (tweet.isRetweet()) {
            int colorBgRetweet = Themes.getStyledColor(activity, theme, R.attr.color_status_bg_retweet, 0);
            convertedView.setBackgroundColor(colorBgRetweet);
        } else if (tweet.getOriginalTweet().getMentions().contains(Application.getCurrentAccount().getUser().getScreenName())) {
            int colorBgMention = Themes.getStyledColor(activity, theme, R.attr.color_status_bg_mention, 0);
            convertedView.setBackgroundColor(colorBgMention);
        } else {
            int colorBgNormal = Themes.getStyledColor(activity, theme, R.attr.color_status_bg_normal, 0);
            convertedView.setBackgroundColor(colorBgNormal);
        }
    }

    private void updateViewFavorited(MainActivity activity, View convertedView) {
        ImageView favorited = (ImageView) convertedView.findViewById(R.id.imageview_status_favorited);
        favorited.setVisibility(tweet.isFavoritedBy(Application.getCurrentAccount().getUserId()) ? View.VISIBLE : View.GONE);
    }

    private void updateViewEmbeddeds(MainActivity activity, View convertedView, boolean extendStatusURL) {
        for (BackgroundTask task : lastTasks) {
            task.cancel();
        }
        lastTasks.clear();
        final ViewGroup embeddedStatus = (ViewGroup) convertedView.findViewById(R.id.view_status_embedded_status);
        embeddedStatus.removeAllViews();
        if (extendStatusURL) {
            List<Long> embeddedStatusIDs = tweet.getEmbeddedStatusIDs();
            if (embeddedStatusIDs.size() > 0) {
                embeddedStatus.setVisibility(View.VISIBLE);
                for (long id : embeddedStatusIDs) {
                    BackgroundTask task = Application.getCurrentAccount().fetchTweet(id, embeddedTweet -> {
                        if (embeddedTweet != null) {
                            StatusViewModel viewModel = new StatusViewModel(embeddedTweet);
                            View embeddedHolder = viewModel.getView(activity, activity.getLayoutInflater(), null, false);
                            embeddedStatus.addView(embeddedHolder);
                            embeddedStatus.invalidate();
                        }
                    });
                    if (task != null) {
                        lastTasks.add(task);
                    }
                }

            }
        } else {
            embeddedStatus.setVisibility(View.GONE);
        }
    }

    private boolean isReadMorseEnabled() {
        return UserPreferenceHelper.getInstance().get(R.string.key_setting_read_morse, true);
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
