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
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.BuildConfig;
import net.lacolaco.smileessence.IntentRouter;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.data.UserListCache;
import net.lacolaco.smileessence.entity.*;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.notification.NotificationType;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.AppPreferenceHelper;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.*;
import net.lacolaco.smileessence.twitter.UserStreamListener;
import net.lacolaco.smileessence.twitter.task.*;
import net.lacolaco.smileessence.twitter.util.TwitterUtils;
import net.lacolaco.smileessence.util.*;
import net.lacolaco.smileessence.view.*;
import net.lacolaco.smileessence.view.adapter.*;
import net.lacolaco.smileessence.view.dialog.ConfirmDialogFragment;
import net.lacolaco.smileessence.viewmodel.*;
import net.lacolaco.smileessence.viewmodel.menu.MainActivityMenuHelper;
import twitter4j.Paging;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterStream;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    // ------------------------------ FIELDS ------------------------------

    public enum AdapterID {
        Home,
        Mentions,
        History,
        Messages,
        Search,
        UserList;

        public static AdapterID get(int ordinal) {
            return values()[ordinal];
        }
    }

    public static final int REQUEST_OAUTH = 10;
    public static final int REQUEST_GET_PICTURE_FROM_GALLERY = 11;
    public static final int REQUEST_GET_PICTURE_FROM_CAMERA = 12;
    private static final String KEY_LAST_USED_SEARCH_QUERY = "lastUsedSearchQuery";
    private static final String KEY_LAST_USED_ACCOUNT_ID = "lastUsedAccountID";
    private static final String KEY_LAST_USER_LIST = "lastUsedUserList";
    public static final int PAGE_INDEX_GONE = -1;
    private int pageIndexPost;
    private int pageIndexHome;
    private int pageIndexMentions;
    private int pageIndexHistory;
    private int pageIndexMessages;
    private int pageIndexSearch;
    private int pageIndexUserList;
    private ViewPager viewPager;
    private PageListAdapter pagerAdapter;
    private Account currentAccount;
    private TwitterStream stream;
    private HashMap<AdapterID, CustomListAdapter<?>> adapterMap = new HashMap<>();
    private boolean streaming = false;
    private Uri cameraTempFilePath;

    // --------------------- GETTER / SETTER METHODS ---------------------

    public AppPreferenceHelper getAppPreferenceHelper() {
        return new AppPreferenceHelper(this);
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

    private String getLastSearch() {
        return getAppPreferenceHelper().getValue(KEY_LAST_USED_SEARCH_QUERY, "");
    }

    public void setLastSearch(String query) {
        getAppPreferenceHelper().putValue(KEY_LAST_USED_SEARCH_QUERY, query);
    }

    private long getLastUsedAccountID() {
        String id = getAppPreferenceHelper().getValue(KEY_LAST_USED_ACCOUNT_ID, "");
        if (TextUtils.isEmpty(id)) {
            return -1;
        } else {
            return Long.parseLong(id);
        }
    }

    private void setLastUsedAccountID(Account account) {
        getAppPreferenceHelper().putValue(KEY_LAST_USED_ACCOUNT_ID, account.getId());
    }

    private String getLastUserList() {
        return getAppPreferenceHelper().getValue(KEY_LAST_USER_LIST, "");
    }

    public Collection<CustomListAdapter<?>> getListAdapters() {
        return adapterMap.values();
    }

    public int getPageIndexHome() {
        return pageIndexHome;
    }

    public int getPageIndexMentions() {
        return pageIndexMentions;
    }

    public int getPageIndexHistory() {
        return pageIndexHistory;
    }

    public int getPageIndexMessages() {
        return pageIndexMessages;
    }

    public int getPageIndexSearch() {
        return pageIndexSearch;
    }

    public int getPageIndexUserList() {
        return pageIndexUserList;
    }

    public PageListAdapter getPagerAdapter() {
        return pagerAdapter;
    }

    public int getThemeIndex() {
        return ((Application) getApplication()).getThemeIndex();
    }

    public UserPreferenceHelper getUserPreferenceHelper() {
        return new UserPreferenceHelper(this);
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    private boolean isAuthorized() {
        long lastUsedAccountID = getLastUsedAccountID();
        return lastUsedAccountID >= 0 && Account.load(Account.class, lastUsedAccountID) != null;
    }

    /**
     * Returns which twitter stream is running
     *
     * @return
     */
    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
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
        } else if (viewPager.getCurrentItem() != getPageIndexHome()) {
            viewPager.setCurrentItem(getPageIndexHome(), true);
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
        Notificator.stopNotification();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.debug("onResume");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Notificator.startNotification();
    }

    // -------------------------- OTHER METHODS --------------------------

    public int addListPage(Class<? extends CustomListFragment> fragmentClass, CustomListAdapter<?> adapter, AdapterID adapterId, boolean visible) {
        if (visible) {
            Bundle args = new Bundle();
            adapterMap.put(adapterId, adapter);
            return addPage(fragmentClass, args, false);
        } else {
            return PAGE_INDEX_GONE;
        }
    }

    @Deprecated
    public int addPage(Class<? extends PageFragment> fragmentClass, Bundle args, boolean withNotify) {
        pagerAdapter.addPage(fragmentClass, args, withNotify);
        return pagerAdapter.getCount() - 1;
    }

    public void forceFinish() {
        super.finish();
    }

    public CustomListAdapter<?> getListAdapter(AdapterID adapterId) {
        return adapterMap.get(adapterId);
    }

    public void openPostPage() {
        setSelectedPageIndex(pageIndexPost);
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
            Notificator.publish(this, R.string.notice_select_image_succeeded);
        } catch (Exception e) {
            e.printStackTrace();
            Notificator.publish(this, R.string.notice_select_image_failed, NotificationType.ALERT);
        }
    }

    /**
     * Open search page
     */
    public void openSearchPage() {
        setSelectedPageIndex(pageIndexSearch);
    }

    /**
     * Open search page with given query
     */
    public void openSearchPage(final String query) {
        startNewSearch(TwitterApi.getTwitter(getCurrentAccount()), query);
        openSearchPage();
    }

    public void openUserListPage(String listFullName) {
        startUserList(TwitterApi.getTwitter(getCurrentAccount()), listFullName);
        openUserListPage();
    }

    public void saveLastUserList(String lastUserList) {
        getAppPreferenceHelper().putValue(KEY_LAST_USER_LIST, lastUserList);
    }

    public void setSelectedPageIndex(final int position, final boolean smooth) {
        new UIHandler() {
            @Override
            public void run() {
                viewPager.setCurrentItem(position, smooth);
            }
        }.post();
    }

    public void startMainLogic() {
        initializeView();
        CommandSetting.initialize();
        startTwitter();
    }

    public void startNewSearch(final Twitter twitter, final String query) {
        setLastSearch(query);
        if (!TextUtils.isEmpty(query)) {
            final SearchListAdapter adapter = (SearchListAdapter) getListAdapter(AdapterID.Search);
            adapter.initSearch(query);
            adapter.clear();
            adapter.updateForce();
            new SearchTask(twitter, query, this) {
                @Override
                protected void onPostExecute(QueryResult queryResult) {
                    super.onPostExecute(queryResult);
                    if (queryResult != null) {
                        List<twitter4j.Status> tweets = queryResult.getTweets();
                        for (int i = tweets.size() - 1; i >= 0; i--) {
                            twitter4j.Status status = tweets.get(i);
                            if (!status.isRetweet()) {
                                StatusViewModel viewModel = new StatusViewModel(Tweet.fromTwitter(status));
                                adapter.addToTop(viewModel);
                                StatusFilter.filter(MainActivity.this, viewModel);
                            }
                        }
                        adapter.setTopID(queryResult.getMaxId());
                        adapter.updateForce();
                    }
                }
            }.execute();
        }
    }

    public boolean startStream() {
        if (!new NetworkHelper(this).canConnect()) {
            return false;
        }
        if (stream != null) {
            stream.shutdown();
        }
        stream = new TwitterApi(currentAccount).getTwitterStream();
        UserStreamListener listener = new UserStreamListener(this);
        stream.addListener(listener);
        stream.addConnectionLifeCycleListener(listener);
        stream.user();
        return true;
    }

    public boolean startTwitter() {
        if (!startStream()) {
            return false;
        }
        int count = TwitterUtils.getPagingCount(this);
        Twitter twitter = TwitterApi.getTwitter(currentAccount);
        Paging paging = TwitterUtils.getPaging(count);
        initInvisibleUser(twitter);
        initUserListCache(twitter);
        initHome(twitter, paging);
        initMentions(twitter, paging);
        initMessages(twitter, paging);
        initSearch(twitter);
        initUserList(twitter);
        updateActionBarIcon();
        return true;
    }

    public void updateActionBarIcon() {
        Twitter twitter = new TwitterApi(currentAccount).getTwitter();
        final ImageView homeIcon = (ImageView) findViewById(android.R.id.home);
        ShowUserTask userTask = new ShowUserTask(twitter, currentAccount.userID) {
            @Override
            protected void onPostExecute(User user) {
                super.onPostExecute(user);
                if (user != null) {
                    String urlHttps = user.getProfileImageUrl();
                    homeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    new BitmapURLTask(urlHttps, homeIcon).execute();
                }
            }
        };
        userTask.execute();
    }

    private void addPostPage() {
        pageIndexPost = addPage(PostFragment.class, null, true);
    }

    private void addHomePage() {
        StatusListAdapter homeAdapter = new StatusListAdapter(this);
        pageIndexHome = addListPage( HomeFragment.class, homeAdapter, AdapterID.Home, true);
    }

    private void addMentionsPage() {
        StatusListAdapter mentionsAdapter = new StatusListAdapter(this);
        pageIndexMentions = addListPage(MentionsFragment.class, mentionsAdapter, AdapterID.Mentions, true);
    }

    private void addHistoryPage() {
        boolean visible = getUserPreferenceHelper().getValue(R.string.key_page_history_visibility, true);
        getUserPreferenceHelper().putValue(R.string.key_page_history_visibility, visible);
        EventListAdapter historyAdapter = new EventListAdapter(this);
        pageIndexHistory = addListPage(HistoryFragment.class, historyAdapter, AdapterID.History, visible);
    }

    private void addMessagesPage() {
        boolean visible = getUserPreferenceHelper().getValue(R.string.key_page_messages_visibility, true);
        getUserPreferenceHelper().putValue(R.string.key_page_messages_visibility, visible);
        MessageListAdapter messagesAdapter = new MessageListAdapter(this);
        pageIndexMessages = addListPage(MessagesFragment.class, messagesAdapter, AdapterID.Messages, visible);
    }

    private void addSearchPage() {
        boolean visible = getUserPreferenceHelper().getValue(R.string.key_page_search_visibility, true);
        getUserPreferenceHelper().putValue(R.string.key_page_search_visibility, visible);
        SearchListAdapter searchAdapter = new SearchListAdapter(this);
        pageIndexSearch = addListPage(SearchFragment.class, searchAdapter, AdapterID.Search, visible);
    }

    private void addUserListPage() {
        boolean visible = getUserPreferenceHelper().getValue(R.string.key_page_list_visibility, true);
        getUserPreferenceHelper().putValue(R.string.key_page_list_visibility, visible);
        UserListListAdapter userListAdapter = new UserListListAdapter(this);
        pageIndexUserList = addListPage(UserListFragment.class, userListAdapter, AdapterID.UserList, visible);
    }

    private void getImageUri(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Logger.error(requestCode);
            Notificator.publish(this, R.string.notice_select_image_failed);
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

    private void initHome(final Twitter twitter, final Paging paging) {
        new HomeTimelineTask(twitter, this, paging) {
            @Override
            protected void onPostExecute(List<Tweet> tweets) {
                super.onPostExecute(tweets);
                StatusListAdapter adapter = (StatusListAdapter) getListAdapter(AdapterID.Home);
                for (Tweet tweet : tweets) {
                    StatusViewModel statusViewModel = new StatusViewModel(tweet);
                    adapter.addToBottom(statusViewModel);
                    StatusFilter.filter(MainActivity.this, statusViewModel);
                }
                adapter.updateForce();
            }
        }.execute();
    }

    private void initInvisibleUser(Twitter twitter) {
        new BlockIDsTask(twitter).execute();
        new MutesIDsTask(twitter).execute();
    }

    private void initMentions(final Twitter twitter, final Paging paging) {
        new MentionsTimelineTask(twitter, this, paging) {
            @Override
            protected void onPostExecute(List<Tweet> tweets) {
                super.onPostExecute(tweets);
                StatusListAdapter adapter = (StatusListAdapter) getListAdapter(AdapterID.Mentions);
                for (Tweet tweet : tweets) {
                    adapter.addToBottom(new StatusViewModel(tweet));
                }
                adapter.updateForce();
            }
        }.execute();
    }

    private void initMessages(final Twitter twitter, final Paging paging) {
        if (pageIndexMessages == PAGE_INDEX_GONE) {
            return;
        }
        new DirectMessagesTask(twitter, this, paging) {
            @Override
            protected void onPostExecute(List<DirectMessage> directMessages) {
                super.onPostExecute(directMessages);
                MessageListAdapter adapter = (MessageListAdapter) getListAdapter(AdapterID.Messages);
                for (DirectMessage message : directMessages) {
                    adapter.addToBottom(new MessageViewModel(message));
                }
                adapter.notifyDataSetChanged();
            }
        }.execute();
        new SentDirectMessagesTask(twitter, this, paging) {
            @Override
            protected void onPostExecute(List<DirectMessage> directMessages) {
                super.onPostExecute(directMessages);
                MessageListAdapter adapter = (MessageListAdapter) getListAdapter(AdapterID.Messages);
                for (DirectMessage message : directMessages) {
                    adapter.addToBottom(new MessageViewModel(message));
                }
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private void initPostState() {
        PostState.newState().beginTransaction().commit();
    }

    private void initSearch(Twitter twitter) {
        if (pageIndexSearch == PAGE_INDEX_GONE) {
            return;
        }
        String lastUsedSearchQuery = getLastSearch();
        if (!TextUtils.isEmpty(lastUsedSearchQuery)) {
            startNewSearch(twitter, lastUsedSearchQuery);
        }
    }

    private void initUserList(Twitter twitter) {
        if (pageIndexUserList == PAGE_INDEX_GONE) {
            return;
        }
        String lastUserList = getLastUserList();
        if (!TextUtils.isEmpty(lastUserList)) {
            startUserList(twitter, lastUserList);
        }
    }

    private void initUserListCache(Twitter twitter) {
        UserListCache.getInstance().clear();
        new GetUserListsTask(twitter).execute();
    }

    private void initializePages() {
        addPostPage();
        addHomePage();
        addMentionsPage();
        addHistoryPage();
        addMessagesPage();
        addSearchPage();
        addUserListPage();
        pagerAdapter.notifyDataSetChanged();
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());
        initPostState();
        setSelectedPageIndex(pageIndexHome, false);
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
        setSelectedPageIndex(pageIndexUserList);
    }

    private void receiveOAuth(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Logger.error(requestCode);
            Notificator.publish(this, R.string.notice_error_authenticate);
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
        ((Application) getApplication()).setThemeIndex(getUserPreferenceHelper().getValue(R.string.key_setting_theme, 0));
        setTheme(Themes.getTheme(getThemeIndex()));
    }

    private void setupAccount() {
        Account account = Account.load(Account.class, getLastUsedAccountID());
        setCurrentAccount(account);
    }

    private void startOAuthActivity() {
        startActivityForResult(new Intent(this, OAuthActivity.class), REQUEST_OAUTH);
    }

    private void startUserList(Twitter twitter, String listFullName) {
        saveLastUserList(listFullName);
        final UserListListAdapter adapter = (UserListListAdapter) getListAdapter(AdapterID.UserList);
        adapter.setListFullName(listFullName);
        adapter.clear();
        adapter.updateForce();
        new UserListStatusesTask(twitter, this, listFullName) {
            @Override
            protected void onPostExecute(List<Tweet> tweets) {
                super.onPostExecute(tweets);
                for (Tweet tweet : tweets) {
                    StatusViewModel statusViewModel = new StatusViewModel(tweet);
                    adapter.addToBottom(statusViewModel);
                    StatusFilter.filter(MainActivity.this, statusViewModel);
                }
                adapter.updateForce();
            }
        }.execute();
    }
}
