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
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.BuildConfig;
import net.lacolaco.smileessence.IntentRouter;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.data.PostState;
import net.lacolaco.smileessence.entity.*;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.InternalPreferenceHelper;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.UserStreamListener;
import net.lacolaco.smileessence.twitter.task.ShowUserTask;
import net.lacolaco.smileessence.util.BitmapOptimizer;
import net.lacolaco.smileessence.util.BitmapURLTask;
import net.lacolaco.smileessence.util.IntentUtils;
import net.lacolaco.smileessence.util.UIObserverBundle;
import net.lacolaco.smileessence.view.adapter.PageListAdapter;
import net.lacolaco.smileessence.view.dialog.ConfirmDialogFragment;
import net.lacolaco.smileessence.view.page.*;
import twitter4j.TwitterStream;

import java.lang.reflect.Field;

public class MainActivity extends Activity implements Application.OnCurrentAccountChangedListener {
    // ------------------------------ FIELDS ------------------------------

    public static final int REQUEST_GET_PICTURE_FROM_GALLERY = 11;
    public static final int REQUEST_GET_PICTURE_FROM_CAMERA = 12;
    private static final int REQUEST_MANAGE_ACCOUNT = 13;
    private ViewPager viewPager;
    private ImageView currentAccountIconImageView;
    private PageListAdapter pagerAdapter;
    private TwitterStream stream;
    private Uri cameraTempFilePath;
    private UserStreamListener userStreamListener;
    private final UIObserverBundle currentUserBundle = new UIObserverBundle();

    public Uri getCameraTempFilePath() {
        return cameraTempFilePath;
    }

    public void setCameraTempFilePath(Uri cameraTempFilePath) {
        this.cameraTempFilePath = cameraTempFilePath;
    }

    public boolean isStreaming() {
        return userStreamListener != null && userStreamListener.isConnected();
    }

    public void setSelectedPageIndex(int position) {
        setSelectedPageIndex(position, true);
    }

