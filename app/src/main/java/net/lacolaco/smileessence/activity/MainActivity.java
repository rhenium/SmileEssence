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

import android.app.ActionBar;
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
import net.lacolaco.smileessence.data.PostState;
import net.lacolaco.smileessence.data.UserListCache;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.CommandSetting;
import net.lacolaco.smileessence.entity.MuteUserIds;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.notification.NotificationType;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.InternalPreferenceHelper;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.OAuthSession;
import net.lacolaco.smileessence.twitter.UserStreamListener;
import net.lacolaco.smileessence.twitter.task.BlockIDsTask;
import net.lacolaco.smileessence.twitter.task.GetUserListsTask;
import net.lacolaco.smileessence.twitter.task.MutesIDsTask;
import net.lacolaco.smileessence.twitter.task.ShowUserTask;
import net.lacolaco.smileessence.util.*;
import net.lacolaco.smileessence.view.*;
import net.lacolaco.smileessence.view.adapter.PageListAdapter;
import net.lacolaco.smileessence.view.dialog.ConfirmDialogFragment;
import net.lacolaco.smileessence.viewmodel.menu.MainActivityMenuHelper;
import twitter4j.TwitterStream;
import twitter4j.UserList;

public class MainActivity extends Activity {

    // ------------------------------ FIELDS ------------------------------

    public static final int REQUEST_OAUTH = 10;
    public static final int REQUEST_GET_PICTURE_FROM_GALLERY = 11;
    public static final int REQUEST_GET_PICTURE_FROM_CAMERA = 12;
    private ViewPager viewPager;
    private PageListAdapter pagerAdapter;
    private Account currentAccount;
    private TwitterStream stream;
    private Uri cameraTempFilePath;
    private UserStreamListener userStreamListener;

    // --------------------- GETTER / SETTER METHODS ---------------------
    public int getRequestCountPerPage() {
        return UserPreferenceHelper.getInstance().get(R.string.key_setting_timelines, 20);
    }

    public Uri getCameraTempFilePath() {
        return cameraTempFilePath;
    }

    public void setCameraTempFilePath(Uri cameraTempFilePath) {
        this.cameraTempFilePath = cameraTempFilePath;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Account account) {
        this.currentAccount = account;
    }

    public String getLastSearch() {
        return InternalPreferenceHelper.getInstance().get(R.string.key_last_used_search_query, "");
    }

    public void setLastSearch(String query) {
        InternalPreferenceHelper.getInstance().set(R.string.key_last_used_search_query, query);
    }

    private long getLastUsedAccountID() {
        return InternalPreferenceHelper.getInstance().get(R.string.key_last_used_account_id, -1L);
    }

    private void setLastUsedAccountID(Account account) {
        InternalPreferenceHelper.getInstance().set(R.string.key_last_used_account_id, account.getId());
    }

    public String getLastUserList() {
        return InternalPreferenceHelper.getInstance().get(R.string.key_last_used_user_list, "");
    }

    public void setLastUserList(String lastUserList) {
        InternalPreferenceHelper.getInstance().set(R.string.key_last_used_user_list, lastUserList);
    }

