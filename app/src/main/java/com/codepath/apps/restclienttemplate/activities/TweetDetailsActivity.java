package com.codepath.apps.restclienttemplate.activities;

import android.content.res.ColorStateList;
import android.media.Image;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetDetailsActivity extends AppCompatActivity {

    private static final String KEY_LINK = "link";
    private static final String TAG = "TweetDetailsActivity";

    // The movie to display
    private Tweet tweet;

    private Button retweet;
    private ImageView attachedImage;
    private TextView tvScreenName;
    private TextView relativeDate;
    private TextView tvBody;
    private TextView tvScreenUsername;
    private ImageView ivProfileImage;
    private Button favorite;
    private TextView retweetCount;
    private TextView favoriteCount;

    private TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);

        client = TwitterApp.getRestClient(this);


        // the view objects
        // Find the views
        retweet = findViewById(R.id.retweet);
        attachedImage = findViewById(R.id.attachedImage);
        tvScreenName = findViewById(R.id.tvScreenName);
        relativeDate = findViewById(R.id.relativeDate);
        tvBody = findViewById(R.id.tvBody);
        tvScreenUsername = findViewById(R.id.tvScreenUsername);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        favorite = findViewById(R.id.favorite);
        retweetCount = findViewById(R.id.retweetCount);
        favoriteCount = findViewById(R.id.favoriteCount);


        // Unwrap the movie passed in via intent, using its simple name as a key
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        wireUI();
        retweetListener();
        favoriteListener();


    }

    // It connects the UI and the movie information
    private void wireUI()
    {
        tvBody.setText(tweet.getBody());
        tvScreenUsername.setText("@" + tweet.getUser().getScreenName());
        tvScreenName.setText(tweet.getUser().getName());
        relativeDate.setText(getRelativeTimeAgo(tweet.getCreatedAt()));
        retweetCount.setText(Integer.toString(tweet.getRetweetCount()));
        favoriteCount.setText(Integer.toString(tweet.getFavoriteCount()));
        Glide.with(this).load(tweet.getUser().getProfileImageUrl()).into(ivProfileImage);
        if(!tweet.imageUrl.isEmpty()){
            attachedImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(tweet.getImageUrl()).into(attachedImage);
            Log.i(TAG, "The image is: " + tweet.getImageUrl());
        }
        else
        {
            attachedImage.setVisibility(View.GONE);
        }

        if(tweet.isRetweeted()){
            retweet.setBackgroundResource(R.drawable.ic_vector_retweet_blue);
        }
        else{
            retweet.setBackgroundResource(R.drawable.ic_vector_retweet);
        }

        if(tweet.isFavorited()){
            favorite.setBackgroundResource(R.drawable.ic_vector_heart_blue);
        }
        else{
            favorite.setBackgroundResource(R.drawable.ic_vector_heart);
        }
    }

    private void retweetListener() {
        // Listener for thumbnail click
        retweet.setOnClickListener(new View.OnClickListener() {
            // Handler for add button click
            @Override
            public void onClick(View v) {
                Log.i(TAG, "retweet clicked!");
                if(tweet.retweeted) {
                    unretweet(tweet);
                    tweet.retweeted = false;
                }
                else{
                    retweet(tweet);
                    tweet.retweeted = true;
                }
            }
        });
    }

    private void favoriteListener() {
        // Listener for thumbnail click
        favorite.setOnClickListener(new View.OnClickListener() {
            // Handler for add button click
            @Override
            public void onClick(View v) {
                Log.i(TAG, "retweet clicked!");
                if(tweet.favorited) {
                    unfavorite(tweet);
                    tweet.favorited = false;
                }
                else{
                    favorite(tweet);
                    tweet.favorited = true;
                }
            }
        });
    }

    private void retweet(Tweet tweet){
        // Send an API request to post the retweet
        client.retweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess retweet!");
                retweet.setBackgroundResource(R.drawable.ic_vector_retweet_blue);
                retweetCount.setText(Integer.toString(Integer.parseInt(retweetCount.getText().toString()) + 1));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retweet", throwable);
            }
            // maxId is the id of the last tweet (older tweets have lower ids)
        }, tweet.id);
    }

    private void unretweet(Tweet tweet){
        // Send an API request to post the unretweet
        client.unretweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess unretweet!");
                retweet.setBackgroundResource(R.drawable.ic_vector_retweet);
                retweetCount.setText(Integer.toString(Integer.parseInt(retweetCount.getText().toString()) - 1));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retweet", throwable);
            }
            // maxId is the id of the last tweet (older tweets have lower ids)
        }, tweet.id);
    }

    private void favorite(Tweet tweet){
        // Send an API request to post the favorite
        client.favorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess favorite!");
                favorite.setBackgroundResource(R.drawable.ic_vector_heart_blue);
                favoriteCount.setText(Integer.toString(Integer.parseInt(favoriteCount.getText().toString()) + 1));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to favorite", throwable);
            }
            // maxId is the id of the last tweet (older tweets have lower ids)
        }, tweet.id);
    }

    private void unfavorite(Tweet tweet){
        // Send an API request to post unfavorite
        client.unfavorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess unfavorite!");
                favorite.setBackgroundResource(R.drawable.ic_vector_heart);
                favoriteCount.setText(Integer.toString(Integer.parseInt(favoriteCount.getText().toString()) - 1));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to unfavorite", throwable);
            }
            // maxId is the id of the last tweet (older tweets have lower ids)
        }, tweet.id);
    }


    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
