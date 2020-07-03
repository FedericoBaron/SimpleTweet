package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.activities.TweetDetailsActivity;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    private static final String TAG = "TweetsAdapter";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private Context context;
    private List<Tweet> tweets;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets){
        this.context = context;
        this.tweets = tweets;
    }


    // For each row, inflate the layout (expensive operation)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use layout inflator to inflate a view
        // Inflator turns XML content into a view
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemTweetBinding binding = ItemTweetBinding.inflate(inflater);


        return new ViewHolder(binding);
    }

    // Bind values based on the position of the element (cheap operation)
    // RecyclerView works efficiently by binding to old views
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);

        // Bind the data with the View Holder
        holder.bind(tweet);
    }

    // Gets the amount of tweet objects
    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clear all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    // More efficient way of clearing and adding items
    public void setAll(List<Tweet> tweetList)
    {
        tweets.clear();
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }


    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenUsername;
        TextView relativeDate;
        TextView tvScreenName;
        ImageView attachedImage;

        public ViewHolder(ItemTweetBinding binding){
            super(binding.getRoot());
            ivProfileImage = binding.ivProfileImage;
            tvBody = binding.tvBody;
            tvScreenUsername = binding.tvScreenUsername;
            relativeDate = binding.relativeDate;
            tvScreenName = binding.tvScreenName;
            attachedImage = binding.attachedImage;

            // Add this as the itemView's OnClickListener
            itemView.setOnClickListener(this);
        }

        // Binds tweet to view
        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenUsername.setText("@" + tweet.user.screenName);
            tvScreenName.setText(tweet.user.name);
            relativeDate.setText(getRelativeTimeAgo(tweet.createdAt));
            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);
            if(!tweet.imageUrl.isEmpty()){
                attachedImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(tweet.imageUrl).into(attachedImage);
                Log.i(TAG, "The image is: " + tweet.imageUrl);
            }
            else
            {
                attachedImage.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "OnClick adapter");
            // Gets item position
            int position = getAdapterPosition();

            // Make sure the position is valid i.e actually exists in the view
            if(position != RecyclerView.NO_POSITION) {
                // Get the tweet at the position, this won't work if the class is static
                Tweet tweet = tweets.get(position);

                // Create intent for the new activity
                Intent intent = new Intent(context, TweetDetailsActivity.class);

                // Serialize the tweet using the parceler, use its short name as a key
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));

                // Show the activity
                ((TimelineActivity) context).startActivityForResult(intent, 9);
            }
        }
    }

    // Gets how long ago something was tweeted in a good format
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "Just now";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "An hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } catch (ParseException e) {
            Log.i(TAG, "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }
}
