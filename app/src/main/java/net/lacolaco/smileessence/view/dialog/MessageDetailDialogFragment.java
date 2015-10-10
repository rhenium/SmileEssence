/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2012-2015 lacolaco.net
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

package net.lacolaco.smileessence.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.CommandOpenURL;
import net.lacolaco.smileessence.entity.DirectMessage;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.twitter.task.DeleteMessageTask;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.adapter.MessageListAdapter;
import net.lacolaco.smileessence.view.listener.ListItemClickListener;
import net.lacolaco.smileessence.viewmodel.MessageViewModel;

import java.util.ArrayList;

public class MessageDetailDialogFragment extends StackableDialogFragment implements View.OnClickListener {

    // ------------------------------ FIELDS ------------------------------

    private static final String KEY_MESSAGE_ID = "messageID";
    private DirectMessage message;

    // --------------------- GETTER / SETTER METHODS ---------------------

    public long getMessageID() {
        return getArguments().getLong(KEY_MESSAGE_ID);
    }

    public void setMessageID(long messageID) {
        Bundle args = new Bundle();
        args.putLong(KEY_MESSAGE_ID, messageID);
        setArguments(args);
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface OnClickListener ---------------------

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_status_detail_reply: {
                openSendMessageDialog();
                break;
            }
            case R.id.button_status_detail_delete: {
                deleteMessage();
                break;
            }
            case R.id.button_status_detail_menu: {
                openMenu();
                break;
            }
            default: {
                dismiss();
            }
        }
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        message = DirectMessage.fetch(getMessageID());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (message == null) {
            Notificator.getInstance().alert(R.string.notice_error_get_messages);
            return new DisposeDialog(getActivity());
        }

        View header = getTitleView();
        ListView listView = (ListView) header.findViewById(R.id.listview_status_detail_reply_to);
        final MessageListAdapter adapter = new MessageListAdapter(getActivity());
        listView.setAdapter(adapter);

        // TODO: 効率的な探索どうする
        DirectMessage replyTo = null;
        for (DirectMessage mes : DirectMessage.cached()) {
            if (message.getId() > mes.getId() &&
                    message.getRecipient() == mes.getSender() &&
                    message.getSender() == mes.getRecipient() &&
                    (replyTo == null || replyTo.getId() < mes.getId())) {
                replyTo = mes;
            }
        }
        if (replyTo != null) {
            listView.setVisibility(View.VISIBLE);
            adapter.addItem(new MessageViewModel(replyTo));
            adapter.updateForce();
        } else {
            listView.setVisibility(View.GONE);
        }

        return new AlertDialog.Builder(getActivity()).setView(header).create();
    }

    // -------------------------- OTHER METHODS --------------------------

    public void deleteMessage() {
        ConfirmDialogFragment.show(getActivity(), getString(R.string.dialog_confirm_commands), () -> {
            new DeleteMessageTask(Application.getInstance().getCurrentAccount(), message.getId())
                    .onDone(x -> Notificator.getInstance().publish(R.string.notice_message_delete_succeeded))
                    .onFail(x -> Notificator.getInstance().alert(R.string.notice_message_delete_failed))
                    .execute();
            dismiss();
        });
    }

    public void openSendMessageDialog() {
        SendMessageDialogFragment dialogFragment = new SendMessageDialogFragment();
        dialogFragment.setScreenName(message.getSender().getScreenName());
        DialogHelper.showDialog(getActivity(), dialogFragment);
    }

    private ArrayList<Command> getCommands() {
        ArrayList<Command> commands = new ArrayList<>();
        // URL
        for (String url : message.getUrlsExpanded()) {
            commands.add(new CommandOpenURL(getActivity(), url));
        }
        for (String url : message.getMediaUrls()) {
            commands.add(new CommandOpenURL(getActivity(), url));
        }
        return commands;
    }

    private View getTitleView() {
        MainActivity activity = (MainActivity) getActivity();
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_status_detail, null);
        View messageHeader = view.findViewById(R.id.layout_status_header);
        MessageViewModel statusViewModel = new MessageViewModel(message);
        messageHeader = statusViewModel.getView(activity, activity.getLayoutInflater(), messageHeader);
        messageHeader.setClickable(false);
        int background = ((ColorDrawable) messageHeader.getBackground()).getColor();
        view.setBackgroundColor(background);
        ImageButton reply = (ImageButton) view.findViewById(R.id.button_status_detail_reply);
        reply.setOnClickListener(this);
        ImageButton delete = (ImageButton) view.findViewById(R.id.button_status_detail_delete);
        delete.setVisibility(Application.getInstance().getCurrentAccount().canDelete(message) ? View.VISIBLE : View.GONE);
        delete.setOnClickListener(this);
        ImageButton menuButton = (ImageButton) view.findViewById(R.id.button_status_detail_menu);
        menuButton.setOnClickListener(this);
        LinearLayout commandsLayout = (LinearLayout) view.findViewById(R.id.linearlayout_status_detail_menu);
        commandsLayout.setClickable(true);
        // commands
        ArrayList<Command> commands = getCommands();
        Command.filter(commands);
        for (final Command command : commands) {
            View commandView = command.getView(activity, activity.getLayoutInflater(), null);
            commandView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent));
            commandView.setOnClickListener(new ListItemClickListener(activity, command::execute));
            commandsLayout.addView(commandView);
        }
        // status only parts
        view.findViewById(R.id.button_status_detail_retweet).setVisibility(View.GONE);
        view.findViewById(R.id.button_status_detail_favorite).setVisibility(View.GONE);
        view.findViewById(R.id.image_status_detail_fav_count).setVisibility(View.GONE);
        view.findViewById(R.id.image_status_detail_rt_count).setVisibility(View.GONE);
        return view;
    }

    private void openMenu() {
        MessageMenuDialogFragment fragment = new MessageMenuDialogFragment();
        fragment.setMessageID(getMessageID());
        DialogHelper.showDialog(getActivity(), fragment);
    }
}
