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

package net.lacolaco.smileessence.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.twitter.Validator;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.data.PostState;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.twitter.task.SendMessageTask;
import net.lacolaco.smileessence.util.SystemServiceHelper;

public class SendMessageDialogFragment extends StackableDialogFragment implements TextWatcher, View.OnClickListener {

    // ------------------------------ FIELDS ------------------------------

    private static final String screenNameKey = "screenName";
    private String screenName;
    private EditText editText;
    private TextView textViewCount;
    private Button buttonSend;

    // --------------------- GETTER / SETTER METHODS ---------------------

    public void setScreenName(String screenName) {
        Bundle args = new Bundle();
        args.putString(screenNameKey, screenName);
        setArguments(args);
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface OnClickListener ---------------------

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.button_send_message: {
                sendMessage();
                break;
            }
            case R.id.button_send_message_delete: {
                deleteMessage();
            }
        }
    }

    // --------------------- Interface TextWatcher ---------------------

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Validator validator = new Validator();
        int remainingCount = 140 - validator.getTweetLength(s.toString());
        if (!TextUtils.isEmpty(PostState.getState().getMediaFilePath())) {
            remainingCount -= validator.getShortUrlLength();
        }
        textViewCount.setText(String.valueOf(remainingCount));
        if (remainingCount == 140 || remainingCount < 0) {
            textViewCount.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
            buttonSend.setEnabled(false);
        } else {
            textViewCount.setTextAppearance(getActivity(), android.R.style.TextAppearance_Widget_TextView);
            buttonSend.setEnabled(true);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        screenName = args.getString(screenNameKey);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_send_message, null);
        TextView textViewName = (TextView) view.findViewById(R.id.textview_send_message_name);
        textViewName.setText("To: @" + screenName);
        textViewCount = (TextView) view.findViewById(R.id.textview_send_message_count);
        editText = (EditText) view.findViewById(R.id.edittext_send_message);
        editText.addTextChangedListener(this);
        buttonSend = (Button) view.findViewById(R.id.button_send_message);
        buttonSend.setOnClickListener(this);
        ImageButton buttonDelete = (ImageButton) view.findViewById(R.id.button_send_message_delete);
        buttonDelete.setOnClickListener(this);
        editText.setText("");
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    private void deleteMessage() {
        editText.setText("");
    }

    private void sendMessage() {
        SystemServiceHelper.hideIM(getActivity(), editText);
        String text = editText.getText().toString();
        new SendMessageTask(Application.getInstance().getCurrentAccount(), screenName, text)
                .onDone(x -> Notificator.getInstance().publish(R.string.notice_message_send_succeeded))
                .onFail(x -> Notificator.getInstance().alert(R.string.notice_message_send_failed))
                .execute();
        dismiss();
    }
}
