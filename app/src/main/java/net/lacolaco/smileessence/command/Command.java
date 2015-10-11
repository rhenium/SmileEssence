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

package net.lacolaco.smileessence.command;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.command.message.MessageCommandCopyTextToClipboard;
import net.lacolaco.smileessence.command.message.MessageCommandSearchOnGoogle;
import net.lacolaco.smileessence.command.message.MessageCommandShare;
import net.lacolaco.smileessence.command.message.MessageCommandTofuBuster;
import net.lacolaco.smileessence.command.status.*;
import net.lacolaco.smileessence.command.user.*;
import net.lacolaco.smileessence.entity.CommandSetting;
import net.lacolaco.smileessence.entity.DirectMessage;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.viewmodel.IViewModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Command implements IViewModel {

    // ------------------------------ FIELDS ------------------------------

    private final int key;
    private final Activity activity;

    // -------------------------- STATIC METHODS --------------------------

    public Command(int key, Activity activity) {
        this.key = key;
        this.activity = activity;
    }

    public static List<Command> getAllCommands(Activity activity) {
        List<Command> commands = new ArrayList<>();
        commands.addAll(getStatusCommands(activity, null));
        commands.addAll(getUserCommands(activity, null));
        return commands;
    }

    public static List<Command> getUserCommands(Activity activity, User user) {
        List<Command> commands = new ArrayList<>();
        commands.add(new UserCommandReply(activity, user));
        commands.add(new UserCommandAddToReply(activity, user));
        commands.add(new UserCommandSendMessage(activity, user));
        commands.add(new UserCommandBlock(activity, user));
        commands.add(new UserCommandUnblock(activity, user));
        commands.add(new UserCommandReportForSpam(activity, user));
        commands.add(new UserCommandOpenFavstar(activity, user));
        commands.add(new UserCommandOpenAclog(activity, user));
        commands.add(new UserCommandOpenTwilog(activity, user));
        commands.add(new UserCommandIntroduce(activity, user));
        return commands;
    }

    public static List<Command> getStatusCommands(Activity activity, Tweet tweet) {
        List<Command> commands = new ArrayList<>();
        commands.add(new StatusCommandAddToReply(activity, tweet));
        commands.add(new StatusCommandOpenTalkView(activity, tweet));
        commands.add(new StatusCommandFavAndRT(activity, tweet));
        commands.add(new StatusCommandOpenQuoteDialog(activity, tweet));
        commands.add(new StatusCommandShare(activity, tweet));
        commands.add(new StatusCommandOpenInBrowser(activity, tweet));
        commands.add(new StatusCommandCopyTextToClipboard(activity, tweet));
        commands.add(new StatusCommandCopyURLToClipboard(activity, tweet));
        commands.add(new StatusCommandCopy(activity, tweet));
        commands.add(new StatusCommandSearchOnGoogle(activity, tweet));
        commands.add(new StatusCommandTofuBuster(activity, tweet));
        commands.add(new StatusCommandNanigaja(activity, tweet));
        commands.add(new StatusCommandMakeAnonymous(activity, tweet));
        commands.add(new StatusCommandCongratulate(activity, tweet));
        commands.add(new StatusCommandReview(activity, tweet));
        return commands;
    }

    public static List<Command> getMessageCommands(Activity activity, DirectMessage message) {
        List<Command> commands = new ArrayList<>();
        commands.add(new MessageCommandShare(activity, message));
        commands.add(new MessageCommandCopyTextToClipboard(activity, message));
        commands.add(new MessageCommandSearchOnGoogle(activity, message));
        commands.add(new MessageCommandTofuBuster(activity, message));
        return commands;
    }

    // --------------------------- CONSTRUCTORS ---------------------------

    public static void filter(List<Command> commands) {
        Iterator<Command> iterator = commands.iterator();
        while (iterator.hasNext()) {
            Command command = iterator.next();
            if (!command.isEnabled()) {
                iterator.remove();
            } else if (command.getKey() >= 0) {
                if (!CommandSetting.isVisible(command.getKey())) {
                    iterator.remove();
                }
            }
        }
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    protected Activity getActivity() {
        return activity;
    }

    public int getKey() {
        return key;
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface IViewModel ---------------------

    @Override
    public final View getView(Activity activity, LayoutInflater inflater, View convertedView) {
        if (convertedView == null) {
            convertedView = inflater.inflate(R.layout.menu_item_simple_text, null);
        }
        TextView textView = (TextView) convertedView.findViewById(R.id.list_item_textview);
        textView.setTextSize(UserPreferenceHelper.getInstance().getTextSize());
        textView.setText(getText());
        return convertedView;
    }

    // -------------------------- OTHER METHODS --------------------------

    public abstract boolean execute();

    public abstract String getText();

    public abstract boolean isEnabled();
}
