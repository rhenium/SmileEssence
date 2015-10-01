package net.lacolaco.smileessence.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.lacolaco.smileessence.util.UIObservable;

public class User extends UIObservable {
    private static Cache<Long, User> storage = CacheBuilder.newBuilder().weakValues().build();

    public synchronized static User fetch(long userId) {
        return storage.getIfPresent(userId);
    }

    public synchronized static User fromTwitter(final twitter4j.User st) {
        User u = fetch(st.getId());
        if (u == null) {
            u = new User();
            storage.put(st.getId(), u);
        }

        u.update(st);
        return u;
    }

    // only for initialization; DO NOT have reference for this object
    public synchronized static User _makeSkeleton(long id, String screenName) {
        User u = fetch(id);
        if (u != null) {
            return u;
        } else {
            u = new User();
            u.id = id;
            u.screenName = screenName;
            storage.put(id, u);
            return u;
        }
    }

    // インスタンス
    private long id;
    private boolean isProtected;
    private String screenName;
    private String name;
    private String profileImageUrl;
    private String profileBannerUrl;
    private String description;
    private String location;
    private String url;
    private int favoritesCount;
    private int statusesCount;
    private int friendsCount;
    private int followersCount;
    private boolean isVerified;

    private User() {
    }

    private void update(twitter4j.User user) {
        id = user.getId();

        if (isProtected() != user.isProtected() ||
                getScreenName() == null || !getScreenName().equals(user.getScreenName()) ||
                getName() == null || !getName().equals(user.getName()) ||
                getProfileImageUrl() == null || !getProfileImageUrl().equals(user.getProfileImageURLHttps())) {
            isProtected = user.isProtected();
            if (user.getScreenName() != null)
                screenName = user.getScreenName();
            if (user.getName() != null)
                name = user.getName();
            if (user.getProfileImageURLHttps() != null)
                profileImageUrl = user.getProfileImageURLHttps();

            notifyChange(RBinding.BASIC);
        }

        if (getProfileBannerUrl() == null || !getProfileBannerUrl().equals(user.getProfileBannerURL()) ||
                getDescription() == null || !getDescription().equals(user.getDescription()) ||
                getLocation() == null || !getLocation().equals(user.getLocation()) ||
                getUrl() == null || !getUrl().equals(user.getURL()) ||
                getFavoritesCount() != user.getFavouritesCount() ||
                getStatusesCount() != user.getStatusesCount() ||
                getFriendsCount() != user.getFriendsCount() ||
                getFollowersCount() != user.getFollowersCount()) {
            isVerified = user.isVerified();
            if (user.getProfileBannerURL() != null)
                profileBannerUrl = user.getProfileBannerURL();
            if (user.getDescription() != null)
                description = user.getDescription();
            if (user.getLocation() != null)
                location = user.getLocation();
            if (user.getURL() != null)
                url = user.getURL();
            if (user.getFavouritesCount() != -1)
                favoritesCount = user.getFavouritesCount();
            if (user.getStatusesCount() != -1)
                statusesCount = user.getStatusesCount();
            if (user.getFriendsCount() != -1)
                friendsCount = user.getFriendsCount();
            if (user.getFollowersCount() != -1)
                followersCount = user.getFollowersCount();

            notifyChange(RBinding.DETAIL);
        }
    }

    public long getId() {
        return id;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getName() {
        return name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getProfileImageUrlOriginal() {
        return getProfileImageUrlWithSuffix("");
    }

    private String getProfileImageUrlWithSuffix(String suffix) {
        String original = getProfileImageUrl();
        if (original != null) {
            String url = original.substring(0, original.lastIndexOf("_")) + suffix;
            int extIndex = original.lastIndexOf(".");
            if (extIndex > original.lastIndexOf("/")) {
                url += original.substring(extIndex);
            }
            return url;
        }
        return null;
    }

    public String getProfileBannerUrl() {
        return profileBannerUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getUrl() {
        return url;
    }

    public int getFavoritesCount() {
        return favoritesCount;
    }

    public int getStatusesCount() {
        return statusesCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public boolean isVerified() {
        return isVerified;
    }

    // helper methods
    public String getUserHomeURL() {
        return String.format("https://twitter.com/%s", getScreenName());
    }

    public String getAclogTimelineURL() {
        return String.format("http://aclog.koba789.com/%s/timeline", getScreenName());
    }

    public String getFavstarRecentURL() {
        return String.format("http://favstar.fm/users/%s/recent", getScreenName());
    }

    public String getTwilogURL() {
        return String.format("http://twilog.org/%s", getScreenName());
    }
}
