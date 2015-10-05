package net.lacolaco.smileessence.entity;

import android.test.InstrumentationTestCase;
import net.lacolaco.smileessence.util.TwitterMock;
import twitter4j.TwitterObjectFactory;

import java.util.EnumSet;

public class TweetTest extends InstrumentationTestCase {
    TwitterMock mock;

    @Override
    public void setUp() throws Exception {
        mock = new TwitterMock(getInstrumentation().getContext());
    }

    // begin: static methods
    public void testFetch() throws Exception {
        // register a Tweet
        Tweet sample = Tweet.fromTwitter(mock.getTweetRawMock(), 0);
        assertSame(sample, Tweet.fetch(sample.getId()));
        assertNull(Tweet.fetch(sample.getId() + 1));
    }

    public void testRemove() throws Exception {
        Tweet sample = Tweet.fromTwitter(mock.getTweetRawMock(), 0);
        Tweet.remove(sample.getId());
        assertNull(Tweet.fetch(sample.getId()));
    }

    public void testFromTwitterSingle() throws Exception {
        twitter4j.Status status = mock.getTweetRawMock();
        Tweet first = Tweet.fromTwitter(status, 0);
        Tweet second = Tweet.fromTwitter(status, 1);
        assertSame(first, second);
    }
    // end: static methods

    // begin: update
    public void testUpdateData() throws Exception {
        twitter4j.Status status = mock.getTweetRawMock();
        Tweet original = Tweet.fromTwitter(status, status.getUser().getId());
        assertEquals(status.getId(), original.getId());
        assertEquals(0, original.getFavoriteCount());

        String newJson = mock.getTweetJSONMock().replace("\"favorite_count\":0", "\"favorite_count\":100");
        assertTrue("[BUG] status.json is invalid", newJson.contains("\"favorite_count\":100"));
        twitter4j.Status updatedStatus = TwitterObjectFactory.createStatus(newJson);

        Tweet updated = Tweet.fromTwitter(updatedStatus, 12345L);
        assertEquals(100, updated.getFavoriteCount());
        assertTrue("favoriter list contains user_id 12345", updated.getFavoriters().contains(12345L));
    }

    public void testUpdateObserver() throws Exception {
        final O<EnumSet<RBinding>> changes = new O<>();
        twitter4j.Status status = mock.getTweetRawMock();
        Tweet original = Tweet.fromTwitter(status, 0);
        original.addObserver(this, changes_ -> changes.object = changes_);

        String newJson = mock.getTweetJSONMock().replace("\"favorite_count\":0", "\"favorite_count\":100");
        assertTrue("[BUG] status.json is invalid", newJson.contains("\"favorite_count\":100"));
        twitter4j.Status updatedStatus = TwitterObjectFactory.createStatus(newJson);

        Tweet.fromTwitter(updatedStatus, 0);
        assertTrue("changes contains REACTION_COUNT", changes.object.contains(RBinding.REACTION_COUNT));
        Tweet.fromTwitter(updatedStatus, 12345);
        assertTrue("changes contains FAVORITERS", changes.object.contains(RBinding.FAVORITERS));
    }
    // end: update

    // begin: instance methods
    public void testGetTwitterUrl() throws Exception {
        Tweet sample = Tweet.fromTwitter(mock.getTweetRawMock(), 0);
        assertTrue("twitter URL contains status ID", sample.getTwitterUrl().contains(String.valueOf(sample.getId())));
    }

    public void testAddFavoriter() throws Exception {
        Tweet sample = Tweet.fromTwitter(mock.getTweetRawMock(), 0);
        assertEquals(1, sample.getFavoriters().size());
        sample.addFavoriter(12345);
        assertEquals(2, sample.getFavoriters().size());
    }

    public void testRemoveFavoriter() throws Exception {
        Tweet sample = Tweet.fromTwitter(mock.getTweetRawMock(), 0);
        assertEquals(1, sample.getFavoriters().size());
        sample.removeFavoriter(0);
        assertEquals(0, sample.getFavoriters().size());
    }

    public void testGetEmbeddedStatusIds() throws Exception {
        Tweet sample = Tweet.fromTwitter(mock.getTweetRawMock(), 0);
        assertTrue(sample.getEmbeddedStatusIDs().contains(441249190841032704L));
    }

    // utility class
    static class O<T> {
        public T object;
    }
}
