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

    private Tweet() {
    }

    private void update(twitter4j.Status status, long myUserId) {
        id = status.getId();
        user = User.fromTwitter(status.getUser());
        text = extractText(status, false);
        createdAt = status.getCreatedAt();
        source = status.getSource();
        favoriteCount = status.getFavoriteCount();
        retweetCount = status.getRetweetCount();

        inReplyTo = status.getInReplyToStatusId();
        isRetweet = status.isRetweet();
        if (isRetweet()) {
            retweetedTweet = Tweet.fromTwitter(status.getRetweetedStatus(), myUserId);
        }

        if (favoriters == null) {
            if (isRetweet()) {
                favoriters = getRetweetedTweet().getFavoriters();
            } else {
                favoriters = Collections.newSetFromMap(new ConcurrentHashMap<>());
            }
        }

        if (status.isFavorited()) {
            favoriters.add(myUserId);
        } else {
            favoriters.remove(myUserId);
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
        return favoriters.add(id); // false means already added
    }

    public boolean removeFavoriter(long id) {
        return favoriters.remove(id); //false means not contained
    }
}
