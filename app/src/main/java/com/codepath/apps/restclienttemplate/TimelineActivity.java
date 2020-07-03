package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeDialogFragment.EditNameDialogListener {
    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE_POST = 10;
    private final int REQUEST_CODE_REPLY = 20;
    private final int REQUEST_CODE_DETAILS = 30;

    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    MenuItem miActionProgressItem;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_timeline);

        ActivityTimelineBinding binding = ActivityTimelineBinding.inflate(getLayoutInflater());

        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_twitter_logo);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        client = TwitterApp.getRestClient(this);

        swipeContainer = binding.swipeContainer;
        // Configures the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "Fetching new data");
                populateHomeTimeline();
            }
        });

        // Finds the RecyclerView
        rvTweets = binding.rvTweets;

        // Initializes the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        // Sets up layout manager and adapter for RecyclerView
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);
        rvTweets.addItemDecoration(itemDecoration);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore: " + page);
                loadMoreData();
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        populateHomeTimeline();
    }

    private void loadMoreData() {
        miActionProgressItem.setVisible(true);
        // Sends an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for loadMoreData");

                // Deserializes and constructs a new model object from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Appends the new data objects to the existing set of items and notifies the adapter of the new items
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                miActionProgressItem.setVisible(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure for loadMoreData");
            }
        }, tweets.get(tweets.size() - 1).id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates the menu, adding items to the action bar if it's present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Return to finish
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If compose icon has been selected
        if (item.getItemId() == R.id.compose) {
            // Navigate to the compose activity
//            Intent intent = new Intent(this, ComposeActivity.class);
//            startActivityForResult(intent, REQUEST_CODE_POST);
            FragmentManager fm = getSupportFragmentManager();
            ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance("Some Title");
            composeDialogFragment.show(fm, "fragment_edit_name");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_POST && resultCode == RESULT_OK) {
            // Gets the data (tweet) from the intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

            // Updates the RecyclerView with the tweet
            tweets.add(0, tweet);
            // Notifies the adapter that the tweet was added
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }

        if (requestCode == REQUEST_CODE_REPLY && resultCode == RESULT_OK) {
            // Gets the data (tweet) from the intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

            int indexFirstSpace = tweet.body.indexOf(' ');
            tweet.body = tweet.body.substring(0, indexFirstSpace) + '\n' + '\n' + tweet.body.substring(indexFirstSpace + 1);
            tweet.body = "Replying to " + tweet.body;

            // Updates the RecyclerView with the tweet
            tweets.add(0, tweet);
            // Notifies the adapter that the tweet was added
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }

        if (requestCode == REQUEST_CODE_DETAILS) {
            // Gets the data (tweet) from the intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

            int position = -1;

            for (int i = 0; i < tweets.size(); i++) {
                if (tweets.get(i).id == tweet.id) {
                    position = i;
                    break;
                }
            }
            
            // Updates the RecyclerView with the tweet
            tweets.remove(position);
            tweets.add(position, tweet);
            // Notifies the adapter that the tweet was added
            adapter.notifyItemChanged(position);
            rvTweets.smoothScrollToPosition(position);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess " + json.toString());

                JSONArray jsonArray = json.jsonArray;
                try {
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));

                    // Signals refresh has finished
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON excpetion", e);
                    e.printStackTrace();
                }
                miActionProgressItem.setVisible(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure", throwable);
            }
        });
    }

    @Override
    public void onFinishEditDialog(Tweet tweet) {
        // Updates the RecyclerView with the tweet
        tweets.add(0, tweet);
        // Notifies the adapter that the tweet was added
        adapter.notifyItemInserted(0);
        rvTweets.smoothScrollToPosition(0);
    }
}