package com.codepath.apps.restclienttemplate.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
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


        // Find the views and set them
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

        // Attaches view to values
        wireUI();

        // Sets retweet and favorite
        setButtonColors();

        // Listeners for retweet and favorite
        retweetListener();
        favoriteListener();


    }

    // Listener for retweet button click
    private void retweetListener(){
        retweet.setOnClickListener(new View.OnClickListener() {
            // Handler for add button click
            @Override
            public void onClick(View v) {
                Log.i(TAG, "retweet clicked!");
                if(tweet.retweeted) {
                    unretweet(tweet);
                }
                else{
                    retweet(tweet);
                }
            }
        });
    }

    // Listener for favorite button click
    private void favoriteListener(){
        favorite.setOnClickListener(new View.OnClickListener() {
            // Handler for add button click
            @Override
            public void onClick(View v) {
                Log.i(TAG, "favorite clicked!");
                if(tweet.favorited) {
                    unfavorite(tweet);
                    //tweet.favorited = false;
                }
                else{
                    favorite(tweet);
                    //tweet.favorited = true;
                }
            }
        });
    }

    private void setButtonColors(){
        // sets retweet button to be the right color
        if(tweet.isRetweeted()){
            Log.i(TAG,"this has been retweeted");
            retweet.setBackgroundResource(R.drawable.ic_vector_retweet_blue);
        }
        else{
            retweet.setBackgroundResource(R.drawable.ic_vector_retweet);
        }

        // sets favorite button to be the right color
        if(tweet.isFavorited()){
            favorite.setBackgroundResource(R.drawable.ic_vector_heart_blue);
        }
        else{
            favorite.setBackgroundResource(R.drawable.ic_vector_heart);
        }

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
    }

    private void retweet(final Tweet tweet){
        // Send an API request to post the retweet
        client.retweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess retweet!");
                retweet.setBackgroundResource(R.drawable.ic_vector_retweet_blue);
                tweet.retweeted = true;
                tweet.retweetCount++;
                retweetCount.setText(Integer.toString(tweet.retweetCount));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retweet" + response, throwable);
            }
            // maxId is the id of the last tweet (older tweets have lower ids)
        }, tweet.id);
    }

    private void unretweet(final Tweet tweet){
        // Send an API request to post the unretweet
        client.unretweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess unretweet!");
                retweet.setBackgroundResource(R.drawable.ic_vector_retweet);
                tweet.retweeted = false;
                tweet.retweetCount--;
                retweetCount.setText(Integer.toString(tweet.retweetCount));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retweet" +response, throwable);
            }
            // maxId is the id of the last tweet (older tweets have lower ids)
        }, tweet.id);
    }

    private void favorite(final Tweet tweet){
        // Send an API request to post the favorite
        client.favorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess favorite!");
                favorite.setBackgroundResource(R.drawable.ic_vector_heart_blue);
                tweet.favorited = true;
                tweet.favoriteCount++;
                favoriteCount.setText(Integer.toString(tweet.favoriteCount));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to favorite" + response, throwable);
            }
            // maxId is the id of the last tweet (older tweets have lower ids)
        }, tweet.id);
    }

    private void unfavorite(final Tweet tweet){
        // Send an API request to post unfavorite
        client.unfavorite(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess unfavorite!");
                tweet.favorited = false;
                tweet.favoriteCount--;
                favorite.setBackgroundResource(R.drawable.ic_vector_heart);
                favoriteCount.setText(Integer.toString(tweet.favoriteCount));
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to unfavorite" + response, throwable);
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


    // Listener for back button is pressed
    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra("tweet", Parcels.wrap(tweet));
        setResult(RESULT_OK,i);
        finish();
    }
}