    public int getThemeIndex() {
        return ((Application) getApplication()).getThemeIndex();
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    private boolean isAuthorized() {
        long lastUsedAccountID = getLastUsedAccountID();
        return lastUsedAccountID >= 0 && Account.load(Account.class, lastUsedAccountID) != null;
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
            ConfirmDialogFragment.show(this, getString(R.string.dialog_confirm_finish_app), new Runnable() {

                @Override
                public void run() {
                    forceFinish();
                }
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
            case REQUEST_GET_PICTURE_FROM_GALLERY:
            case REQUEST_GET_PICTURE_FROM_CAMERA: {
                getImageUri(requestCode, resultCode, data);
                break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (isAuthorized()) {
            setupAccount();
            startMainLogic();
            IntentRouter.onNewIntent(this, getIntent());
        } else {
            startOAuthActivity();
        }
        Notificator.initialize(this);
        Logger.debug("onCreate");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MainActivityMenuHelper.addItemsToMenu(this, menu);
        return true;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        return MainActivityMenuHelper.onItemSelected(this, item);
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
            fragment.startSearch(getCurrentAccount(), query);
            openSearchPage();
        }
    }

    public void openUserListPage(String listFullName) {
        UserListFragment fragment = pagerAdapter.getFragment(UserListFragment.class);
        if (fragment != null) {
            fragment.startUserList(getCurrentAccount(), listFullName);
            openUserListPage();
        }
    }

    public void setSelectedPageIndex(final int position, final boolean smooth) {
        new UIHandler().post(() -> viewPager.setCurrentItem(position, smooth));
    }

    public void startMainLogic() {
        CommandSetting.initialize();
        initializeView();
        startTwitter();
    }

    public boolean startStream() {
        if (!new NetworkHelper(this).canConnect()) {
            return false;
        }
        if (stream != null) {
            stream.shutdown();
        }
        stream = currentAccount.getTwitterStream();
        userStreamListener = new UserStreamListener(currentAccount);
        stream.addListener(userStreamListener);
        stream.addConnectionLifeCycleListener(userStreamListener);
        stream.user();
        return true;
    }

    public boolean startTwitter() {
        if (!startStream()) {
            return false;
        }
        initInvisibleUser();
        initUserListCache();
        updateActionBarIcon();
        return true;
    }

    public void updateActionBarIcon() {
        final ImageView homeIcon = (ImageView) findViewById(android.R.id.home);
        new ShowUserTask(currentAccount, currentAccount.getUserId())
                .onDoneUI(user -> {
                    String urlHttps = user.getProfileImageUrl();
                    homeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    new BitmapURLTask(urlHttps, homeIcon).execute();
                })
                .onFail(x -> Notificator.getInstance().publish(R.string.notice_error_show_user, NotificationType.ALERT))
                .execute();
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

    private void initInvisibleUser() {
        new BlockIDsTask(getCurrentAccount()).onDone(idList -> {
            for (Long blockID : idList) {
                MuteUserIds.add(blockID);
            }
        }).execute();
        new MutesIDsTask(getCurrentAccount()).onDone(mutesIDs -> {
            for (Long mutesID : mutesIDs) {
                MuteUserIds.add(mutesID);
            }
        }).execute();
    }

    private void initPostState() {
        PostState.newState().beginTransaction().commit();
    }

    private void initUserListCache() {
        UserListCache.getInstance().clear();
        new GetUserListsTask(getCurrentAccount())
                .onDone(lists -> {
                    for (UserList list : lists) {
                        UserListCache.getInstance().put(list.getFullName());
                    }
                })
                .onFail(x -> { /* TODO */ })
                .execute();
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
        initPostState();
        setSelectedPageIndex(pagerAdapter.getIndex(HomeFragment.class), false);
    }

    public void initializeView() {
        ActionBar bar = getActionBar();
        bar.setDisplayShowHomeEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new PageListAdapter(this, viewPager);
        initializePages();
    }

    private void openUserListPage() {
        setSelectedPageIndex(pagerAdapter.getIndex(UserListFragment.class));
    }

    private void receiveOAuth(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Logger.error(requestCode);
            Notificator.getInstance().publish(R.string.notice_error_authenticate);
            finish();
        } else {
            Account account = new Account(data.getStringExtra(OAuthSession.KEY_TOKEN),
                    data.getStringExtra(OAuthSession.KEY_TOKEN_SECRET),
                    data.getLongExtra(OAuthSession.KEY_USER_ID, -1L),
                    data.getStringExtra(OAuthSession.KEY_SCREEN_NAME));
            account.save();
            setCurrentAccount(account);
            setLastUsedAccountID(account);
            startMainLogic();
        }
    }

    private void setTheme() {
        ((Application) getApplication()).setThemeIndex(UserPreferenceHelper.getInstance().get(R.string.key_setting_theme, 0));
        setTheme(Themes.getTheme(getThemeIndex()));
    }

    private void setupAccount() {
        Account account = Account.load(Account.class, getLastUsedAccountID());
        setCurrentAccount(account);
    }

    private void startOAuthActivity() {
        startActivityForResult(new Intent(this, OAuthActivity.class), REQUEST_OAUTH);
    }
}
