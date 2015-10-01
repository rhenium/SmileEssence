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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.BuildConfig;
import net.lacolaco.smileessence.IntentRouter;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.command.CommandOpenURL;
import net.lacolaco.smileessence.data.PostState;
import net.lacolaco.smileessence.entity.*;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.notification.NotificationType;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.InternalPreferenceHelper;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.UserStreamListener;
import net.lacolaco.smileessence.twitter.task.ShowUserTask;
import net.lacolaco.smileessence.util.BitmapOptimizer;
import net.lacolaco.smileessence.util.BitmapURLTask;
import net.lacolaco.smileessence.util.NetworkHelper;
import net.lacolaco.smileessence.util.UIHandler;
import net.lacolaco.smileessence.view.*;
import net.lacolaco.smileessence.view.adapter.PageListAdapter;
import net.lacolaco.smileessence.view.dialog.ConfirmDialogFragment;
import twitter4j.TwitterStream;

public class MainActivity extends Activity {

    // ------------------------------ FIELDS ------------------------------

    public static final int REQUEST_GET_PICTURE_FROM_GALLERY = 11;
    public static final int REQUEST_GET_PICTURE_FROM_CAMERA = 12;
    private static final int REQUEST_MANAGE_ACCOUNT = 13;
    private ViewPager viewPager;
    private PageListAdapter pagerAdapter;
    private TwitterStream stream;
    private Uri cameraTempFilePath;
    private UserStreamListener userStreamListener;
    private boolean waitingAccount = true;

    public Uri getCameraTempFilePath() {
        return cameraTempFilePath;
    }

    public void setCameraTempFilePath(Uri cameraTempFilePath) {
        this.cameraTempFilePath = cameraTempFilePath;
    }

    /**
     * Returns whether twitter stream is running
     *
     * @return
     */
    public boolean isStreaming() {
        return userStreamListener != null && userStreamListener.isConnected();
    }

