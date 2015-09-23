package net.lacolaco.smileessence.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.lacolaco.smileessence.util.ListUtils;
import twitter4j.Status;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Tweet extends EntitySupport {
    // キャッシュ こっちは soft reference
    private static Cache<Long, Tweet> storage = CacheBuilder.newBuilder().softValues().build();

    public synchronized static Tweet fetch(long statusId) {
        return storage.getIfPresent(statusId);
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
    private long inReplyTo;
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
        text = extractText(status, false);
        createdAt = status.getCreatedAt();
        source = status.getSource();

        if (getFavoriteCount() != status.getFavoriteCount() ||
                getRetweetCount() != status.getRetweetCount()) {
            favoriteCount = status.getFavoriteCount();
            retweetCount = status.getRetweetCount();

            notifyChange(RO.REACTION_COUNT);
        }

        inReplyTo = status.getInReplyToStatusId();
        isRetweet = status.isRetweet();
        if (isRetweet()) {
            retweetedTweet = Tweet.fromTwitter(status.getRetweetedStatus(), myUserId);
            retweetedTweet.addRetweet(this);
        }

        if (favoriters == null) {
            if (isRetweet()) {
                favoriters = getRetweetedTweet().getFavoriters();
            } else {
                favoriters = Collections.newSetFromMap(new ConcurrentHashMap<>());
            }
        }
        if (status.isFavorited()) {
            addFavoriter(myUserId);
        } else {
            removeFavoriter(myUserId);
        }

        if (retweets == null) {
            if (isRetweet()) {
                retweets = getRetweetedTweet().getRetweets();
            } else {
                retweets = new ConcurrentHashMap<>();
            }
        }
        if (status.getCurrentUserRetweetId() > 0) {
            addRetweet(myUserId, status.getCurrentUserRetweetId());
        }

        updateEntities(status);
    }

    public String getTwitterUrl() {
        return String.format("https://twitter.com/%s/status/%s", getOriginalTweet().getUser().getScreenName(), getOriginalTweet().getId());
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getText() {
        return text;
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
        return favoriteCount;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public long getInReplyTo() {
        return inReplyTo;
    }

    public boolean isFavoritedBy(long id) {
        return favoriters.contains(id);
    }

    public Set<Long> getFavoriters() {
        return favoriters;
    }

    public boolean addFavoriter(long id) {
        boolean changed = favoriters.add(id);
        if (changed) notifyChange(RO.FAVORITERS);
        return changed;
    }

    public boolean removeFavoriter(long id) {
        boolean changed = favoriters.remove(id);
        if (changed) notifyChange(RO.FAVORITERS);
        return changed;
    }

    public boolean isRetweetedBy(long id) {
        return retweets.get(id) != null;
    }

    public long getRetweetIdBy(long id) {
        return retweets.get(id);
    }

    public Map<Long, Long> getRetweets() {
        return retweets;
    }

    public boolean addRetweet(Tweet retweet) {
        return addRetweet(retweet.getUser().getId(), retweet.getId());
    }

    public boolean addRetweet(long uid, long sid) {
        Long result = retweets.put(uid, sid);
        boolean changed = result == null || result != sid;
        if (changed) notifyChange(RO.RETWEETERS);
        return changed;
    }

    private boolean removeRetweet(long sid) {
        boolean changed = retweets.values().remove(sid);
        if (changed) notifyChange(RO.RETWEETERS);
        return changed;
    }
}
