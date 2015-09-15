package net.lacolaco.smileessence.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.lacolaco.smileessence.twitter.util.TwitterUtils;
import net.lacolaco.smileessence.util.ListUtils;
import twitter4j.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tweet {
    // キャッシュ こっちは soft reference
    private static Cache<Long, Tweet> storage = CacheBuilder.newBuilder().softValues().build();

    public synchronized static Tweet fetch(long statusId) {
        return storage.getIfPresent(statusId);
    }

    public synchronized static void remove(long statusId) {
        storage.invalidate(statusId);
    }

    public synchronized static Tweet fromTwitter(final twitter4j.Status st) {
        Tweet t = fetch(st.getId());
        if (t == null) {
            t = new Tweet(st);
            storage.put(st.getId(), t);
        } else {
            t.update(st);
        }
        return t;
    }

    public synchronized static List<Tweet> fromTwitter(List<Status> sts) {
        return ListUtils.map(sts, Tweet::fromTwitter);
    }

    // インスタンス
    private long id;
    private User user;
    private String text;
    private Date createdAt;
    private String source;
    private boolean isRetweet;
    private Tweet retweetedTweet;
    private UserMentionEntity[] mentions;
    private HashtagEntity[] hashtags;
    private MediaEntity[] media;
    private URLEntity[] urls;
    private SymbolEntity[] symbols;
    private long inReplyTo;
    private int favoriteCount;
    private int retweetCount;

    private Tweet(twitter4j.Status st) {
        update(st);
    }

    private void update(twitter4j.Status status) {
        id = status.getId();
        user = User.fromTwitter(status.getUser());
        text = TwitterUtils.replaceURLEntities(status.getText(), status.getURLEntities(), false);
        createdAt = status.getCreatedAt();
        source = status.getSource();
        favoriteCount = status.getFavoriteCount();
        retweetCount = status.getRetweetCount();

        mentions = status.getUserMentionEntities();
        hashtags = status.getHashtagEntities();
        media = status.getExtendedMediaEntities().length > 0 ? status.getExtendedMediaEntities() : status.getMediaEntities();
        urls = status.getURLEntities();
        symbols = status.getSymbolEntities();
        inReplyTo = status.getInReplyToStatusId();

        isRetweet = status.isRetweet();
        if (isRetweet()) {
            retweetedTweet = Tweet.fromTwitter(status.getRetweetedStatus());
        }
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

    public UserMentionEntity[] getMentions() {
        return mentions;
    }

    public HashtagEntity[] getHashtags() {
        return hashtags;
    }

    public MediaEntity[] getMedia() {
        return media;
    }

    public URLEntity[] getUrls() {
        return urls;
    }

    public SymbolEntity[] getSymbols() {
        return symbols;
    }

    public long getInReplyTo() {
        return inReplyTo;
    }

    public List<String> getMentioningScreenNames(String excludeScreenName) {
        List<String> names = getMentioningScreenNames();
        if (excludeScreenName != null) {
            names.remove(excludeScreenName);
        }
        return names;
    }

    public List<String> getMentioningScreenNames() {
        List<String> names = new ArrayList<>();
        names.add(getUser().getScreenName());
        if (getMentions() != null) {
            for (UserMentionEntity entity : getMentions()) {
                if (!names.contains(entity.getScreenName())) {
                    names.add(entity.getScreenName());
                }
            }
        }
        return names;
    }
}
