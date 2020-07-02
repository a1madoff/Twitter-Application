package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.w3c.dom.Text;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

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
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvSpacedName;
        TextView tvRelativeTime;
        ImageView ivContentImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvSpacedName = itemView.findViewById(R.id.tvSpacedName);
            tvRelativeTime = itemView.findViewById(R.id.tvRelativeTime);
            ivContentImage = itemView.findViewById(R.id.ivContentImage);

        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(String.format("@%s", tweet.user.screenName));
            tvSpacedName.setText(tweet.user.name);
            tvRelativeTime.setText(ParseRelativeDate.getRelativeTimeAgo(tweet.createdAt));

            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .transform(new RoundedCornersTransformation(30, 10))
                    .into(ivProfileImage);

            if (!tweet.contentImageUrl.equals("")) {
                ivContentImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(tweet.contentImageUrl)
                        .transform(new RoundedCornersTransformation(50, 10))
                        .into(ivContentImage);
            } else {
                ivContentImage.setVisibility(View.GONE);
            }
        }
    }
}