    public void setSelectedPageIndex(int position, boolean smooth) {
        viewPager.setCurrentItem(position, smooth);
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
            Notificator.getInstance().alert(R.string.notice_select_image_failed);
        }
    }

    public void openSearchPage() {
        setSelectedPageIndex(pagerAdapter.getIndex(SearchFragment.class));
    }

    public void openSearchPage(String query) {
        SearchFragment fragment = (SearchFragment) pagerAdapter.getCachedFragment(pagerAdapter.getIndex(SearchFragment.class));
        if (fragment != null) {
            fragment.startSearch(query);
            openSearchPage();
        }
    }

    public void openUserListPage(String listFullName) {
        UserListFragment fragment = (UserListFragment) pagerAdapter.getCachedFragment(pagerAdapter.getIndex(UserListFragment.class));
        if (fragment != null) {
            fragment.startUserList(listFullName);
            openUserListPage();
        }
    }

    private void openUserListPage() {
        setSelectedPageIndex(pagerAdapter.getIndex(UserListFragment.class));
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public void onBackPressed() {
        this.finish();
    }

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

    public void forceFinish() {
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MANAGE_ACCOUNT: {
                if (resultCode == RESULT_CANCELED && Application.getInstance().getCurrentAccount() == null) {
                    forceFinish();
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
        Application app = (Application) getApplication();
        app.resetState();
        setTheme(app.getThemeResId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new PageListAdapter(this, viewPager);
        currentAccountIconImageView = (ImageView) findViewById(android.R.id.home);
        currentAccountIconImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Notificator.getInstance().setDefault(this);
        CommandSetting.initialize();
        Account.load();
        ExtractionWord.load();
        initializePages();
        app.addOnCurrentAccountChangedListener(this);

        Account account = getLastUsedAccount();
        if (account != null) {
            app.setCurrentAccount(account);
            IntentRouter.onNewIntent(this, getIntent());
        } else {
            startActivityForResult(new Intent(this, ManageAccountsActivity.class), REQUEST_MANAGE_ACCOUNT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentUserBundle.detachAll();
        Crouton.cancelAllCroutons();
        if (stream != null) {
            new Thread(stream::shutdown).start();
        }
        if (BuildConfig.DEBUG) fixIMMLeak(); // LeakCanary shows
        fixCroutonLeak();
        Logger.debug("onDestroy");
    }

    private void fixIMMLeak() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Object imm = getSystemService(Context.INPUT_METHOD_SERVICE);
                Field mCurRootView = imm.getClass().getDeclaredField("mCurRootView");
                mCurRootView.setAccessible(true);
                mCurRootView.set(imm, null);
                Field mServedView = imm.getClass().getDeclaredField("mServedView");
                mServedView.setAccessible(true);
                mServedView.set(imm, null);
                Field mNextServedView = imm.getClass().getDeclaredField("mNextServedView");
                mNextServedView.setAccessible(true);
                mNextServedView.set(imm, null);
            } catch (Exception e) {
                Logger.debug("imm fix error: " + e);
            }
        }
    }

    private void fixCroutonLeak() {
        try {
            Class klass = Class.forName("de.keyboardsurfer.android.widget.crouton.DefaultAnimationsBuilder");
            Field slideInDownAnimation = klass.getDeclaredField("slideInDownAnimation");
            slideInDownAnimation.setAccessible(true);
            slideInDownAnimation.set(null, null);
            Field slideOutUpAnimation = klass.getDeclaredField("slideOutUpAnimation");
            slideOutUpAnimation.setAccessible(true);
            slideOutUpAnimation.set(null, null);
        } catch (Exception e) {
            Logger.error("crouton fix error: " + e);
        }
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
                IntentUtils.openUri(this, Application.getInstance().getCurrentAccount().getUser().getFavstarRecentURL());
                return true;
            }
            case R.id.actionbar_aclog: {
                IntentUtils.openUri(this, Application.getInstance().getCurrentAccount().getUser().getAclogTimelineURL());
                return true;
            }
            case R.id.actionbar_twilog: {
                IntentUtils.openUri(this, Application.getInstance().getCurrentAccount().getUser().getTwilogURL());
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

    private void getImageUri(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Logger.error(requestCode);
            Notificator.getInstance().alert(R.string.notice_select_image_failed);
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

    private boolean startStream() {
        if (stream != null) {
            new Thread(stream::cleanUp).start();
        }
        stream = Application.getInstance().getCurrentAccount().getTwitterStream();
        userStreamListener = new UserStreamListener(Application.getInstance().getCurrentAccount());
        stream.addListener(userStreamListener);
        stream.addConnectionLifeCycleListener(userStreamListener);
        stream.user();
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCurrentAccountChanged(Account account) {
        Logger.debug(String.format("onCurrentAccountChanged: %s", account.getUser().getScreenName()));
        User user = account.getUser();

        // update cache
        account.refreshListSubscriptions();
        account.refreshUserMuteList();
        new ShowUserTask(account, account.getUserId()).execute();

        // update actionbar
        Runnable update = () -> {
            getActionBar().setTitle(user.getScreenName());
            String newUrl = user.getProfileImageUrl();
            if (newUrl != null) {
                new BitmapURLTask(newUrl, currentAccountIconImageView).execute();
            }
        };
        update.run(); //first run

        currentUserBundle.detachAll();
        currentUserBundle.attach(user, changes -> {
            if (changes.contains(RBinding.BASIC)) update.run();
        });

        // refresh all pages
        for (int i = 0; i < pagerAdapter.getCount(); ++i) {
            PageFragment pf = pagerAdapter.getCachedFragment(i);
            if (pf != null && pf.isAdded()) {
                Logger.debug(String.format("PageFragment %s is already attached; refreshing", pf.getClass().getName()));
                pf.refresh();
            }
        }

        // start user stream
        startStream();
    }

    // TODO: tab order?
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
        viewPager.setAdapter(pagerAdapter);
        setSelectedPageIndex(pagerAdapter.getIndex(HomeFragment.class), false);
        PostState.newState().beginTransaction().commit();
    }

    private Account getLastUsedAccount() {
        long lastId = InternalPreferenceHelper.getInstance().get(R.string.key_last_used_account_id, -1L);
        Account account = null;
        if (lastId != -1) {
            account = Account.get(lastId);
        }
        if (account == null && Account.count() > 0) {
            account = Account.all().get(0);
        }
        return account;
    }
}
