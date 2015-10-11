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

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.util.IntentUtils;
import net.lacolaco.smileessence.view.adapter.UnorderedCustomListAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectImageDialogFragment extends MenuDialogFragment {

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected void setMenuItems(final UnorderedCustomListAdapter<Command> adapter) {
        List<Command> commands = getCommands();
        Command.filter(commands);
        adapter.addItemsToBottom(commands);
        adapter.update();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.dialog_title_select_image);
        return dialog;
    }

    // -------------------------- OTHER METHODS --------------------------

    private List<Command> getCommands() {
        Activity activity = getActivity();
        ArrayList<Command> commands = new ArrayList<>();
        commands.add(new Command(-1, activity) {
            @Override
            public boolean execute() {
                startGallery();
                return true;
            }

            @Override
            public String getText() {
                return activity.getString(R.string.command_select_image_from_gallery);
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        });
        commands.add(new Command(-1, activity) {
            @Override
            public boolean execute() {
                startCamera();
                return true;
            }

            @Override
            public String getText() {
                return activity.getString(R.string.command_select_image_from_camera);
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        });
        return commands;
    }

    private void startCamera() {
        MainActivity activity = (MainActivity) getActivity();
        ContentValues values = new ContentValues();
        String filename = System.currentTimeMillis() + ".jpg";
        values.put(MediaStore.MediaColumns.TITLE, filename);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        // Uriを取得して覚えておく、Intentにも保存先として渡す
        Uri tempFilePath = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        activity.setCameraTempFilePath(tempFilePath);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempFilePath);
        IntentUtils.startActivityForResultIfFound(activity, intent, MainActivity.REQUEST_GET_PICTURE_FROM_CAMERA);
    }

    private void startGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        IntentUtils.startActivityForResultIfFound(getActivity(), intent, MainActivity.REQUEST_GET_PICTURE_FROM_GALLERY);
    }
}
