*This is an unofficial fork of [SmileEssence](https://github.com/laco0416/SmileEssence)*

SmileEssence
============

![icon](https://raw.githubusercontent.com/rhenium/SmileEssence/master/icon_application.png)

[Official Web Site](http://smileessence.lacolaco.net)

# Build

+ Clone this repository.
+ Add your application's API keys to `app/src/main/resources/twitter4j.properties` (for release builds) and `app/src/debug/resources/twitter4j.properties` (for debug builds / tests).

    ```
    oauth.consumerKey=YOUR_API_KEY
    oauth.consumerSecret=YOUR_API_KEY_SECRET
    ```

+ Add an OAuth token pair (for tests) to `app/src/androidTest/assets/tokens.properties`.

    ```
    accessToken=YOUR_OAUTH_TOKEN
    accessTokenSecret=YOUR_OAUTH_TOKEN_SECRET
    ```
