package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ReplyActivity extends AppCompatActivity {

    public static final String TAG = "ReplyActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    EditText etCompose;
    Button btnTweet;
    TextView tvReplyTo;

    Tweet tweet;
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_twitter_logo);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        client = TwitterApp.getRestClient(this);

        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);
        tvReplyTo = findViewById(R.id.tvReplyTo);

        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        tvReplyTo.setText(String.format("In reply to %s", tweet.user.name));
        etCompose.setText(String.format("@%s", tweet.user.screenName));

        // Sets a click listener on the button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();

                if (tweetContent.isEmpty()) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                } else if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ReplyActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ReplyActivity.this, tweetContent, Toast.LENGTH_LONG).show();
                }

                // Makes an API call to Twitter to publish the tweet
                client.publishReply(tweet.id, tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says " + tweet.body);
                            Intent intent = new Intent();
                            // Passes the data back as a result
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            // Sets result code and bundle data for response
                            setResult(RESULT_OK, intent);
                            // Closes the activity, passing the data to the parent
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });
            }
        });
    }
}