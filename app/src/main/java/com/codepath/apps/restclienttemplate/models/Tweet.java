package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {
    public static final String TAG = "Tweet";

    public String body;
    public String createdAt;
    public long id;
    public User user;
    public String contentImageUrl;
    public int retweetCount;
    public int likeCount;
    public boolean isRetweeted;
    public boolean isLiked;


    // Empty constructor for Parceler library
    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.id = jsonObject.getLong("id");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.contentImageUrl = getContentImageUrl(jsonObject);

        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.likeCount = jsonObject.getInt("favorite_count");
        tweet.isRetweeted = jsonObject.getBoolean("retweeted");
        tweet.isLiked = jsonObject.getBoolean("favorited");
//        Log.i("likecount", tweet.body);
//        Log.i("likecount", String.valueOf(jsonObject.getInt("favorite_count")));

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }

        return tweets;
    }

    public static String getContentImageUrl(JSONObject jsonObject) {
        String contentImageUrl;
        try {
            contentImageUrl = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
            Log.i(TAG, contentImageUrl);
        } catch (JSONException e) {
            Log.i(TAG, "No media");
            contentImageUrl = "";
        }
        return contentImageUrl;
    }
}