    public void setSelectedPageIndex(int position) {
        viewPager.setCurrentItem(position, true);
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface Callback ---------------------

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event);
        }
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK: {
                finish();
                return false;
            }
            default: {
                return super.dispatchKeyEvent(event);
            }
        }
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public void finish() {
        if (viewPager == null) {
            forceFinish();
            return;
        }
        int homeIndex = pagerAdapter.getIndex(HomeFragment.class);
        if (viewPager.getCurrentItem() != homeIndex) {
            viewPager.setCurrentItem(homeIndex, true);
        } else {
            ConfirmDialogFragment.show(this, getString(R.string.dialog_confirm_finish_app), this::forceFinish);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MANAGE_ACCOUNT: {
                if (waitingAccount) {
                    // first run
                    waitingAccount = false;
                    startMainLogic();
                }
                break;
            }
            case REQUEST_GET_PICTURE_FROM_GALLERY:
            case REQUEST_GET_PICTURE_FROM_CAMERA: {
                getImageUri(requestCode, resultCode, data);
                break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.debug("onCreate");
        setTheme(Application.getThemeResId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        Notificator.initialize(this);
        CommandSetting.initialize();
        Account.load();

        if (setupLastUsedAccount()) {
            waitingAccount = false;
            startMainLogic();
            IntentRouter.onNewIntent(this, getIntent());
        } else {
            startActivityForResult(new Intent(this, ManageAccountsActivity.class), REQUEST_MANAGE_ACCOUNT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stream != null) {
            stream.shutdown();
        }
        Notificator.getInstance().onBackground();
        Logger.debug("onDestroy");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        IntentRouter.onNewIntent(this, intent);
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionbar_post: {
                openPostPage();
                return true;
            }
            /* TODO: case R.id.actionbar_search: {
                openSearchPage();
                return true;
            }*/
            case R.id.actionbar_setting: {
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            }
            case R.id.actionbar_accounts: {
                startActivity(new Intent(this, ManageAccountsActivity.class));
                return true;
            }
            case R.id.actionbar_edit_templates: {
                startActivity(new Intent(this, EditTemplateActivity.class));
                return true;
            }
            case R.id.actionbar_edit_extraction: {
                startActivity(new Intent(this, EditExtractionActivity.class));
                return true;
            }
            case R.id.actionbar_edit_commands: {
                startActivity(new Intent(this, EditCommandActivity.class));
                return true;
            }
            case R.id.actionbar_edit_tabs: {
                startActivity(new Intent(this, EditTabActivity.class));
                return true;
            }
            case R.id.actionbar_favstar: {
                new CommandOpenURL(this, Application.getCurrentAccount().getUser().getFavstarRecentURL()).execute();
                return true;
            }
            case R.id.actionbar_aclog: {
                new CommandOpenURL(this, Application.getCurrentAccount().getUser().getAclogTimelineURL()).execute();
                return true;
            }
            case R.id.actionbar_twilog: {
                new CommandOpenURL(this, Application.getCurrentAccount().getUser().getTwilogURL()).execute();
                return true;
            }
            case R.id.actionbar_report: {
                PostState.getState().beginTransaction()
                        .appendText(getString(R.string.text_message_to_author, BuildConfig.VERSION_NAME))
                        .commitWithOpen(this);
                return true;
            }
            default: {
                return false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.debug("onPause");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Notificator.getInstance().onBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.debug("onResume");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Notificator.getInstance().onForeground();
    }

    // -------------------------- OTHER METHODS --------------------------

    public void forceFinish() {
        super.finish();
    }

    public void openHomePage() {
        setSelectedPageIndex(pagerAdapter.getIndex(HomeFragment.class));
    }

    public void openPostPage() {
        setSelectedPageIndex(pagerAdapter.getIndex(PostFragment.class));
    }

    public void openPostPageWithImage(Uri uri) {
        try {
            Cursor c = getContentResolver().query(uri, null, null, null, null);
            c.moveToFirst();
            String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
            String rotatedPath = BitmapOptimizer.rotateImageByExif(this, path);
            PostState.getState().beginTransaction()
                    .setMediaFilePath(rotatedPath)
                    .commitWithOpen(this);
            Notificator.getInstance().publish(R.string.notice_select_image_succeeded);
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            Notificator.getInstance().publish(R.string.notice_select_image_failed, NotificationType.ALERT);
        }
    }

    /**
     * Open search page
     */
    public void openSearchPage() {
        setSelectedPageIndex(pagerAdapter.getIndex(SearchFragment.class));
    }

    /**
     * Open search page with given query
     */
    public void openSearchPage(final String query) {
        SearchFragment fragment = pagerAdapter.getFragment(SearchFragment.class);
        if (fragment != null) {
            fragment.startSearch(query);
            openSearchPage();
        }
    }

    public void openUserListPage(String listFullName) {
        UserListFragment fragment = pagerAdapter.getFragment(UserListFragment.class);
        if (fragment != null) {
            fragment.startUserList(listFullName);
            openUserListPage();
        }
    }

    public void setSelectedPageIndex(final int position, final boolean smooth) {
        new UIHandler().post(() -> viewPager.setCurrentItem(position, smooth));
    }

    public void startMainLogic() {
        initializeView();
        onChangeCurrentAccount();
    }

    public boolean startStream() {

        if (stream != null) {
            stream.shutdown();
        }
        stream = Application.getCurrentAccount().getTwitterStream();
        userStreamListener = new UserStreamListener(Application.getCurrentAccount());
        stream.addListener(userStreamListener);
        stream.addConnectionLifeCycleListener(userStreamListener);
        stream.user();
        return true;
    }

    private void onChangeCurrentAccount() {
        if (!new NetworkHelper(this).canConnect()) {
            return; // TODO: error message?
        }

        Account account = Application.getCurrentAccount();
        User user = account.getUser();
        startStream();
        MuteUserIds.refresh(account);
        account.refreshListSubscriptions();

        final ImageView iconImageView = (ImageView) findViewById(android.R.id.home);

        Runnable update = () -> {
            getActionBar().setTitle(user.getScreenName());
            String newUrl = user.getProfileImageUrl();
            new BitmapURLTask(newUrl, iconImageView).execute();
        };

        update.run();
        user.addObserver(this, (x, changes) -> {
            if (changes.contains(RBinding.BASIC)) update.run();
        });

        new ShowUserTask(account, account.getUserId()).execute();
    }

    private void getImageUri(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Logger.error(requestCode);
            Notificator.getInstance().publish(R.string.notice_select_image_failed);
            finish();
            return;
        }
        Uri uri;
        if (requestCode == REQUEST_GET_PICTURE_FROM_GALLERY) {
            uri = data.getData();
        } else {
            uri = getCameraTempFilePath();
        }
        openPostPageWithImage(uri);
    }

    private void initializePages() {
        pagerAdapter.addPage(PostFragment.class, getString(R.string.page_name_post), null, false);
        pagerAdapter.addPage(HomeFragment.class, getString(R.string.page_name_home), null, false);
        pagerAdapter.addPage(MentionsFragment.class, getString(R.string.page_name_mentions), null, false);
        if (UserPreferenceHelper.getInstance().get(R.string.key_page_history_visibility, true))
            pagerAdapter.addPage(HistoryFragment.class, getString(R.string.page_name_history), null, false);
        if (UserPreferenceHelper.getInstance().get(R.string.key_page_messages_visibility, true))
            pagerAdapter.addPage(MessagesFragment.class, getString(R.string.page_name_messages), null, false);
        if (UserPreferenceHelper.getInstance().get(R.string.key_page_search_visibility, true))
            pagerAdapter.addPage(SearchFragment.class, getString(R.string.page_name_search), null, false);
        if (UserPreferenceHelper.getInstance().get(R.string.key_page_list_visibility, true))
            pagerAdapter.addPage(UserListFragment.class, getString(R.string.page_name_list), null, false);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());
        PostState.newState().beginTransaction().commit();
        setSelectedPageIndex(pagerAdapter.getIndex(HomeFragment.class), false);
    }

    public void initializeView() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new PageListAdapter(this, viewPager);
        ImageView iconImageView = (ImageView) findViewById(android.R.id.home);
        iconImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        initializePages();
    }

    private void openUserListPage() {
        setSelectedPageIndex(pagerAdapter.getIndex(UserListFragment.class));
    }

    private boolean setupLastUsedAccount() {
        long lastId = InternalPreferenceHelper.getInstance().get(R.string.key_last_used_account_id, -1L);
        Account account = null;
        if (lastId != -1) {
            account = Account.get(lastId);
        }
        if (account == null && Account.count() > 0) {
            account = Account.all().get(0);
        }
        if (account != null) {
            Application.setCurrentAccount(account);
            return true;
        } else {
            return false;
        }
    }
}
