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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.data.ImageCache;
import net.lacolaco.smileessence.entity.RBinding;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.util.StringUtils;
import net.lacolaco.smileessence.util.Themes;
import net.lacolaco.smileessence.util.UIObserverBundle;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.dialog.UserDetailDialogFragment;
import net.lacolaco.smileessence.view.listener.ListItemClickListener;

import java.lang.ref.WeakReference;
import java.util.Date;

public class EventViewModel implements IViewModel {

    // ------------------------------ FIELDS ------------------------------

    private EnumEvent event;
    private User source;
    private Tweet targetObject;
    private Date createdAt;

    // --------------------------- CONSTRUCTORS ---------------------------

    public EventViewModel(EnumEvent event, User source) {
        this(event, source, null);
    }

    public EventViewModel(EnumEvent event, User source, Tweet tweet) {
        this.event = event;
        this.source = source;
        this.createdAt = new Date();

        if (tweet != null) {
            if (event == EnumEvent.RETWEETED) {
                this.targetObject = tweet.getRetweetedTweet();
            } else {
                this.targetObject = tweet;
            }
        }
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public Date getCreatedAt() {
        return createdAt;
    }

    public EnumEvent getEvent() {
        return event;
    }

    public boolean isStatusEvent() {
        return targetObject != null;
    }

    public Tweet getTargetObject() {
        return targetObject;
    }

    public User getSource() {
        return source;
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface IViewModel ---------------------

    private void updateViewUser(View convertedView) {
        NetworkImageView icon = (NetworkImageView) convertedView.findViewById(R.id.imageview_status_icon);
        String iconUrl;
        if (UserPreferenceHelper.getInstance().get(R.string.key_setting_original_icon, false)) {
            iconUrl = source.getProfileImageUrlOriginal();
        } else {
            iconUrl = source.getProfileImageUrl();
        }
        ImageCache.getInstance().setImageToView(iconUrl, icon);

        TextView header = (TextView) convertedView.findViewById(R.id.textview_status_header);
        header.setText(getFormattedString());
    }

    @Override
    public View getView(final Activity activity, LayoutInflater inflater, View convertedView) {
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

        int textSize = UserPreferenceHelper.getInstance().getTextSize();

        TextView header = (TextView) convertedView.findViewById(R.id.textview_status_header);
        header.setTextSize(textSize);
        int colorHeader = Themes.getStyledColor(activity, R.attr.color_status_text_mine);
        header.setTextColor(colorHeader);

        updateViewUser(convertedView);

        TextView content = (TextView) convertedView.findViewById(R.id.textview_status_text);
        content.setTextSize(textSize);
        int colorNormal = Themes.getStyledColor(activity, R.attr.color_status_text_normal);
        content.setTextColor(colorNormal);
        content.setText(isStatusEvent() ? targetObject.getText() : "");
        TextView footer = (TextView) convertedView.findViewById(R.id.textview_status_footer);
        footer.setTextSize(textSize - 2);
        int colorFooter = Themes.getStyledColor(activity, R.attr.color_status_text_footer);
        footer.setTextColor(colorFooter);
        footer.setText(StringUtils.dateToString(getCreatedAt()));
        ImageView favorited = (ImageView) convertedView.findViewById(R.id.imageview_status_favorited);
        favorited.setVisibility(View.GONE);
        int colorBgNormal = Themes.getStyledColor(activity, R.attr.color_status_bg_normal);
        convertedView.setBackgroundColor(colorBgNormal);
        convertedView.setOnClickListener(new ListItemClickListener(activity, () -> {
            UserDetailDialogFragment fragment = new UserDetailDialogFragment();
            fragment.setUserID(source.getId());
            DialogHelper.showDialog(activity, fragment);
        }));

        final WeakReference<View> weakView = new WeakReference<>(convertedView);
        bundle.attach(source, changes -> {
            View strongView = weakView.get();
            if (strongView != null && changes.contains(RBinding.BASIC))
                updateViewUser(strongView);
        });

        return convertedView;
    }

    // -------------------------- OTHER METHODS --------------------------

    public String getFormattedString() {
        return Application.getInstance().getString(event.getTextFormatResourceID(), source.getScreenName());
    }

    public enum EnumEvent {

        FAVORITED(R.string.format_event_favorited),
        UNFAVORITED(R.string.format_event_unfavorited),
        RETWEETED(R.string.format_event_retweeted),
        MENTIONED(R.string.format_event_mentioned),
        FOLLOWED(R.string.format_event_followed),
        BLOCKED(R.string.format_event_blocked),
        UNBLOCKED(R.string.format_event_unblocked),
        RECEIVE_MESSAGE(R.string.format_event_message);

        // ------------------------------ FIELDS ------------------------------

        private int textFormatResourceID;

        // --------------------------- CONSTRUCTORS ---------------------------

        EnumEvent(int textFormatResourceID) {
            this.textFormatResourceID = textFormatResourceID;
        }

        // --------------------- GETTER / SETTER METHODS ---------------------

        public int getTextFormatResourceID() {
            return textFormatResourceID;
        }
    }
}
