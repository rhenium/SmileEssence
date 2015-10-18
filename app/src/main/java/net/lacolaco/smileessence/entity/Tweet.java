package net.lacolaco.smileessence.entity;

import android.net.Uri;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.lacolaco.smileessence.twitter.task.ShowStatusTask;
import net.lacolaco.smileessence.util.BackgroundTask;
import net.lacolaco.smileessence.util.ListUtils;
import twitter4j.Status;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Tweet extends EntitySupport {
    private static Cache<Long, Tweet> storage = CacheBuilder.newBuilder().softValues().build();

    public synchronized static Tweet fetch(long statusId) {
        return storage.getIfPresent(statusId);
    }

    public synchronized static BackgroundTask<Tweet, Void> fetchTask(long statusId, Account account) {
        Tweet tweet = fetch(statusId);
        if (tweet != null) {
            return new BackgroundTask<Tweet, Void>() {
                @Override
                protected Tweet doInBackground() throws Exception {
                    return tweet;
                }
            };
        } else {
            return new ShowStatusTask(account, statusId);
        }
    }

    public synchronized static void remove(long statusId) {
        storage.invalidate(statusId);
    }

    public synchronized static Tweet fromTwitter(final twitter4j.Status st, long myUserId) {
        Tweet t = fetch(st.getId());
        if (t == null) {
            t = new Tweet();
            storage.put(st.getId(), t);
        }

        t.update(st, myUserId);
        return t;
    }

    public synchronized static List<Tweet> fromTwitter(List<Status> sts, long myUserId) {
        return ListUtils.map(sts, st -> fromTwitter(st, myUserId));
    }

    // インスタンス
    private long id;
    private User user;
    private String text;
    private Date createdAt;
    private String source;
    private boolean isRetweet;
    private Tweet retweetedTweet;
    private long inReplyToStatusId;
    private int favoriteCount;
    private int retweetCount;
    private Set<Long> favoriters;
    private Map<Long, Long> retweets;

    private Tweet() {
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            Tweet original = getRetweetedTweet();
            if (original != null) {
                original.removeRetweet(getId());
            }
        }
    }

    private void update(twitter4j.Status status, long myUserId) {
        id = status.getId();
        user = User.fromTwitter(status.getUser());
        createdAt = status.getCreatedAt();
        source = status.getSource();
        isRetweet = status.isRetweet();

        if (!isRetweet) {
            text = extractText(status, false);
            inReplyToStatusId = status.getInReplyToStatusId();
            updateEntities(status);
            if (favoriters == null) favoriters = Collections.newSetFromMap(new ConcurrentHashMap<>());
            if (retweets == null) retweets = new ConcurrentHashMap<>();

            if (favoriteCount != status.getFavoriteCount() || retweetCount != status.getRetweetCount()) {
                favoriteCount = status.getFavoriteCount();
                retweetCount = status.getRetweetCount();

                notifyChange(RBinding.REACTION_COUNT);
            }

            if (status.isFavorited()) {
                addFavoriter(myUserId);
            } else {
                removeFavoriter(myUserId);
            }
            if (status.getCurrentUserRetweetId() > 0) {
                addRetweet(myUserId, status.getCurrentUserRetweetId());
            }
        } else {
            retweetedTweet = Tweet.fromTwitter(status.getRetweetedStatus(), myUserId);
            retweetedTweet.addRetweet(this);
            if (status.isFavorited()) {
                retweetedTweet.addFavoriter(myUserId);
            }
            if (status.getCurrentUserRetweetId() > 0) {
                retweetedTweet.addRetweet(myUserId, status.getCurrentUserRetweetId());
            }
        }
    }

    public String getTwitterUrl() {
        return String.format("https://twitter.com/%s/status/%s", getUser().getScreenName(), id);
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getText() {
        return getOriginalTweet().text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getSource() {
        return source;
    }

    public boolean isRetweet() {
        return isRetweet;
    }

    public Tweet getRetweetedTweet() {
        return retweetedTweet;
    }

    public Tweet getOriginalTweet() {
        if (isRetweet()) {
            return getRetweetedTweet();
        } else {
            return this;
        }
    }

    public int getFavoriteCount() {
        return getOriginalTweet().favoriteCount;
    }

    public int getRetweetCount() {
        return getOriginalTweet().retweetCount;
    }

    public long getInReplyToStatusId() {
        return getOriginalTweet().inReplyToStatusId;
    }

    public Tweet getInReplyToIfPresent() {
        return Tweet.fetch(getInReplyToStatusId());
    }

    public boolean isFavoritedBy(long id) {
        return getOriginalTweet().favoriters.contains(id);
    }

    public Set<Long> getFavoriters() {
        return getOriginalTweet().favoriters;
    }

    public boolean addFavoriter(long id) {
        boolean changed = getOriginalTweet().favoriters.add(id);
        if (changed) notifyChange(RBinding.FAVORITERS);
        return changed;
    }

    public boolean removeFavoriter(long id) {
        boolean changed = getOriginalTweet().favoriters.remove(id);
        if (changed) notifyChange(RBinding.FAVORITERS);
        return changed;
    }

    public boolean isRetweetedBy(long id) {
        return getOriginalTweet().retweets.get(id) != null;
    }

    public long getRetweetIdBy(long id) {
        return getOriginalTweet().retweets.get(id);
    }

    public Map<Long, Long> getRetweets() {
        return getOriginalTweet().retweets;
    }

    public boolean addRetweet(Tweet retweet) {
        return addRetweet(retweet.getUser().getId(), retweet.getId());
    }

    public boolean addRetweet(long uid, long sid) {
        Long result = getOriginalTweet().retweets.put(uid, sid);
        boolean changed = result == null || result != sid;
        if (changed) notifyChange(RBinding.RETWEETERS);
        return changed;
    }

    private boolean removeRetweet(long sid) {
        boolean changed = getOriginalTweet().retweets.values().remove(sid);
        if (changed) notifyChange(RBinding.RETWEETERS);
        return changed;
    }

    // helper methods::
    public List<Long> getEmbeddedStatusIDs() {
        ArrayList<Long> list = new ArrayList<>();
        for (String url : getUrlsExpanded()) {
            Uri uri = Uri.parse(url);
            if ("twitter.com".equals(uri.getHost())) {
                String[] arr = uri.toString().split("/");
                if (arr.length > 2 && "status".equals(arr[arr.length - 2])) {
                    list.add(Long.parseLong(arr[arr.length - 1].split("\\?")[0]));
                }
            }
        }
        return list;
    }

    // override EntitySupport
    @Override
    public List<String> getMentions() {
        if (isRetweet) {
            return getOriginalTweet().getMentions();
        } else {
            return super.getMentions();
        }
    }

    @Override
    public List<String> getHashtags() {
        if (isRetweet) {
            return getOriginalTweet().getHashtags();
        } else {
            return super.getHashtags();
        }
    }

    @Override
    public List<String> getMediaUrls() {
        if (isRetweet) {
            return getOriginalTweet().getMediaUrls();
        } else {
            return super.getMediaUrls();
        }
    }

    @Override
    public List<String> getUrlsExpanded() {
        if (isRetweet) {
            return getOriginalTweet().getUrlsExpanded();
        } else {
            return super.getUrlsExpanded();
        }
    }

    @Override
    public List<String> getSymbols() {
        if (isRetweet) {
            return getOriginalTweet().getSymbols();
        } else {
            return super.getSymbols();
        }
    }
}
