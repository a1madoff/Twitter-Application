package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {
    private final int REQUEST_CODE_REPLY = 20;
    private final int REQUEST_CODE_DETAILS = 30;

    Context context;
    List<Tweet> tweets;

    // Passes in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // Inflates the layout for each row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Binds values based on the position of the elements
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Gets the data at the position
        Tweet tweet = tweets.get(position);

        // Binds the tweet with the ViewHolder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Cleans all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Adds a list of items to the recycler
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    // Defines a ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvSpacedName = itemView.findViewById(R.id.tvSpacedName);
            tvRelativeTime = itemView.findViewById(R.id.tvRelativeTime);
            ivContentImage = itemView.findViewById(R.id.ivContentImage);

            ivReply = itemView.findViewById(R.id.ivReply);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);
            tvNumRetweets = itemView.findViewById(R.id.tvNumRetweets);
            ivHeart = itemView.findViewById(R.id.ivHeart);
            tvNumLikes = itemView.findViewById(R.id.tvNumLikes);

            itemView.setOnClickListener(this);

            // Reply button
            ivReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(context, ReplyActivity.class);
                        Tweet currentTweet = tweets.get(position);
                        intent.putExtra("tweet", Parcels.wrap(currentTweet));
                        ((TimelineActivity) context).startActivityForResult(intent, REQUEST_CODE_REPLY);
                    }
                }
            });

            // Retweet button
            ivRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Tweet currentTweet = tweets.get(position);
                        if (currentTweet.isRetweeted) {
                            ivRetweet.setColorFilter(ContextCompat.getColor(context,R.color.medium_gray));
                            currentTweet.retweetCount--;
                            tvNumRetweets.setText(NumberFormat.getIntegerInstance().format(currentTweet.retweetCount));
                            currentTweet.isRetweeted = false;
                        } else {
                            ivRetweet.setColorFilter(ContextCompat.getColor(context,R.color.medium_green));
                            currentTweet.retweetCount++;
                            tvNumRetweets.setText(NumberFormat.getIntegerInstance().format(currentTweet.retweetCount));
                            currentTweet.isRetweeted = true;
                        }
                    }
                }
            });

            // Like button
            ivHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Tweet currentTweet = tweets.get(position);
                        if (currentTweet.isLiked) {
                            ivHeart.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_vector_heart));
                            ivHeart.setColorFilter(ContextCompat.getColor(context,R.color.medium_gray));
                            currentTweet.likeCount--;
                            tvNumLikes.setText(NumberFormat.getIntegerInstance().format(currentTweet.likeCount));
                            currentTweet.isLiked = false;
                        } else {
                            ivHeart.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_vector_heart_full));
                            ivHeart.setColorFilter(ContextCompat.getColor(context,R.color.medium_red_lighter));
                            currentTweet.likeCount++;
                            tvNumLikes.setText(NumberFormat.getIntegerInstance().format(currentTweet.likeCount));
                            currentTweet.isLiked = true;
                        }
                    }
                }
            });
        }

        public void bind(Tweet tweet) {
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
            } else {
                ivHeart.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_vector_heart));
                ivHeart.setColorFilter(ContextCompat.getColor(context,R.color.medium_gray));
            }

            if (tweet.isRetweeted) {
                ivRetweet.setColorFilter(ContextCompat.getColor(context,R.color.medium_green));
            } else {
                ivRetweet.setColorFilter(ContextCompat.getColor(context,R.color.medium_gray));
            }
        }

        @Override
        public void onClick(View view) {
            Log.i("click", "clicked");
            // Clicking on tweet, go to details activity
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Tweet currentTweet = tweets.get(position);
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("tweet", Parcels.wrap(currentTweet));
                ((TimelineActivity) context).startActivityForResult(intent, REQUEST_CODE_DETAILS);
            }
        }
    }
}
