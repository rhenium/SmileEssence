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

package net.lacolaco.smileessence.command.status;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.notification.Notificator;

public class StatusCommandCopyURLToClipboard extends StatusCommand {

    // --------------------------- CONSTRUCTORS ---------------------------

    public StatusCommandCopyURLToClipboard(Activity activity, Tweet tweet) {
        super(R.id.key_command_status_copy_url_to_clipboard, activity, tweet);
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    public String getText() {
        return getActivity().getString(R.string.command_status_copy_url_to_clipboard);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // -------------------------- OTHER METHODS --------------------------

    @Override
    public boolean execute() {
        String statusURL = getOriginalStatus().getTwitterUrl();
        ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("tweet url", statusURL));
        Notificator.getInstance().publish(R.string.notice_copy_clipboard);
        return true;
    }
}
