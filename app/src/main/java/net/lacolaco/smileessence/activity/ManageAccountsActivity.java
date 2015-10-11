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

package net.lacolaco.smileessence.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.android.volley.toolbox.NetworkImageView;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.data.ImageCache;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.InternalPreferenceHelper;
import net.lacolaco.smileessence.twitter.OAuthSession;
import net.lacolaco.smileessence.view.dialog.ConfirmDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ManageAccountsActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final int REQUEST_OAUTH = 10;
    private EditAccountsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Application.getInstance().getThemeResId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_list);

        adapter = new EditAccountsAdapter();
        ListView listView = (ListView) findViewById(R.id.listview_edit_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        Logger.debug("onCreate");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem add = menu.add(Menu.NONE, R.id.menu_edit_list_add, Menu.NONE, "");
        add.setIcon(android.R.drawable.ic_menu_add);
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Account account = adapter.getItem(i);
        if (account != Application.getInstance().getCurrentAccount()) {
            setCurrentAccount(account);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapter.getCount() > 1) {
            // remove account from application
            Account account = adapter.getItem(i);
            ConfirmDialogFragment.show(this, getString(R.string.dialog_confirm_clear_account, account.getUser().getScreenName()), () -> {
                adapter.removeAt(i);
                Account.unregister(account.getModelId());
                if (account == Application.getInstance().getCurrentAccount()) {
                    setCurrentAccount(adapter.getItem(0));
                }
            }, false);
            return true;
        } else {
            Notificator.getInstance().alert(R.string.notice_cant_remove_last_account);
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_list_add: {
                startActivityForResult(new Intent(this, OAuthActivity.class), REQUEST_OAUTH);
                break;
            }
            case android.R.id.home: {
                safeFinish();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        safeFinish();
    }

    private void safeFinish() {
        if (Application.getInstance().getCurrentAccount() != null) {
            setResult(RESULT_OK);
            finish();
        } else {
            ConfirmDialogFragment.show(this, getString(R.string.notice_no_account_selected), () -> {
                setResult(RESULT_CANCELED);
                finish();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OAUTH: {
                receiveOAuth(requestCode, resultCode, data);
                break;
            }
            default: {
                Logger.error(String.format("[BUG] unexpected activity result: reqCode=%d, resCode=%d", requestCode, resultCode));
                break;
            }
        }
    }

    private void receiveOAuth(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Account account = Account.register(data.getStringExtra(OAuthSession.KEY_TOKEN),
                    data.getStringExtra(OAuthSession.KEY_TOKEN_SECRET),
                    data.getLongExtra(OAuthSession.KEY_USER_ID, -1L),
                    data.getStringExtra(OAuthSession.KEY_SCREEN_NAME));
            adapter.add(account);
            if (Application.getInstance().getCurrentAccount() == null) {
                setCurrentAccount(account);
            }
        } else {
            Logger.error(requestCode);
            Notificator.getInstance().alert(R.string.notice_error_authenticate);
        }
    }

    private void setCurrentAccount(Account account) {
        Application.getInstance().setCurrentAccount(account);
        InternalPreferenceHelper.getInstance().set(R.string.key_last_used_account_id, account.getModelId());
    }

    private class EditAccountsAdapter extends BaseAdapter {
        private final List<Account> accounts;

        public EditAccountsAdapter() {
            accounts = new ArrayList<>(Account.all());
        }

        @Override
        public int getCount() {
            return accounts.size();
        }

        @Override
        public Account getItem(int position) {
            return accounts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return accounts.get(position).getModelId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_account, null);
            }
            Account account = getItem(position);
            NetworkImageView iconView = (NetworkImageView) convertView.findViewById(R.id.account_icon);
            ImageCache.getInstance().setImageToView(account.getUser().getProfileImageUrl(), iconView);

            TextView textView = (TextView) convertView.findViewById(R.id.account_text_view);
            String text = "@" + account.getUser().getScreenName();
            textView.setText(text);

            RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.account_radio_button);
            radioButton.setChecked(account == Application.getInstance().getCurrentAccount());

            return convertView;
        }

        public int add(Account account) {
            accounts.add(account);
            notifyDataSetChanged();
            return accounts.size() - 1;
        }

        public Account removeAt(int position) {
            Account account = accounts.remove(position);
            notifyDataSetChanged();
            return account;
        }
    }
}
