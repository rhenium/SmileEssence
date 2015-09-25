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
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.DirectMessage;
import net.lacolaco.smileessence.entity.RO;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.util.NameStyles;
import net.lacolaco.smileessence.util.StringUtils;
import net.lacolaco.smileessence.util.Themes;
import net.lacolaco.smileessence.util.UIObserverBundle;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.dialog.MessageDetailDialogFragment;
import net.lacolaco.smileessence.view.dialog.UserDetailDialogFragment;
import net.lacolaco.smileessence.view.listener.ListItemClickListener;

public class MessageViewModel implements IViewModel {

    // ------------------------------ FIELDS ------------------------------

    public static final String DETAIL_DIALOG = "messageDetail";

    private final DirectMessage directMessage;

    // --------------------------- CONSTRUCTORS ---------------------------

    public MessageViewModel(DirectMessage mes) {
        directMessage = mes;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------


    public DirectMessage getDirectMessage() {
        return directMessage;
    }

    private String getFooterText(Account account) {
        StringBuilder builder = new StringBuilder();
        builder.append(StringUtils.dateToString(directMessage.getCreatedAt()));
        if (directMessage.getSender().getId() == account.getUserId()) {
            builder.append(" to @").append(directMessage.getRecipient().getScreenName());
        }
        return builder.toString();
    }

    // ------------------------ INTERFACE METHODS ------------------------

    // --------------------- Interface IViewModel ---------------------

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

        int theme = ((Application) activity.getApplication()).getThemeResId();
        int colorBgMessage = Themes.getStyledColor(activity, theme, R.attr.color_message_bg_normal, 0);
        convertedView.setBackgroundColor(colorBgMessage);
        convertedView.setOnClickListener(new ListItemClickListener(activity, () -> {
            MessageDetailDialogFragment dialogFragment = new MessageDetailDialogFragment();
            dialogFragment.setMessageID(directMessage.getId());
            DialogHelper.showDialog(activity, dialogFragment);
        }));

        ImageView favorited = (ImageView) convertedView.findViewById(R.id.imageview_status_favorited);
        favorited.setVisibility(View.GONE);

        updateViewSender(activity, convertedView);
        updateViewBody(activity, convertedView);

        final View finalView = convertedView;
        bundle.attach(directMessage.getSender(), (x, changes) -> {
            if (changes.contains(RO.BASIC))
                updateViewSender(activity, finalView);
        });

        return convertedView;
    }

    private void updateViewSender(Activity activity, View convertedView) {
        int textSize = UserPreferenceHelper.getInstance().getTextSize();
        int nameStyle = UserPreferenceHelper.getInstance().getNameStyle();
        int theme = ((Application) activity.getApplication()).getThemeResId();

        NetworkImageView icon = (NetworkImageView) convertedView.findViewById(R.id.imageview_status_icon);
        ImageCache.getInstance().setImageToView(directMessage.getSender().getProfileImageUrl(), icon);
        icon.setOnClickListener(v -> {
            UserDetailDialogFragment dialogFragment = new UserDetailDialogFragment();
            dialogFragment.setUserID(directMessage.getSender().getId());
            DialogHelper.showDialog(activity, dialogFragment);
        });

        TextView header = (TextView) convertedView.findViewById(R.id.textview_status_header);
        header.setTextSize(textSize);
        int colorHeader = Themes.getStyledColor(activity, theme, R.attr.color_message_text_header, 0);
        header.setTextColor(colorHeader);
        header.setText(NameStyles.getNameString(nameStyle, directMessage.getSender()));
    }

    private void updateViewBody(Activity activity, View convertedView) {
        int textSize = UserPreferenceHelper.getInstance().getTextSize();
        int theme = ((Application) activity.getApplication()).getThemeResId();

        TextView content = (TextView) convertedView.findViewById(R.id.textview_status_text);
        content.setTextSize(textSize);
        int colorNormal = Themes.getStyledColor(activity, theme, R.attr.color_status_text_normal, 0);
        content.setTextColor(colorNormal);
        content.setText(directMessage.getText());
        TextView footer = (TextView) convertedView.findViewById(R.id.textview_status_footer);
        footer.setTextSize(textSize - 2);
        int colorFooter = Themes.getStyledColor(activity, theme, R.attr.color_status_text_footer, 0);
        footer.setTextColor(colorFooter);
        footer.setText(getFooterText(Application.getCurrentAccount()));
    }
}
