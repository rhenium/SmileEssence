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

package net.lacolaco.smileessence.view;

import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.CommandOpenSearch;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.SearchQuery;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.notification.NotificationType;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.twitter.StatusFilter;
import net.lacolaco.smileessence.twitter.task.SearchTask;
import net.lacolaco.smileessence.twitter.util.TwitterUtils;
import net.lacolaco.smileessence.util.UIHandler;
import net.lacolaco.smileessence.view.adapter.SearchListAdapter;
import net.lacolaco.smileessence.view.dialog.SelectSearchQueryDialogFragment;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;

import java.util.List;

public class SearchFragment extends CustomListFragment<SearchListAdapter> implements View.OnClickListener, View.OnFocusChangeListener,
        SearchListAdapter.OnQueryChangeListener {

    // ------------------------------ FIELDS ------------------------------

    private EditText editText;

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    protected PullToRefreshBase.Mode getRefreshMode() {
        return PullToRefreshBase.Mode.BOTH;
    }

    // ------------------------ INTERFACE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SearchListAdapter adapter = new SearchListAdapter(getActivity());
        setAdapter(adapter);

        final MainActivity activity = (MainActivity) getActivity();
        final Twitter twitter = activity.getCurrentAccount().getTwitter();
        String lastUsedSearchQuery = activity.getLastSearch();
        if (!TextUtils.isEmpty(lastUsedSearchQuery)) {
            startSearch(twitter, lastUsedSearchQuery);
        }
    }

    // --------------------- Interface OnClickListener ---------------------

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_search_queries: {
                openSearchQueryDialog((MainActivity) getActivity());
                break;
            }
            case R.id.button_search_execute: {
                search();
                break;
            }
            case R.id.button_search_save: {
                saveQuery();
            }
        }
    }

    // --------------------- Interface OnFocusChangeListener ---------------------

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            hideIME();
        }
    }

    // --------------------- Interface OnQueryChangeListener ---------------------

    @Override
    public void onQueryChange(String newQuery) {
        if (editText != null) {
            editText.setText(newQuery);
        }
    }

    // --------------------- Interface OnRefreshListener2 ---------------------

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final MainActivity activity = (MainActivity) getActivity();
        final Account currentAccount = activity.getCurrentAccount();
        Twitter twitter = currentAccount.getTwitter();
        final SearchListAdapter adapter = getAdapter();
        String queryString = adapter.getQuery();
        if (TextUtils.isEmpty(queryString)) {
            new UIHandler(() -> {
                notifyTextEmpty(activity);
                refreshView.onRefreshComplete();
            }).post();
            return;
        }
        final Query query = new Query();
        query.setQuery(queryString);
        query.setCount(TwitterUtils.getPagingCount(activity));
        query.setResultType(Query.RECENT);
        if (adapter.getCount() > 0) {
            query.setSinceId(adapter.getTopID());
        }
        new SearchTask(twitter, query) {
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
                            StatusFilter.getInstance().filter(viewModel);
                        }
                    }
                    updateListViewWithNotice(refreshView.getRefreshableView(), true);
                    adapter.setTopID(queryResult.getMaxId());
                    refreshView.onRefreshComplete();
                }
            }
        }.execute();
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final MainActivity activity = (MainActivity) getActivity();
        final Account currentAccount = activity.getCurrentAccount();
        Twitter twitter = currentAccount.getTwitter();
        final SearchListAdapter adapter = getAdapter();
        String queryString = adapter.getQuery();
        if (TextUtils.isEmpty(queryString)) {
            new UIHandler(() -> {
                notifyTextEmpty(activity);
                refreshView.onRefreshComplete();
            }).post();
            return;
        }
        final Query query = new Query();
        query.setQuery(queryString);
        query.setCount(TwitterUtils.getPagingCount(activity));
        query.setResultType(Query.RECENT);
        if (adapter.getCount() > 0) {
            query.setMaxId(adapter.getLastID() - 1);
        }
        new SearchTask(twitter, query) {
            @Override
            protected void onPostExecute(QueryResult queryResult) {
                super.onPostExecute(queryResult);
                if (queryResult != null) {
                    List<twitter4j.Status> tweets = queryResult.getTweets();
                    for (twitter4j.Status status : tweets) {
                        if (!status.isRetweet()) {
                            StatusViewModel viewModel = new StatusViewModel(Tweet.fromTwitter(status));
                            adapter.addToBottom(viewModel);
                            StatusFilter.getInstance().filter(viewModel);
                        }
                    }
                    updateListViewWithNotice(refreshView.getRefreshableView(), false);
                    refreshView.onRefreshComplete();
                }
            }
        }.execute();
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected PullToRefreshListView getListView(View page) {
        return (PullToRefreshListView) page.findViewById(R.id.listview_search);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.actionbar_search);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View page = inflater.inflate(R.layout.fragment_search, container, false);
        PullToRefreshListView listView = getListView(page);
        SearchListAdapter adapter = getAdapter();
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        listView.setOnRefreshListener(this);
        listView.setMode(getRefreshMode());
        ImageButton buttonQueries = getQueriesButton(page);
        buttonQueries.setOnClickListener(this);
        ImageButton buttonExecute = getExecuteButton(page);
        buttonExecute.setOnClickListener(this);
        ImageButton buttonSave = getSaveButton(page);
        buttonSave.setOnClickListener(this);
        editText = getEditText(page);
        editText.setOnFocusChangeListener(this);
        editText.setText(adapter.getQuery());
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH ||
                        keyEvent != null &&
                        keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    search();
                }
                return true;
            }
        });
        editText.setMovementMethod(new ArrowKeyMovementMethod() {
            @Override
            protected boolean right(TextView widget, Spannable buffer) {
                //Don't move page
                return widget.getSelectionEnd() == widget.length() || super.right(widget, buffer);
            }

            @Override
            protected boolean left(TextView widget, Spannable buffer) {
                //Don't move page
                return widget.getSelectionStart() == 0 || super.left(widget, buffer);
            }
        });
        adapter.setOnQueryChangeListener(this);
        return page;
    }

    private EditText getEditText(View page) {
        return (EditText) page.findViewById(R.id.edittext_search);
    }

    private ImageButton getExecuteButton(View page) {
        return (ImageButton) page.findViewById(R.id.button_search_execute);
    }

    private ImageButton getQueriesButton(View page) {
        return (ImageButton) page.findViewById(R.id.button_search_queries);
    }

    private ImageButton getSaveButton(View page) {
        return (ImageButton) page.findViewById(R.id.button_search_save);
    }

    private void hideIME() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void notifyTextEmpty(MainActivity activity) {
        Notificator.getInstance().publish(R.string.notice_search_text_empty);
    }

    private void openSearchQueryDialog(final MainActivity mainActivity) {
        if (SearchQuery.getAll().size() == 0) {
            Notificator.getInstance().publish(R.string.notice_no_query_exists);
            return;
        }
        DialogHelper.showDialog(mainActivity, new SelectSearchQueryDialogFragment() {
            @Override
            protected void executeCommand(Command command) {
                super.executeCommand(command);
                SearchQuery query = ((CommandOpenSearch) command).getQuery();
                editText.setText(query.query);
                hideIME();
            }
        });
    }

    private void saveQuery() {
        String text = editText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            Notificator.getInstance().publish(R.string.notice_query_is_empty, NotificationType.ALERT);
        } else {
            SearchQuery.saveIfNotFound(text);
            Notificator.getInstance().publish(R.string.notice_query_saved);
        }
    }

    private void search() {
        if (editText != null) {
            String text = editText.getText().toString();
            if (TextUtils.isEmpty(text)) {
                Notificator.getInstance().publish(R.string.notice_query_is_empty, NotificationType.ALERT);
            } else {
                ((MainActivity) getActivity()).openSearchPage(text);
                hideIME();
            }
        }
    }

    public void startSearch(final Twitter twitter, final String queryString) {
        ((MainActivity) getActivity()).setLastSearch(queryString);
        if (!TextUtils.isEmpty(queryString)) {
            final SearchListAdapter adapter = getAdapter();
            adapter.initSearch(queryString);
            adapter.clear();
            adapter.updateForce();
            final Query query = new Query();
            query.setQuery(queryString);
            query.setCount(TwitterUtils.getPagingCount((MainActivity) getActivity()));
            query.setResultType(Query.RECENT);
            new SearchTask(twitter, query) {
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
                                StatusFilter.getInstance().filter(viewModel);
                            }
                        }
                        adapter.setTopID(queryResult.getMaxId());
                        adapter.updateForce();
                    }
                }
            }.execute();
        }
    }
}
