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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.data.ImageCache;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.IdObject;
import net.lacolaco.smileessence.entity.RBinding;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.util.*;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.adapter.StatusListAdapter;
import net.lacolaco.smileessence.view.dialog.StatusDetailDialogFragment;
import net.lacolaco.smileessence.view.dialog.UserDetailDialogFragment;
import net.lacolaco.smileessence.view.listener.ListItemClickListener;

import java.lang.ref.WeakReference;

public class StatusViewModel implements IViewModel, IdObject {
    private final Tweet tweet;
    private StatusListAdapter embeddedTweetsAdapter = null; // load when first rendering
    private final boolean expandEmbeddedTweets;

    // --------------------------- CONSTRUCTORS ---------------------------

    public StatusViewModel(Tweet tw) {
        this(tw, UserPreferenceHelper.getInstance().get(R.string.key_setting_extend_status_url, true));
    }

    public StatusViewModel(Tweet tw, boolean expand) {
        tweet = tw;
        expandEmbeddedTweets = expand;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public Tweet getTweet() {
        return tweet;
    }

    @Override
    public long getId() {
        return tweet.getId();
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

        updateViewUser(activity, convertedView);
        updateViewBody(activity, convertedView);
        updateViewFavorited(convertedView);
        updateViewEmbeddeds(activity, convertedView);

        final WeakReference<View> weakView = new WeakReference<>(convertedView);
        final WeakReference<Activity> weakActivity = new WeakReference<>(activity);
        bundle.attach(tweet.getOriginalTweet(), changes -> {
            View strongView = weakView.get();
            if (strongView != null && changes.contains(RBinding.FAVORITERS))
                updateViewFavorited(strongView);
        });
        bundle.attach(tweet.getUser(), changes -> {
            View strongView = weakView.get();
            Activity strongActivity = weakActivity.get();
            if (strongView != null && strongActivity != null && changes.contains(RBinding.BASIC))
                updateViewUser(strongActivity, strongView);
        });

        return convertedView;
    }

    private void updateViewUser(Activity activity, View convertedView) {
        int textSize = UserPreferenceHelper.getInstance().getTextSize();
        int nameStyle = UserPreferenceHelper.getInstance().getNameStyle();

        NetworkImageView icon = (NetworkImageView) convertedView.findViewById(R.id.imageview_status_icon);
        String iconUrl;
        if (UserPreferenceHelper.getInstance().get(R.string.key_setting_original_icon, false)) {
            iconUrl = tweet.getOriginalTweet().getUser().getProfileImageUrlOriginal();
        } else {
            iconUrl = tweet.getOriginalTweet().getUser().getProfileImageUrl();
        }
        ImageCache.getInstance().setImageToView(iconUrl, icon);
        icon.setOnClickListener(v -> onIconClick(activity));

        TextView header = (TextView) convertedView.findViewById(R.id.textview_status_header);
        header.setTextSize(textSize);
        int colorHeader = Themes.getStyledColor(activity, R.attr.color_status_text_header);
        int colorMineHeader = Themes.getStyledColor(activity, R.attr.color_status_text_mine);
        header.setTextColor(tweet.getUser() == Application.getInstance().getCurrentAccount().getUser() ? colorMineHeader : colorHeader);
        header.setText(NameStyles.getNameString(nameStyle, tweet.getOriginalTweet().getUser()));
    }

    private void updateViewBody(Activity activity, View convertedView) {
        int textSize = UserPreferenceHelper.getInstance().getTextSize();

        TextView content = (TextView) convertedView.findViewById(R.id.textview_status_text);
        content.setTextSize(textSize);
        int colorNormal = Themes.getStyledColor(activity, R.attr.color_status_text_normal);
        content.setTextColor(colorNormal);
        String rawText = tweet.getOriginalTweet().getText();
        if (isReadMorseEnabled() && Morse.isMorse(rawText)) {
            content.setText(String.format("%s\n(%s)", rawText, Morse.morseToJa(rawText)));
        } else {
            content.setText(rawText);
        }
        TextView footer = (TextView) convertedView.findViewById(R.id.textview_status_footer);
        footer.setTextSize(textSize - 2);
        int colorFooter = Themes.getStyledColor(activity, R.attr.color_status_text_footer);
        footer.setTextColor(colorFooter);
        footer.setText(getFooterText());


        if (tweet.isRetweet()) {
            int colorBgRetweet = Themes.getStyledColor(activity, R.attr.color_status_bg_retweet);
            convertedView.setBackgroundColor(colorBgRetweet);
        } else if (tweet.getOriginalTweet().getMentions().contains(Application.getInstance().getCurrentAccount().getUser().getScreenName())) {
            int colorBgMention = Themes.getStyledColor(activity, R.attr.color_status_bg_mention);
            convertedView.setBackgroundColor(colorBgMention);
        } else {
            int colorBgNormal = Themes.getStyledColor(activity, R.attr.color_status_bg_normal);
            convertedView.setBackgroundColor(colorBgNormal);
        }
    }

    private void updateViewFavorited(View convertedView) {
        ImageView favorited = (ImageView) convertedView.findViewById(R.id.imageview_status_favorited);
        favorited.setVisibility(tweet.isFavoritedBy(Application.getInstance().getCurrentAccount().getUserId()) ? View.VISIBLE : View.GONE);
    }

    private void prepareEmbeddedTweetsAdapter(Activity activity) {
        if (embeddedTweetsAdapter != null) {
            return;
        }
        embeddedTweetsAdapter = new StatusListAdapter(activity);

        Account account = Application.getInstance().getCurrentAccount();
        for (long id : tweet.getEmbeddedStatusIDs()) {
            Tweet.fetchTask(id, account).onDone(t -> {
                StatusViewModel viewModel = new StatusViewModel(t, false);
                embeddedTweetsAdapter.addItem(viewModel);
                embeddedTweetsAdapter.update();
            }).execute();
        }
    }

    private void updateViewEmbeddeds(Activity activity, View convertedView) {
        final ListView embeddedStatus = (ListView) convertedView.findViewById(R.id.listview_status_embedded_status);
        if (expandEmbeddedTweets) {
            prepareEmbeddedTweetsAdapter(activity);
            embeddedStatus.setAdapter(embeddedTweetsAdapter);
            embeddedStatus.setVisibility(View.VISIBLE);
        } else {
            embeddedStatus.setAdapter(null); // view may be reused, set null explicitly
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
