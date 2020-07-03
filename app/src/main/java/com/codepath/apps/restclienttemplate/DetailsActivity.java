package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.databinding.ActivityDetailsBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

import java.text.NumberFormat;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class DetailsActivity extends AppCompatActivity {
    private final int REQUEST_CODE_REPLY = 20;

    ImageView ivProfileImage;
    TextView tvBody;
    TextView tvScreenName;
    TextView tvSpacedName;
    TextView tvRelativeTime;
    ImageView ivContentImage;

    ImageView ivReply;
    ImageView ivRetweet;
    TextView tvNumRetweets;
    ImageView ivHeart;
    TextView tvNumLikes;

    Context context;
    Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_details);

        ActivityDetailsBinding binding = ActivityDetailsBinding.inflate(getLayoutInflater());

        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_twitter_logo);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        ivProfileImage = binding.ivProfileImage;
        tvBody = binding.tvBody;
        tvScreenName = binding.tvScreenName;
        tvSpacedName = binding.tvSpacedName;
        tvRelativeTime = binding.tvRelativeTime;
        ivContentImage = binding.ivContentImage;

        ivReply = binding.ivReply;
        ivRetweet = binding.ivRetweet;
        tvNumRetweets = binding.tvNumRetweets;
        ivHeart = binding.ivHeart;
        tvNumLikes = binding.tvNumLikes;

        context = this;
        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));

        tvBody.setText(tweet.body);
        tvScreenName.setText(String.format("@%s", tweet.user.screenName));
        tvSpacedName.setText(tweet.user.name);
        tvRelativeTime.setText(ParseRelativeDate.getRelativeTimeAgo(tweet.createdAt));
        tvNumRetweets.setText(NumberFormat.getIntegerInstance().format(tweet.retweetCount));
        tvNumLikes.setText(NumberFormat.getIntegerInstance().format(tweet.likeCount));

        Glide.with(context)
                .load(tweet.user.profileImageUrl)
                .transform(new RoundedCornersTransformation(30, 10))
                .into(ivProfileImage);

        if (!tweet.contentImageUrl.equals("")) {
            ivContentImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(tweet.contentImageUrl)
                    .transform(new RoundedCornersTransformation(20, 10))
                    .into(ivContentImage);
        } else {
            ivContentImage.setVisibility(View.GONE);
        }

        if (tweet.isLiked) {
            ivHeart.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_vector_heart_full));
            ivHeart.setColorFilter(ContextCompat.getColor(context,R.color.medium_red_lighter));
        }

        if (tweet.isRetweeted) {
            ivRetweet.setColorFilter(ContextCompat.getColor(context,R.color.medium_green));
        }

        // Reply button
        ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ReplyActivity.class);
                intent.putExtra("tweet", Parcels.wrap(tweet));
                startActivityForResult(intent, REQUEST_CODE_REPLY);
            }
        });

        // Retweet button
        ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tweet.isRetweeted) {
                    ivRetweet.setColorFilter(ContextCompat.getColor(context,R.color.medium_gray));
                    tweet.retweetCount--;
                    tvNumRetweets.setText(NumberFormat.getIntegerInstance().format(tweet.retweetCount));
                    tweet.isRetweeted = false;
                } else {
                    ivRetweet.setColorFilter(ContextCompat.getColor(context,R.color.medium_green));
                    tweet.retweetCount++;
                    tvNumRetweets.setText(NumberFormat.getIntegerInstance().format(tweet.retweetCount));
                    tweet.isRetweeted = true;
                }
            }
        });

        // Like button
        ivHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tweet.isLiked) {
                    ivHeart.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_vector_heart));
                    ivHeart.setColorFilter(ContextCompat.getColor(context,R.color.medium_gray));
                    tweet.likeCount--;
                    tvNumLikes.setText(NumberFormat.getIntegerInstance().format(tweet.likeCount));
                    tweet.isLiked = false;
                } else {
                    ivHeart.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_vector_heart_full));
                    ivHeart.setColorFilter(ContextCompat.getColor(context,R.color.medium_red_lighter));
                    tweet.likeCount++;
                    tvNumLikes.setText(NumberFormat.getIntegerInstance().format(tweet.likeCount));
                    tweet.isLiked = true;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.i("pressed", "pressed");
        Log.i("pressed", String.valueOf(tweet.isLiked));
        Intent intent = new Intent();
        // Passes the data back as a result
        intent.putExtra("tweet", Parcels.wrap(tweet));
        // Sets result code and bundle data for response
        setResult(RESULT_OK, intent);
        Log.i("pressed", "here");
        // Closes the activity, passing the data to the parent
        finish();
        super.onBackPressed();
    }
}