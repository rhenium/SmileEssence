package net.lacolaco.smileessence.entity;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.lacolaco.smileessence.BR;

public class User extends BaseObservable {
    // 重複防止用キャッシュ こっちは weak reference
    private static Cache<Long, User> storage = CacheBuilder.newBuilder().weakValues().build();

    public synchronized static User fetch(long userId) {
        return storage.getIfPresent(userId);
    }

    public synchronized static User fromTwitter(final twitter4j.User st) {
        User u = fetch(st.getId());
        if (u == null) {
            u = new User(st);
            storage.put(st.getId(), u);
        } else {
            u.update(st);
        }
        return u;
    }

    // インスタンス
    private long id;
    private boolean tweetProtected;
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

    private User(twitter4j.User st) {
        update(st);
    }

    private void update(twitter4j.User user) {
        id = user.getId();
        if (isTweetProtected() != user.isProtected()) {
            tweetProtected = user.isProtected();
            notifyPropertyChanged(BR.tweetProtected);
        }
        if (!getScreenName().equals(user.getScreenName())) {
            screenName = user.getScreenName();
            notifyPropertyChanged(BR.screenName);
        }
        if (!getName().equals(user.getName())) {
            name = user.getName();
            notifyPropertyChanged(BR.name);
        }
        if (!getProfileImageUrl().equals(user.getProfileBackgroundImageUrlHttps())) {
            profileImageUrl = user.getProfileImageURLHttps();
            notifyPropertyChanged(BR.profileImageUrl);
        }
        if (!getProfileBannerUrl().equals(user.getProfileBannerURL())) {
            profileBannerUrl = user.getProfileBannerURL();
            notifyPropertyChanged(BR.profileBannerUrl);
        }
        if (!getDescription().equals(user.getDescription())) {
            description = user.getDescription();
            notifyPropertyChanged(BR.description);
        }
        if (!getLocation().equals(user.getLocation())) {
            location = user.getLocation();
            notifyPropertyChanged(BR.location);
        }
        if (!getUrl().equals(user.getURL())) {
            url = user.getURL();
            notifyPropertyChanged(BR.url);
        }
        if (getFavoritesCount() != user.getFavouritesCount()) {
            favoritesCount = user.getFavouritesCount();
            notifyPropertyChanged(BR.favoritesCount);
        }
        if (getStatusesCount() != user.getStatusesCount()) {
            statusesCount = user.getStatusesCount();
            notifyPropertyChanged(BR.statusesCount);
        }
        if (getFriendsCount() != user.getFriendsCount()) {
            friendsCount = user.getFriendsCount();
            notifyPropertyChanged(BR.friendsCount);
        }
        if (getFollowersCount() != user.getFollowersCount()) {
            followersCount = user.getFollowersCount();
            notifyPropertyChanged(BR.followersCount);
        }
        if (isVerified() != user.isVerified()) {
            isVerified = user.isVerified();
            notifyPropertyChanged(BR.verified);
        }
    }

    public long getId() {
        return id;
    }

    @Bindable // TODO: workaround for bugs in com.android.databinding
    public boolean isTweetProtected() {
        return tweetProtected;
    }

    @Bindable
    public String getScreenName() {
        return screenName;
    }

    @Bindable
    public String getName() {
        return name;
    }

    @Bindable
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

    @Bindable
    public String getProfileBannerUrl() {
        return profileBannerUrl;
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    @Bindable
    public String getLocation() {
        return location;
    }

    @Bindable
    public String getUrl() {
        return url;
    }

    @Bindable
    public int getFavoritesCount() {
        return favoritesCount;
    }

    @Bindable
    public int getStatusesCount() {
        return statusesCount;
    }

    @Bindable
    public int getFriendsCount() {
        return friendsCount;
    }

    @Bindable
    public int getFollowersCount() {
        return followersCount;
    }

    @Bindable
    public boolean isVerified() {
        return isVerified;
    }


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
